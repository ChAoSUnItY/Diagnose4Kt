package chaos.unity.qilai

import chaos.unity.qilai.report.*
import chaos.unity.qilai.source.SourceCache
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


class RenderTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun init() {
            SourceCache.getSourceFromFileContent(
                "test.zc",
                "let id<a>(x : a) : a := x + 1\nrec fix(f) := f(fix(f))\nlet const<a, b>(x : a, y : b) : a := x"
            )
            SourceCache.getSourceFromFileContent(
                "somefile.zc",
                "let id<a>(x : a) : a := x + 1\nrec fix(f) := f(fix(f))\nlet const<a, b>(x : a, y : b) : a := x"
            )
            SourceCache.getSourceFromFileContent(
                "err.nst",
                "\n\n\n\n    = jmp g\n\n    g: forall(s: Ts, e: Tc).{ %r0: *s64 | s -> e }"
            )
            SourceCache.getSourceFromFileContent(
                "unsized.nst",
                "main: forall(a: Ta, s: Ts, e: Tc).{ %r5: forall().{| s -> e } | s -> %r5 }\n    = salloc a\n    ; sfree\\n"
            )
        }
    }

    private val diagnostic: Diagnostic<StringPretty> = Diagnostic()

    @BeforeEach
    fun setup() {
        println("-------------------------------------------------------")
    }

    @AfterEach
    fun tearDown() {
        println("--- With Unicode ---");
        diagnostic.print(System.out, CharSet.Unicode, true);
        println("--- With ASCII ---");
        diagnostic.print(System.out, CharSet.ASCII, true);
        println("--- Without colors ---");
        diagnostic.print(System.out, CharSet.Unicode, false);

        diagnostic.clear()
    }

    @Test
    fun noHintsAndNoMarkers() {
        diagnostic.report(Report(true, StringPretty("Error with no marker and no hints"), mapOf()))
    }

    @Test
    fun singleMarkerNoHints() {
        diagnostic.report(
            Report(
                true,
                StringPretty("Error with one marker and no hints"),
                mapOf(
                    Position(1, 25, 1, 30, "test.zc") to Marker.This(StringPretty("Required here"))
                )
            )

        )
    }
}