package chaos.unity.diagnostic4kt

import chaos.unity.diagnostic4kt.report.*
import chaos.unity.diagnostic4kt.source.SourceCache
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
        println("--- With Unicode ---")
        diagnostic.print(System.out, CharSet.Unicode, true)
        println("--- With ASCII ---")
        diagnostic.print(System.out, CharSet.ASCII, true)
        println("--- Without colors ---")
        diagnostic.print(System.out, CharSet.Unicode, false)

        diagnostic.clear()
    }

    @Test
    fun noHintsAndNoMarkers() {
        diagnostic.add(Report(true, StringPretty("Error with no marker and no hints"), mapOf()))
    }

    @Test
    fun singleMarkerNoHints() {
        diagnostic.add(
            Report(
                true,
                StringPretty("Error with one marker and no hints"),
                mapOf(
                    Position(1, 25, 1, 30, "test.zc") to Marker.This(StringPretty("Required here"))
                )
            )

        )
    }

    @Test
    fun simpleDiagnostic() {
        diagnostic.add(
            Report(
                true,
                StringPretty("Could not deduce constraint 'Num(a)' from the current context"),
                mapOf(
                    Position(1, 25, 1, 30, "test.zc") to
                            Marker.This(StringPretty("While applying function '+'")),
                    Position(1, 11, 1, 16, "test.zc") to
                            Marker.Where(StringPretty("'x' is supposed to have type 'a'")),
                    Position(1, 8, 1, 9, "test.zc") to
                            Marker.Where(StringPretty("type 'a' is bound here without constraints"))
                ),
                listOf(StringPretty("Adding 'Num(a)' to the list of constraints may solve this problem."))
            )
        )
    }

    @Test
    fun multilineMessages() {
        diagnostic.add(
            Report(
                true,
                StringPretty("Could not deduce constraint 'Num(a)'\nfrom the current context"),
                mapOf(
                    Position(1, 25, 1, 30, "test.zc") to
                            Marker.This(StringPretty("While applying function '+'")),
                    Position(1, 11, 1, 16, "test.zc") to
                            Marker.Where(StringPretty("'x' is supposed to have type 'a'")),
                    Position(1, 8, 1, 9, "test.zc") to
                            Marker.Where(StringPretty("type 'a' is bound here without constraints"))
                ), listOf(StringPretty("Adding 'Num(a)' to the list of\nconstraints may solve this problem."))
            )
        )
    }

    @Test
    fun multipleFiles() {
        diagnostic.add(
            Report(
                true,
                StringPretty("Error on multiple files"),
                mapOf(
                    Position(1, 5, 1, 7, "test.zc") to Marker.Where(StringPretty("Function already defined here")),
                    Position(
                        1,
                        5,
                        1,
                        7,
                        "somefile.zc"
                    ) to Marker.This(StringPretty("Function `id` already declared in another module"))
                )
            )
        )
    }

    @Test
    fun noMarkerButSomeHints() {
        diagnostic.add(
            Report(
                false,
                StringPretty("Error with no markers but some hints"),
                mapOf(),
                listOf(
                    StringPretty("My first hint on resolving this issue"),
                    StringPretty("And a second one because I'm feeling nice today :)")
                )
            )
        )
    }

    @Test
    fun testCrossing() {
        diagnostic.add(
            Report(
                false,
                StringPretty("Ordered labels with crossing"),
                mapOf(
                    Position(1, 1, 1, 7, "somefile.zc") to Marker.This(StringPretty("leftmost label")),
                    Position(1, 9, 1, 16, "somefile.zc") to Marker.Where(StringPretty("rightmost label"))
                )
            )
        )
    }
}