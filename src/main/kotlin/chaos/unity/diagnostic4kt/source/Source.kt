package chaos.unity.diagnostic4kt.source

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
            this::class.java.getResource(path)?.file?.let { fromFile(File(it)) }

        fun fromString(string: String): Source =
            Source(string.split('\n'))

        fun fromStrings(string: Collection<String>): Source =
            Source(string.toList())
    }
}