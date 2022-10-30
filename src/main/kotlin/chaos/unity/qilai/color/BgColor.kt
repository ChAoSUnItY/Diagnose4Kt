package chaos.unity.qilai.color

enum class BgColor(override val beginInt: Int) : AnsiCode {
    BLACK(40),
    RED(41),
    GREEN(42),
    YELLOW(43),
    BLUE(44),
    MAGENTA(45),
    CYAN(46),
    WHITE(47);

    override val endInt: Int = 49
}