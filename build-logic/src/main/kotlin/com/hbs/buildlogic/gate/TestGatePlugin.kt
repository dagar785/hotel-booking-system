package com.hbs.buildlogic.gate

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

/**
 * THE TEST GATE. Fails `./gradlew build` when production code changed since the `baseline` tag has no tests
 * and no justified waiver.  Config: test-gate.yml   Waivers: test-waivers.yml   Docs: docs/TEST_GATE.md
 *
 *  testGateExistence  fast  – every changed src/main file needs <Name>Test.kt or a waiver   (pre-commit hook)
 *  testGateCoverage   slow  – changed lines must reach minChangedLineCoverage via Jacoco     (pre-push hook)
 */
class TestGatePlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("base")
        val root = rootDir

        tasks.register("installGitHooks") {
            group = "verification"
            description = "Points git at .githooks so the test gate runs on commit/push."
            outputs.upToDateWhen { false }
            doLast {
                if (!File(root, ".git").exists()) return@doLast
                val current = runCatching { git(root, "config", "--get", "core.hooksPath").trim() }.getOrDefault("")
                if (current != ".githooks") git(root, "config", "core.hooksPath", ".githooks")
                File(root, ".githooks").listFiles()?.forEach { it.setExecutable(true) }
            }
        }

        val existence = tasks.register("testGateExistence") {
            group = "verification"
            description = "Test gate (fast): changed production files need a test or a justified waiver."
            outputs.upToDateWhen { false }
            doLast {
                val ctx = GateContext(root)
                val problems = ctx.waiverProblems().toMutableList()
                val missing = ctx.gatedChanged().filter { it !in ctx.waivedPaths() && !GateLogic.hasTest(it, ctx.allFiles()) }
                ctx.printWaivers(logger::lifecycle)
                if (problems.isNotEmpty() || missing.isNotEmpty()) {
                    throw GradleException(GateMessages.existenceFailure(missing, problems, ctx.cfg.baseRef))
                }
                logger.lifecycle("Test gate: ${ctx.gatedChanged().size} changed file(s) checked, all have tests or waivers.")
            }
        }

        val coverage = tasks.register("testGateCoverage") {
            group = "verification"
            description = "Test gate (slow): changed lines must meet minChangedLineCoverage (Jacoco)."
            dependsOn(existence)
            outputs.upToDateWhen { false }
            doLast {
                val ctx = GateContext(root)
                val files = ctx.gatedChanged().filter { it !in ctx.waivedPaths() }
                val reports = subprojects.associate { sp ->
                    sp.projectDir.relativeTo(root).path to
                        File(sp.projectDir, "build/reports/coverage/test/debug/report.xml").takeIf { it.exists() }
                            ?.let(GateLogic::parseJacoco).orEmpty()
                }
                val cov = GateLogic.measure(ctx.changedLines(), files, reports)
                val pct = GateLogic.percent(cov)
                logger.lifecycle("Test gate: changed-line coverage $pct% (required ${ctx.cfg.minChangedLineCoverage}%)")
                if (pct < ctx.cfg.minChangedLineCoverage) {
                    throw GradleException(GateMessages.coverageFailure(cov, pct, ctx.cfg.minChangedLineCoverage))
                }
            }
        }

        tasks.register("testGate") {
            group = "verification"
            description = "Runs both test gate checks. Explained in docs/TEST_GATE.md"
            dependsOn(existence, coverage)
        }

        // Gate self-tests live in the included build-logic build.
        tasks.named("check") { dependsOn("testGate", gradle.includedBuild("build-logic").task(":test")) }
        Unit
    }
}

internal class GateContext(private val root: File) {
    val cfg = GateLogic.parseConfig(File(root, "test-gate.yml").readText())
    private val waivers = File(root, "test-waivers.yml").takeIf { it.exists() }?.let { GateLogic.parseWaivers(it.readText()) }.orEmpty()
    private val base: String by lazy {
        runCatching { git(root, "merge-base", "HEAD", cfg.baseRef).trim() }.getOrElse {
            throw GradleException(GateMessages.noBase(cfg.baseRef))
        }
    }

    fun waivedPaths() = waivers.map { it.path }.toSet()
    fun waiverProblems() = GateLogic.waiverProblems(waivers) { File(root, it).exists() }
    fun allFiles(): List<String> = git(root, "ls-files", "--cached", "--others", "--exclude-standard").lines().filter { it.isNotBlank() }

