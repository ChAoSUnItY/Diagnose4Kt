package chaos.unity.qilai.report

data class Position(
    val startLine: Int,
    val startColumn: Int,
    val endLine: Int,
    val endColumn: Int,
    val fileName: String
) : Comparable<Position> {
    companion object {
        val DEFAULT = Position(1, 1, 1 ,1, "<no-file>")
    }

    override fun toString(): String =
        "${fileName}@${startLine}:${startColumn}-${endLine}:${endColumn}"

    override fun compareTo(other: Position): Int {
        if (this.startLine < other.startLine) {
            return -1
        }
        if (this.startLine == other.startLine && this.startColumn < other.startColumn) {
            return -1
        }
        if (this.endLine < other.endLine) {
            return -1
        }
        if (this.endLine == other.endLine && this.endColumn < other.endColumn) {
            return -1
        }
        if (this.startLine == other.startLine && this.startColumn == other.startColumn
            && this.endLine == other.endLine && this.endColumn == other.endColumn
        ) {
            return 0
        }

        return 1
    }
}
