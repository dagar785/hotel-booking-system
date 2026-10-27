package com.hbs.buildlogic.gate

import org.yaml.snakeyaml.Yaml
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element

data class GateConfig(val baseRef: String, val minChangedLineCoverage: Int, val excludeGlobs: List<String>)
data class Waiver(val path: String, val category: String, val reason: String)
data class FileCoverage(val path: String, val covered: Int, val total: Int)

/** Pure functions only: everything here is unit-tested in build-logic/src/test. */
object GateLogic {
    val CATEGORIES = setOf("GENERATED", "TRIVIAL_WRAPPER", "UI_COVERED_ELSEWHERE", "TIME_BOXED")
    private val PLACEHOLDERS = setOf("todo", "n/a", "na", "none", "skip", "tbd", "no time", "later", "test later")
    const val MIN_REASON_LENGTH = 30

    @Suppress("UNCHECKED_CAST")
    fun parseConfig(yaml: String): GateConfig {
        val m = Yaml().load<Any?>(yaml) as? Map<String, Any?> ?: emptyMap()
        return GateConfig(
            baseRef = m["baseRef"]?.toString() ?: "baseline",
            minChangedLineCoverage = (m["minChangedLineCoverage"] as? Number)?.toInt() ?: 80,
            excludeGlobs = (m["exclude"] as? List<*>)?.map { it.toString() } ?: emptyList(),
        )
    }

    fun parseWaivers(yaml: String): List<Waiver> {
        val list = Yaml().load<Any?>(yaml) as? List<*> ?: return emptyList()
        return list.map { e ->
            val m = e as? Map<*, *> ?: error("test-waivers.yml: each entry needs path, category, reason")
            Waiver(m["path"]?.toString().orEmpty(), m["category"]?.toString().orEmpty(), m["reason"]?.toString().orEmpty())
        }
    }

    fun waiverProblems(waivers: List<Waiver>, exists: (String) -> Boolean): List<String> = waivers.flatMap { w ->
        buildList {
            if (w.path.isBlank() || !exists(w.path)) add("${w.path.ifBlank { "<blank path>" }}: waiver points to a file that does not exist (stale?)")
            if (w.category !in CATEGORIES) add("${w.path}: category '${w.category}' must be one of $CATEGORIES")
            val reason = w.reason.trim()
            if (reason.length < MIN_REASON_LENGTH || reason.lowercase() in PLACEHOLDERS) {
                add("${w.path}: reason must explain why a test is missing (>= $MIN_REASON_LENGTH chars, not a placeholder)")
            }
        }
    }

    fun globToRegex(glob: String): Regex {
        val sb = StringBuilder()
        var i = 0
        while (i < glob.length) {
            val c = glob[i]
            when {
                glob.startsWith("**/", i) -> { sb.append("(?:.*/)?"); i += 2 }
                glob.startsWith("**", i) -> { sb.append(".*"); i += 1 }
                c == '*' -> sb.append("[^/]*")
                c == '?' -> sb.append("[^/]")
                else -> sb.append(Regex.escape(c.toString()))
            }
            i++
        }
        return Regex(sb.toString())
    }

    /** Production Kotlin under src/main that is not excluded by test-gate.yml. */
    fun isGated(path: String, cfg: GateConfig): Boolean =
        path.endsWith(".kt") && "/src/main/" in path && cfg.excludeGlobs.none { globToRegex(it).matches(path) }

    /** A sibling `<Name>Test.kt` / `<Name>Tests.kt` anywhere in the module's src/test or src/androidTest. */
    fun hasTest(path: String, allFiles: Collection<String>): Boolean {
        val module = path.substringBefore("/src/main/")
        val name = path.substringAfterLast('/').removeSuffix(".kt")
        val wanted = setOf("${name}Test.kt", "${name}Tests.kt")
        return allFiles.any { f ->
            (f.startsWith("$module/src/test/") || f.startsWith("$module/src/androidTest/")) && f.substringAfterLast('/') in wanted
        }
    }

    /** Parses `git diff -U0` output into path -> added/changed line numbers (new side). */
    fun parseChangedLines(diff: String): Map<String, Set<Int>> {
        val out = linkedMapOf<String, MutableSet<Int>>()
        val hunk = Regex("""^@@ -\d+(?:,\d+)? \+(\d+)(?:,(\d+))? @@""")
        var current: String? = null
        for (line in diff.lineSequence()) {
            if (line.startsWith("+++ ")) {
                val p = line.removePrefix("+++ ").substringBefore('\t')
                current = if (p == "/dev/null") null else p.removePrefix("b/")
                continue
            }
            val file = current ?: continue
            val m = hunk.find(line) ?: continue
            val start = m.groupValues[1].toInt()
            val count = m.groupValues[2].ifEmpty { "1" }.toInt()
            out.getOrPut(file) { mutableSetOf() }.addAll(start until start + count)
        }
        return out
    }

    /** Jacoco XML -> "pkg/File.kt" -> (line -> covered). Only executable lines are listed. */
    fun parseJacoco(xml: File): Map<String, Map<Int, Boolean>> {
        val factory = DocumentBuilderFactory.newInstance().apply {
            setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        }
        val doc = factory.newDocumentBuilder().parse(xml)
        val out = mutableMapOf<String, MutableMap<Int, Boolean>>()
        val packages = doc.getElementsByTagName("package")
        for (i in 0 until packages.length) {
            val pkg = packages.item(i) as Element
            val files = pkg.getElementsByTagName("sourcefile")
            for (j in 0 until files.length) {
                val sf = files.item(j) as Element
                val lines = sf.getElementsByTagName("line")
                val map = out.getOrPut("${pkg.getAttribute("name")}/${sf.getAttribute("name")}") { mutableMapOf() }
                for (k in 0 until lines.length) {
                    val l = lines.item(k) as Element
                    val covered = l.getAttribute("ci").toInt()
                    if (l.getAttribute("mi").toInt() + covered > 0) map[l.getAttribute("nr").toInt()] = covered > 0
                }
            }
        }
        return out
    }

    /** reports: module dir -> parsed jacoco. Lines not in the report (comments, imports, declarations) are ignored. */
    fun measure(
        changed: Map<String, Set<Int>>,
        files: List<String>,
        reports: Map<String, Map<String, Map<Int, Boolean>>>,
    ): List<FileCoverage> = files.map { path ->
        val module = path.substringBefore("/src/main/")
        val lines = reports[module]?.entries?.firstOrNull { path.endsWith("/" + it.key) }?.value.orEmpty()
        val touched = changed[path].orEmpty().filter { it in lines }
        FileCoverage(path, touched.count { lines[it] == true }, touched.size)
    }

    fun percent(cov: List<FileCoverage>): Int {
        val total = cov.sumOf { it.total }
        return if (total == 0) 100 else cov.sumOf { it.covered } * 100 / total
    }
}