    private fun untracked() = git(root, "ls-files", "--others", "--exclude-standard").lines().filter { it.isNotBlank() }

    fun gatedChanged(): List<String> {
        val tracked = git(root, "diff", "--name-only", "--diff-filter=ACMR", base).lines()
        return (tracked + untracked()).filter { it.isNotBlank() && GateLogic.isGated(it, cfg) }.distinct().sorted()
    }

    fun changedLines(): Map<String, Set<Int>> {
        val fromDiff = GateLogic.parseChangedLines(git(root, "diff", "-U0", "--no-color", base))
        val fromNew = untracked().associateWith { p -> (1..File(root, p).readLines().size).toSet() }
        return fromDiff + fromNew
    }

    fun printWaivers(log: (String) -> Unit) {
        val timeBoxed = waivers.filter { it.category == "TIME_BOXED" }
        if (waivers.isNotEmpty()) log("Test gate: ${waivers.size} active waiver(s), ${timeBoxed.size} TIME_BOXED (reviewer: look at these).")
    }
}

internal fun git(dir: File, vararg args: String): String {
    val pb = ProcessBuilder(listOf("git") + args).directory(dir)
    // A daemon started from a git hook inherits GIT_INDEX_FILE etc. and would read the wrong (temporary) index.
    pb.environment().keys.removeAll(setOf("GIT_INDEX_FILE", "GIT_DIR", "GIT_WORK_TREE", "GIT_PREFIX", "GIT_OBJECT_DIRECTORY"))
    val p = pb.start()
    val out = p.inputStream.bufferedReader().readText()
    val code = p.waitFor()
    if (code != 0) throw GradleException("git ${args.joinToString(" ")} failed ($code): ${p.errorStream.bufferedReader().readText()}")
    return out
}

internal object GateMessages {
    private val WHERE = """
        |Where this check lives:  build-logic/src/main/kotlin/com/hbs/buildlogic/gate/TestGatePlugin.kt
        |Config / thresholds:     test-gate.yml
        |Git hooks:               .githooks/pre-commit, .githooks/pre-push (installed by ./gradlew installGitHooks)
        |Waivers:                 test-waivers.yml
        |Full explanation:        docs/TEST_GATE.md
        |Re-run:                  ./gradlew testGate""".trimMargin()

    private val WAIVER_HELP = """
        |To skip a test you must add a waiver with a real reason, e.g. in test-waivers.yml:
        |  - path: feature/booking/src/main/kotlin/.../Foo.kt
        |    category: TRIVIAL_WRAPPER      # GENERATED | TRIVIAL_WRAPPER | UI_COVERED_ELSEWHERE | TIME_BOXED
        |    reason: "Thin delegate, no branching; covered via FooViewModelTest."   # >= 30 chars""".trimMargin()

    fun existenceFailure(missing: List<String>, problems: List<String>, base: String) = buildString {
        appendLine("TEST GATE FAILED (changes since '$base').")
        if (missing.isNotEmpty()) {
            appendLine("\n${missing.size} changed file(s) have no test (expected <Name>Test.kt in src/test) and no waiver:")
            missing.forEach { appendLine("  - $it") }
        }
        if (problems.isNotEmpty()) {
            appendLine("\nInvalid waivers:")
            problems.forEach { appendLine("  - $it") }
        }
        appendLine("\nOptions: 1) add the missing test   2) add a waiver explaining why not.\n")
        appendLine(WAIVER_HELP)
        appendLine()
        append(WHERE)
    }

    fun coverageFailure(cov: List<FileCoverage>, pct: Int, min: Int) = buildString {
        appendLine("TEST GATE FAILED: changed-line coverage is $pct%, required $min%.\n")
        cov.filter { it.total > 0 }.sortedBy { it.covered * 100 / it.total }.take(10).forEach {
            appendLine("  ${it.covered}/${it.total} lines  ${it.path}")
        }
        appendLine("\nAdd tests for the uncovered changed lines, or waive the file (see test-waivers.yml).\n")
        appendLine(WAIVER_HELP)
        appendLine()
        append(WHERE)
    }

    fun noBase(ref: String) = """
        |TEST GATE cannot find base ref '$ref'. The gate only judges code changed since that tag/branch.
        |If this is a fresh clone run: git fetch --tags   (the seed commit must be tagged '$ref').
        |Config: test-gate.yml (baseRef)   Docs: docs/TEST_GATE.md""".trimMargin()
}
