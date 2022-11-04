package chaos.unity.qilai.report

import chaos.unity.qilai.doc.Doc
import chaos.unity.qilai.doc.Document
import java.io.PrintStream


class Diagnostic<Msg>(private val reports: MutableList<Report<Msg>> = mutableListOf()) where Msg : Pretty<Msg> {
    fun report(report: Report<Msg>): Diagnostic<Msg> {
        reports += report
        return this
    }

    fun clear(): Diagnostic<Msg> {
        reports.clear()
        return this
    }

    fun print(stream: PrintStream, withColors: Boolean) {
        val documentBuilder = Document.Builder()

        for (report in reports) {
            documentBuilder.append(report.pretty())
                .append(Doc.Builder.line())
        }

        if (!withColors) {
            documentBuilder.clear()
        }

        documentBuilder.build().print(stream)
    }
}