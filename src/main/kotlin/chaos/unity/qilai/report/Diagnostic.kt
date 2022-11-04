package chaos.unity.qilai.report

import chaos.unity.qilai.doc.Doc
import chaos.unity.qilai.doc.Document
import java.io.PrintStream
import java.util.*


class Diagnostic<Msg>(reports: List<Report<Msg>> = listOf()) : LinkedList<Report<Msg>>(reports)
        where Msg : Pretty<Msg> {
    fun print(stream: PrintStream, charSet: CharSet, withColors: Boolean) {
        val documentBuilder = Document.Builder()

        for (report in this) {
            documentBuilder.append(report.pretty(charSet))
                .append(Doc.Builder.line())
        }

        if (!withColors) {
            documentBuilder.clear()
        }

        documentBuilder.build().print(stream)
    }
}