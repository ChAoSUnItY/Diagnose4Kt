package chaos.unity.qilai.color

/**
 * The amount of codes required in order to jump from a foreground code to a background code. Equal to 10. For example,
 * the foreground code for blue is "[33m", its respective background code is "[43m"
 */
private const val BG_JUMP = 10

/**
 * An enumeration of colors supported by most terminals. Can be applied to both foreground and background.
 */
enum class FgColor(override val beginInt: Int) : AnsiCode {
    BLACK(30),
    RED(31),
    GREEN(32),
    YELLOW(33),
    BLUE(34),
    MAGENTA(35),
    CYAN(36),
    WHITE(37),
    GRAY(90),
    GREY(90);

    override val endInt: Int = 39
}
