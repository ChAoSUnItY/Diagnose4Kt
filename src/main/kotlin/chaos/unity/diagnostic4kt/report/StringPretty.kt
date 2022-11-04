package chaos.unity.diagnostic4kt.report

import chaos.unity.diagnostic4kt.doc.Doc
import chaos.unity.diagnostic4kt.doc.Document

@JvmInline value class StringPretty(private val string: String): Pretty<StringPretty> {
    override fun pretty(): Document.Builder =
        Document.Builder()
            .append(Doc.Builder(string))
}