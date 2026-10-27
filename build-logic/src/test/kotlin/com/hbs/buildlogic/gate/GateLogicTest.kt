package com.hbs.buildlogic.gate

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GateLogicTest {
    private val cfg = GateConfig("baseline", 80, listOf("**/di/**", "**/*Screen.kt"))

    @Test fun `gates only production kotlin that is not excluded`() {
        assertTrue(GateLogic.isGated("feature/a/src/main/kotlin/x/Foo.kt", cfg))
        assertFalse(GateLogic.isGated("feature/a/src/test/kotlin/x/FooTest.kt", cfg))
        assertFalse(GateLogic.isGated("feature/a/src/main/kotlin/x/di/FooModule.kt", cfg))
        assertFalse(GateLogic.isGated("feature/a/src/main/kotlin/x/HomeScreen.kt", cfg))
    }

    @Test fun `sibling test is found only inside the same module`() {
        val files = listOf("a/src/test/kotlin/FooTest.kt", "b/src/test/kotlin/BarTest.kt")
        assertTrue(GateLogic.hasTest("a/src/main/kotlin/Foo.kt", files))
        assertFalse(GateLogic.hasTest("b/src/main/kotlin/Foo.kt", files))
    }

    @Test fun `waiver needs real reason, known category and existing file`() {
        val ok = Waiver("a.kt", "TIME_BOXED", "Ran out of interview time, covered manually via emulator.")
        assertTrue(GateLogic.waiverProblems(listOf(ok)) { true }.isEmpty())
        assertEquals(1, GateLogic.waiverProblems(listOf(ok.copy(reason = "todo"))) { true }.size)
        assertEquals(1, GateLogic.waiverProblems(listOf(ok.copy(category = "LAZY"))) { true }.size)
        assertEquals(1, GateLogic.waiverProblems(listOf(ok)) { false }.size)
    }

    @Test fun `diff parser reads new-side line ranges`() {
        val diff = "+++ b/a/Foo.kt\n@@ -1,0 +5,2 @@\n+x\n+y\n@@ -9 +20 @@\n+z\n+++ /dev/null\n@@ -1 +1 @@\n"
        assertEquals(mapOf("a/Foo.kt" to setOf(5, 6, 20)), GateLogic.parseChangedLines(diff))
    }

    @Test fun `coverage counts only executable changed lines`() {
        val reports = mapOf("m" to mapOf("p/Foo.kt" to mapOf(5 to true, 6 to false)))
        val cov = GateLogic.measure(mapOf("m/src/main/kotlin/p/Foo.kt" to setOf(1, 5, 6)), listOf("m/src/main/kotlin/p/Foo.kt"), reports)
        assertEquals(50, GateLogic.percent(cov))
    }

    @Test fun `config falls back to defaults`() {
        assertEquals(GateConfig("baseline", 80, emptyList()), GateLogic.parseConfig(""))
    }
}
