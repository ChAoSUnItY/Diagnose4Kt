package chaos.unity.qilai.doc

import chaos.unity.qilai.color.BgColor
import chaos.unity.qilai.color.ColorApplier
import chaos.unity.qilai.color.FgColor
import chaos.unity.qilai.color.Modifier
import java.io.PrintStream
import java.lang.StringBuilder

open class Doc private constructor(
    val content: String,
    val fgColor: FgColor? = null,
    val bgColor: BgColor? = null,
    val modifiers: Array<out Modifier> = arrayOf(),
    val aligned: Boolean = false
) {
    object Colon: Doc(":")
    object Space: Doc(" ")
    object Line: Doc("\n")
    object Empty: Doc("")

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

            stream.print(applier)
        }
    }

    class Builder<T>(private val content: T) {
        private var fgColor: FgColor? = null
        private var bgColor: BgColor? = null
        private var modifiers: Array<out Modifier> = arrayOf()
        private var aligned: Boolean = false

        fun colors(fgColor: FgColor?, bgColor: BgColor?, vararg modifiers: Modifier): Builder<T> {
            this.fgColor = fgColor
            this.bgColor = bgColor
            this.modifiers = modifiers
            return this
        }

        fun aligned(aligned: Boolean): Builder<T> {
            this.aligned = aligned
            return this
        }

        fun build(): Doc =
            Doc(content.toString(), fgColor, bgColor, modifiers, aligned)
    }
}