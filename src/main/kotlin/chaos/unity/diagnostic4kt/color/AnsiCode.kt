package chaos.unity.diagnostic4kt.color

interface AnsiCode {
    companion object {
        const val ESCAPE = '\u001B'
        const val RESET = "$ESCAPE[0m"
    }

    val beginInt: Int
    val endInt: Int

    fun getStart(): String =
        "\u001b[${beginInt}m"

    fun getEnd(): String =
        "\u001b[${endInt}m"
}