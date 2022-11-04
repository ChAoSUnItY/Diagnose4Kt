package chaos.unity.diagnostic4kt.source

import java.io.File

object SourceCache {
    private val sources: MutableMap<String, Source> = mutableMapOf()

    fun getSourceFromFileContent(filePath: String, content: String): Source {
        if (sources.containsKey(filePath)) {
            return sources[filePath]!!
        }

        val source = Source.fromString(content)
        sources[filePath] = source

        return source
    }

    fun getSourceFromFilePath(filePath: String): Source? =
        sources[filePath]

    fun getSourceFromFile(file: File): Source? {
        if (sources.containsKey(file.path)) {
            return sources[file.path]
        }

        val source = Source.fromFile(file)

        if (source != null) {
            sources[file.path] = source
        }

        return source
    }

    fun getSourceFromResource(path: String): Source? {
        if (sources.containsKey(path)) {
            return sources[path]
        }

        val source = Source.fromResource(path)

        if (source != null) {
            sources[path] = source
        }

        return source
    }
}