package chaos.unity.qilai.color

@JvmInline value class ColorApplier(private val text: String) {
    fun apply(code: AnsiCode): ColorApplier =
        ColorApplier("${code.getStart()}${text}${code.getEnd()}")
}