package chaos.unity.qilai

import chaos.unity.qilai.report.*
import chaos.unity.qilai.source.SourceCache
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class ExampleTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun init() {
            SourceCache.getSourceFromResource("/test.yk")
        }
    }

    private val diagnostic = Diagnostic<StringPretty>()

    @AfterEach
    fun tearDown() {
        diagnostic.print(System.out, CharSet.Unicode, true)
        diagnostic.clear()
    }

    @Test
    fun sourceExist() {
        assert(SourceCache.getSourceFromResource("/test.yk") != null)
    }

    @Test
    fun example() {
        diagnostic.add(
            Report(
                true,
                StringPretty("test1"),
                mapOf(
                    Position(1, 1, 1, 6, "/test.yk") to Marker.This(StringPretty("This is `class` keyword")),
                    Position(1, 7, 1, 12, "/test.yk") to Marker.Where(StringPretty("This is an identifier"))
                )
            )
        )
    }
}