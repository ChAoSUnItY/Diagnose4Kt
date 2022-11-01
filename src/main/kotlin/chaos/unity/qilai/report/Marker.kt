package chaos.unity.qilai.report

import chaos.unity.qilai.color.FgColor

sealed class Marker<Msg> : Comparable<Marker<Msg>> where Msg : Pretty<Msg> {
    abstract val msg: Msg
    abstract fun markerColor(isError: Boolean): FgColor

    data class This<Msg>(override val msg: Msg) : Marker<Msg>() where Msg : Pretty<Msg> {
        override fun markerColor(isError: Boolean): FgColor =
            if (isError) FgColor.RED
            else FgColor.YELLOW

        override fun compareTo(other: Marker<Msg>): Int =
            if (other is This) 0
            else 1
    }

    data class Where<Msg>(override val msg: Msg) : Marker<Msg>() where Msg : Pretty<Msg> {
        override fun markerColor(isError: Boolean): FgColor =
            FgColor.BLUE

        override fun equals(other: Any?): Boolean =
            other is Where<*> && super.equals(other)

        override fun compareTo(other: Marker<Msg>): Int =
            when (other) {
                is Where -> 0
                is This -> -1
                else -> 1
            }
    }

    data class Maybe<Msg>(override val msg: Msg) : Marker<Msg>() where Msg : Pretty<Msg> {
        override fun markerColor(isError: Boolean): FgColor =
            FgColor.MAGENTA

        override fun equals(other: Any?): Boolean =
            other is Maybe<*> && super.equals(other)

        override fun compareTo(other: Marker<Msg>): Int =
            when (other) {
                is Maybe -> 0
                else -> -1
            }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Marker<*>
        return msg == other.msg
    }

    override fun hashCode(): Int {
        return msg.hashCode()
    }
}
