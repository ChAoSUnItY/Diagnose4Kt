package chaos.unity.qilai.report

import chaos.unity.qilai.doc.Doc
import chaos.unity.qilai.doc.Document

@JvmInline value class StringPretty(private val string: String): Pretty<StringPretty> {
    override fun pretty(): Document.Builder =
        Document.Builder()
            .append(Doc.Builder(string))
}