package chaos.unity.qilai.source

import java.io.File

@JvmInline value class Source(val lines: List<String>) {
    companion object {
        fun fromFile(file: File): Source? =
            runCatching {
                file.useLines {
                    Source(it.toList())
                }
            }.getOrNull()

        fun fromResource(path: String): Source? =
            runCatching {
                this::class.java.getResourceAsStream(path)
                    ?.bufferedReader()
                    ?.readLines()
                    ?.let(::Source)
            }.getOrNull()

        fun fromString(string: String): Source =
            Source(listOf(string))

        fun fromStrings(string: Collection<String>): Source =
            Source(string.toList())
    }
}