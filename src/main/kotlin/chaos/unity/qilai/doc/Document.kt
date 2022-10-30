package chaos.unity.qilai.doc

import chaos.unity.qilai.color.BgColor
import chaos.unity.qilai.color.FgColor
import chaos.unity.qilai.color.Modifier
import java.io.PrintStream

class Document private constructor(private val docs: List<Doc>) {
    fun print(stream: PrintStream) {
        var currentColumn = 1

        for (doc in docs) {
            doc.print(stream, currentColumn)

            val lines = doc.content.split('\n')
            when (lines.size) {
                0 -> {
                    currentColumn = 0
                }
                1 -> {
                    currentColumn += lines[0].length
                }
                else -> {
                    currentColumn = lines[lines.size - 1].length + 1
                }
            }
        }
    }

    class Builder<T> {
        private val docs: MutableList<Doc.Builder<T>> = mutableListOf()

        fun append(docBuilder: Doc.Builder<T>): Builder<T> {
            docs += docBuilder
            return this
        }

        fun append(documentBuilder: Builder<T>): Builder<T> {
            docs += documentBuilder.docs
            return this
        }

        fun color(fgColor: FgColor, bgColor: BgColor, vararg modifiers: Modifier): Builder<T> {
            for (part in docs) {
                part.colors(fgColor, bgColor, *modifiers)
            }
            return this
        }

        fun clear(): Builder<T> {
            for (part in docs) {
                part.colors(null, null)
            }
            return this
        }

        fun build(): Document {
            return Document(docs.map(Doc.Builder<T>::build))
        }
    }
}