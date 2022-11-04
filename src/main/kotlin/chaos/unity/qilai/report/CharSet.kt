package chaos.unity.qilai.report

sealed class CharSet {
    abstract val pipe: Char
    abstract val pipeWithRightLine: Char
    abstract val pipeWithLeftLine: Char
    abstract val dot: Char
    abstract val line: Char
    abstract val lineWithLowerPipe: Char
    abstract val upperRightPipe: Char
    abstract val lowerRightPipe: Char
    abstract val lowerLeftPipe: Char
    abstract val point: Char

    abstract val upperRightArrow: String
    abstract val centerRightArrow: String

    object Unicode: CharSet() {
        override val pipe: Char = '│'
        override val pipeWithRightLine: Char = '├'
        override val pipeWithLeftLine: Char = '┤'
        override val dot: Char = '•'
        override val line: Char = '─'
        override val lineWithLowerPipe: Char = '┬'
        override val upperRightPipe: Char = '╭'
        override val lowerRightPipe: Char = '╰'
        override val lowerLeftPipe: Char = '╯'
        override val point: Char = '╸'

        override val upperRightArrow: String = "╭──▶"
        override val centerRightArrow: String = "┼──▶"
    }

    object ASCII: CharSet() {
        override val pipe: Char = '|'
        override val pipeWithRightLine: Char = '|'
        override val pipeWithLeftLine: Char = '>'
        override val dot: Char = ':'
        override val line: Char = '-'
        override val lineWithLowerPipe: Char = '^'
        override val upperRightPipe: Char = '+'
        override val lowerRightPipe: Char = '`'
        override val lowerLeftPipe: Char = '+'
        override val point: Char = '-'

        override val upperRightArrow: String = "+-->"
        override val centerRightArrow: String = "+-->"
    }
}
