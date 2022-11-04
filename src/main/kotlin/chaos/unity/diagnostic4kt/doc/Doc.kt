package chaos.unity.diagnostic4kt.doc

import chaos.unity.diagnostic4kt.color.BgColor
import chaos.unity.diagnostic4kt.color.ColorApplier
import chaos.unity.diagnostic4kt.color.FgColor
import chaos.unity.diagnostic4kt.color.Modifier
import java.io.PrintStream

open class Doc private constructor(
    val content: String,
    val fgColor: FgColor? = null,
    val bgColor: BgColor? = null,
    val modifiers: List<Modifier> = listOf(),
    val aligned: Boolean = false
) {
    val width: Int =
        content.split("\n")
            .maxOfWith(Int::compareTo, String::length)

    fun print(stream: PrintStream, currentColumn: Int) {
        var content = content

        if (aligned) {
            content = content.replace("\n", "\n${" ".repeat(currentColumn - 1)}")
        }

        var nbNewLines = content.count { it == '\n' }
        val shouldAddNewLines = content != "\n"
        val splitContent = if (shouldAddNewLines) content.split('\n') else listOf("\n")

        for (line in splitContent) {
            var applier = ColorApplier(if (shouldAddNewLines && nbNewLines-- > 0) "${line}\n" else line)

            if (fgColor != null)
                applier = applier.apply(fgColor)

            if (bgColor != null)
                applier = applier.apply(bgColor)

            for (modifier in modifiers) {
                applier = applier.apply(modifier)
            }

            stream.print(applier.string)
        }
    }

    class Builder<T>(private val content: T) {
        companion object {
            fun colon(): Builder<String> =
                Builder(":")

            fun space(): Builder<String> =
                Builder(" ")

            fun line(): Builder<String> =
                Builder("\n")

            fun empty(): Builder<String> =
                Builder("")
        }

        private var fgColor: FgColor? = null
        private var bgColor: BgColor? = null
        private var modifiers: List<Modifier> = listOf()
        private var aligned: Boolean = false
        private val stringifyContent by lazy(content::toString)

        fun colors(fgColor: FgColor?, bgColor: BgColor?, vararg modifiers: Modifier?): Builder<T> {
            this.fgColor = fgColor
            this.bgColor = bgColor
            this.modifiers = modifiers.filterNotNull()
            return this
        }

        fun aligned(aligned: Boolean): Builder<T> {
            this.aligned = aligned
            return this
        }

        fun width(): Int =
            stringifyContent.length

        fun build(): Doc =
            Doc(stringifyContent, fgColor, bgColor, modifiers, aligned)
    }
}