package chaos.unity.diagnostic4kt.color

enum class Modifier(override val beginInt: Int, override val endInt: Int) : AnsiCode {
    BOLD(1, 22), // 21 isn't widely supported and 22 does the same thing
    UNDERLINE(4, 24),
    INVERSE(7, 27);
}
