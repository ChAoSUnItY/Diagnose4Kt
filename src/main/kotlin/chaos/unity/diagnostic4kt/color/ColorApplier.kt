package chaos.unity.diagnostic4kt.color

@JvmInline value class ColorApplier(val string: String) {
    fun apply(code: AnsiCode): ColorApplier =
        ColorApplier("${code.getStart()}${string}${code.getEnd()}")
}