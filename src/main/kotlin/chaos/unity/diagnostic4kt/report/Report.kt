package chaos.unity.diagnostic4kt.report

import chaos.unity.diagnostic4kt.color.FgColor
import chaos.unity.diagnostic4kt.color.Modifier
import chaos.unity.diagnostic4kt.doc.Doc
import chaos.unity.diagnostic4kt.doc.Document
import chaos.unity.diagnostic4kt.identity
import chaos.unity.diagnostic4kt.source.SourceCache
import java.util.*
import kotlin.math.max

class Report<Msg>(
    private val isError: Boolean,
    private val msg: Msg,
    private val markers: Map<Position, Marker<Msg>>,
    private val hints: List<Msg> = listOf()
) where Msg : Pretty<Msg> {
    companion object {
        private fun pad(
            max: Int,
            padding: Char,
            docBuilder: Doc.Builder<String>,
            paddingColors: (Doc.Builder<String>) -> Doc.Builder<String>
        ): Document.Builder =
            Document.Builder()
                .append(docBuilder)
                .append(paddingColors(Doc.Builder(padding.toString().repeat(max - docBuilder.width()))))
    }

    fun pretty(charSet: CharSet): Document.Builder {
        val docBuilder = Document.Builder()
        val sortedMarkers = markers.entries
            .sortedBy { it.key.startLine }
        val maxLineNumberLength = (if (sortedMarkers.isEmpty()) null else sortedMarkers.last())?.key
            ?.endLine
            ?.toString()
            ?.length
            ?.let {
                max(3, it)
            } ?: 3
        val groupedMarkers = groupMarkersPerFile(sortedMarkers)
        val header = Doc.Builder(if (isError) "[error]" else "[warning]")
            .colors(if (isError) FgColor.RED else FgColor.YELLOW, null, Modifier.BOLD)

        docBuilder.append(header)
            .append(Doc.Builder.colon())
            .append(Doc.Builder.space())
            .append(msg.pretty().align())

        for ((isFirst, markers) in groupedMarkers) {
            docBuilder.append(prettyAllSubReports(charSet, isError, maxLineNumberLength, isFirst, markers))
        }

        if (hints.isNotEmpty()) {
            docBuilder.append(Doc.Builder.line())
                .append(Doc.Builder.space())
                .append(dotPrefix(maxLineNumberLength, charSet))
        }

        docBuilder.append(prettyAllHints(hints, maxLineNumberLength, charSet))
            .append(Doc.Builder.line())

        if (!(markers.isEmpty() && hints.isEmpty())) {
            docBuilder.append(pad(maxLineNumberLength + 2, charSet.line, Doc.Builder.empty()) {
                it.colors(
                    FgColor.GRAY,
                    null,
                    Modifier.BOLD
                )
            }).append(Doc.Builder(charSet.lowerLeftPipe).colors(FgColor.GRAY, null, Modifier.BOLD))
                .append(Doc.Builder.line())
        }

        return docBuilder
    }

    private fun groupMarkersPerFile(
        sortedMarkers: List<Map.Entry<Position, Marker<Msg>>>
    ): List<Pair<Boolean, List<Map.Entry<Position, Marker<Msg>>>>> {
        val markersPerFile = linkedMapOf<String, List<Map.Entry<Position, Marker<Msg>>>>()

        for (entry in sortedMarkers) {
            val pos = entry.key
            val markers =
                if (markersPerFile.containsKey(pos.fileName)) markersPerFile[pos.fileName]!!.toMutableList()
                else mutableListOf()

            markers += entry
            markersPerFile += pos.fileName to markers
        }

        return markersPerFile.values
            .sortedWith { o1, o2 ->
                if (o1.any { it.value is Marker.This<*> }) 1
                else if (o2.any { it.value is Marker.This<*> }) -1
                else 0
            }.mapIndexed { i, entries ->
                ((i == 0) to entries)
            }
    }

    private fun prettyAllSubReports(
        charSet: CharSet,
        isError: Boolean,
        maxLineNumberLength: Int,
        isFirst: Boolean,
        markers: List<Map.Entry<Position, Marker<Msg>>>
    ): Document.Builder {
        val inlineMarkers = hashMapOf<Int, MutableList<Map.Entry<Position, Marker<Msg>>>>()
        val multilineMarkers = mutableListOf<Map.Entry<Position, Marker<Msg>>>()
        splitInlineMarkers(markers, inlineMarkers, multilineMarkers)

        val sortedMarkerPerLine = inlineMarkers.entries
            .sortedBy { it.key }
        val reportFile = markers
            .sortedBy { it.value }
            .map(Map.Entry<Position, Marker<Msg>>::key)
            .firstOrNull() ?: Position.DEFAULT
        val allLineNumbersInReport = sortedMarkerPerLine.map { it.key } +
                multilineMarkers.flatMap { it.key.startLine..it.key.endLine }
        val fileMarker = Document.Builder()

        if (isFirst) {
            fileMarker.append(Doc.Builder.space())
                .append(pad(maxLineNumberLength, ' ', Doc.Builder.empty(), ::identity))
                .append(Doc.Builder.space())
                .append(Doc.Builder(charSet.upperRightArrow).colors(FgColor.GRAY, null, Modifier.BOLD))
                .append(Doc.Builder.space())
                .append(Doc.Builder(reportFile).colors(FgColor.GREEN, null, Modifier.BOLD))
        } else {
            fileMarker.append(Doc.Builder.space())
                .append(dotPrefix(maxLineNumberLength, charSet))
                .append(Doc.Builder.line())
                .append(pad(maxLineNumberLength + 2, charSet.line, Doc.Builder.empty()) {
                    it.colors(
                        FgColor.GRAY,
                        null
                    )
                })
                .append(Doc.Builder(charSet.centerRightArrow).colors(FgColor.GRAY, null))
                .append(Doc.Builder.space())
                .append(Doc.Builder(reportFile).colors(FgColor.GREEN, null, Modifier.BOLD))
                .append(
                    prettyAllLines(
                        charSet,
                        isError,
                        maxLineNumberLength,
                        sortedMarkerPerLine,
                        multilineMarkers,
                        allLineNumbersInReport
                    )
                )
        }

        return Document.Builder()
            .append(Doc.Builder.line())
            .append(fileMarker)
            .append(Doc.Builder.line())
            .append(Doc.Builder.space())
            .append(pipePrefix(maxLineNumberLength, charSet))
            .append(
                prettyAllLines(
                    charSet,
                    isError,
                    maxLineNumberLength,
                    sortedMarkerPerLine,
                    multilineMarkers,
                    allLineNumbersInReport
                )
            )
    }

    private fun prettyAllLines(
        charSet: CharSet,
        isError: Boolean,
        maxLineNumberLength: Int,
        inlineMarkers: List<Map.Entry<Int, List<Map.Entry<Position, Marker<Msg>>>>>,
        multilineMarkers: MutableList<Map.Entry<Position, Marker<Msg>>>,
        allLineNumbersInList: List<Int>
    ): Document.Builder {
        val doc = allLineNumbersInList.map {
            val allInlineMarkersInLine = inlineMarkers.filter { (line, _) -> line == it }
                .flatMap(Map.Entry<Int, List<Map.Entry<Position, Marker<Msg>>>>::value)
            val allMultilineMarkersInLine = multilineMarkers.filter { (pos, _) ->
                pos.startLine == it || pos.endLine == it
            }
            val allMultilineMarkersSpanningLine = multilineMarkers.filter { (pos, _) ->
                pos.startLine < it && pos.endLine > it
            }
            val inSpanOfMultiLine = multilineMarkers.any { (pos, _) ->
                pos.startLine <= it && pos.endLine >= it
            }
            val firstMultilineMarkerColor = (allMultilineMarkersInLine + allMultilineMarkersSpanningLine)
                .firstOrNull()
                ?.value?.markerColor(isError)
            val additionalPrefix = Document.Builder()

            if (allMultilineMarkersInLine.isEmpty()) {
                if (multilineMarkers.isNotEmpty()) {
                    if (allMultilineMarkersSpanningLine.isNotEmpty()) {
                        additionalPrefix.append(
                            Doc.Builder("${charSet.pipe}  ").colors(firstMultilineMarkerColor, null)
                        )
                    } else {
                        additionalPrefix.append(Doc.Builder("   "))
                    }
                }
            } else {
                val (pos, marker) = allMultilineMarkersInLine.first()
                val hasPredecessor =
                    pos.endLine == it || multilineMarkers.firstOrNull()?.key?.let { p -> p != pos } ?: false
                val jointMarker =
                    if (hasPredecessor) charSet.pipeWithRightLine
                    else charSet.upperRightPipe

                additionalPrefix.append(Doc.Builder(jointMarker).colors(firstMultilineMarkerColor, null))
                    .append(Doc.Builder(charSet.pipeWithLeftLine).colors(marker.markerColor(isError), null))
                    .append(Doc.Builder.space())
            }

            val allMarkersInLine = allInlineMarkersInLine + allMultilineMarkersInLine + allMultilineMarkersSpanningLine

            Document.Builder()
                .append(Doc.Builder.line())
                .append(linePrefix(maxLineNumberLength, it, charSet))
                .append(Doc.Builder.space())
                .append(additionalPrefix)
                .append(getLine(allMarkersInLine, it, isError))
                .append(
                    showAllMarkersInLine(
                        multilineMarkers.isNotEmpty(),
                        inSpanOfMultiLine,
                        firstMultilineMarkerColor,
                        charSet,
                        isError,
                        maxLineNumberLength,
                        allInlineMarkersInLine
                    )
                )
        }.fold(Document.Builder(), Document.Builder::append)

        if (multilineMarkers.isNotEmpty()) {
            val lastMultilineMarkerColor = multilineMarkers.last().value.markerColor(isError)
            val prefix = Document.Builder()
                .append(Doc.Builder.line())
                .append(Doc.Builder.space())
                .append(dotPrefix(maxLineNumberLength, charSet))
                .append(Doc.Builder.space())
            val prefixWithBar = Document.Builder()
                .append(prefix)
                .append(Doc.Builder("${charSet.pipe} ").colors(lastMultilineMarkerColor, null))

            doc.append(prefixWithBar)
                .append(prefix)

            for (i in multilineMarkers.lastIndex downTo 1) {
                doc.append(constructMultilineMarkerMessage(multilineMarkers[i], charSet, isError, false))
                    .append(prefix)
            }

            if (multilineMarkers.isNotEmpty()) {
                doc.append(constructMultilineMarkerMessage(multilineMarkers.first(), charSet, isError, true))
            }
        }

        return doc
    }

    private fun getLine(
        allMarkersInline: List<Map.Entry<Position, Marker<Msg>>>,
        line: Int,
        isError: Boolean
    ): Document.Builder =
        allMarkersInline.firstOrNull()
            ?.let { (pos, _) ->
                SourceCache.getSourceFromFilePath(pos.fileName)
            }?.let {
                if (it.lines.size >= line - 1) it.lines[line - 1]
                else null
            }?.let {
                val doc = Document.Builder()

                for (i in 1..it.length) {
                    val color = allMarkersInline.firstOrNull { (pos, _) ->
                        if (pos.startLine == pos.endLine) i >= pos.startColumn && i < pos.endColumn
                        else {
                            (pos.startLine == line && i >= pos.startColumn)
                                    || (pos.endLine == line && i < pos.endColumn)
                        }
                    }?.let(Map.Entry<Position, Marker<Msg>>::value)?.markerColor(isError)

                    doc.append(Doc.Builder(it[i - 1]).colors(color, null, if (color != null) Modifier.BOLD else null))
                }

                doc
            } ?: Document.Builder()
            .append(Doc.Builder("<no line>").colors(FgColor.MAGENTA, null))

    private fun showAllMarkersInLine(
        hasMultilines: Boolean,
        inSpanOfMultiline: Boolean,
        firstMultilineMarkerColor: FgColor?,
        charSet: CharSet,
        isError: Boolean,
        maxLineNumberLength: Int,
        allInlineMarkersInLine: List<Map.Entry<Position, Marker<Msg>>>
    ): Document.Builder {
        val documentBuilder = Document.Builder()

        if (allInlineMarkersInLine.isNotEmpty()) {
            val markers = allInlineMarkersInLine.sortedBy { it.key.endColumn }
            val maxMarkerColumn = markers.last().key.endColumn
            val specialPrefix =
                if (inSpanOfMultiline) Document.Builder()
                    .append(Doc.Builder("${charSet.pipe} ").colors(firstMultilineMarkerColor, null))
                    .append(Doc.Builder.space())
                else if (hasMultilines) Document.Builder()
                    .append(Doc.Builder("  ").colors(firstMultilineMarkerColor, null))
                    .append(Doc.Builder.space())
                else Document.Builder()

            documentBuilder.append(Doc.Builder.line())
                .append(Doc.Builder.space())
                .append(dotPrefix(maxLineNumberLength, charSet))
                .append(Doc.Builder.space())
                .append(specialPrefix)

            for (n in 1..maxMarkerColumn) {
                val allMarkers = allInlineMarkersInLine.filter { n >= it.key.startColumn && n < it.key.endColumn }

                if (allMarkers.isEmpty()) {
                    documentBuilder.append(Doc.Builder.space())
                } else {
                    val (pos, marker) = allMarkers.first()

                    if (pos.startColumn == n) {
                        documentBuilder
                            .append(Doc.Builder(charSet.lineWithLowerPipe).colors(marker.markerColor(isError), null))
                    } else {
                        documentBuilder
                            .append(Doc.Builder(charSet.line).colors(marker.markerColor(isError), null))
                    }
                }
            }

            val ms = LinkedList(allInlineMarkersInLine)
            while (ms.isNotEmpty()) {
                val (pos, marker) = ms.removeFirst()
                val filteredPipes =
                    ms.filter { it.key.startLine != pos.startLine || it.key.startColumn != pos.startColumn }
                val filteredAndNubbedPipes = filteredPipes.filter(MarkerDistincter())
                val hasSuccessor = filteredPipes.size != ms.size
                val pipesBefore = mutableListOf<Map.Entry<Position, Marker<Msg>>>()
                val pipesAfter = mutableListOf<Map.Entry<Position, Marker<Msg>>>()

                for (pipe in filteredAndNubbedPipes) {
                    if (pipe.key.startColumn < pos.startColumn) pipesBefore += pipe
                    else pipesAfter += pipe
                }

                val pipesBeforePreRender = pipesBefore.map { (pos, marker) ->
                    pos to (Doc.Builder(charSet.pipe).colors(marker.markerColor(isError), null))
                }.toMutableList()
                val lastStartPos =
                    if (pipesAfter.isEmpty()) 0
                    else pipesAfter.first().key.startColumn - pos.startColumn
                val currentPipe =
                    if (hasSuccessor) charSet.pipeWithRightLine
                    else charSet.lowerRightPipe
                val prefix = Document.Builder()
                    .append(
                        processLineStart(
                            pos,
                            specialPrefix,
                            dotPrefix(maxLineNumberLength, charSet),
                            pipesBeforePreRender
                        )
                    )
                    .append(Doc.Builder(currentPipe).colors(marker.markerColor(isError), null))
                    .append(
                        Doc.Builder(charSet.line.toString().repeat(lastStartPos))
                            .colors(marker.markerColor(isError), null)
                    )
                    .append(Doc.Builder(charSet.point).colors(marker.markerColor(isError), null))
                    .append(Doc.Builder.space())
                    .append(marker.msg.pretty().color(marker.markerColor(isError), null).align())

                documentBuilder.append(Doc.Builder.line())
                    .append(Doc.Builder.space())
                    .append(prefix)
            }
        }

        return documentBuilder
    }

    private fun prettyAllHints(hints: List<Msg>, maxLineNumberLength: Int, charSet: CharSet): Document.Builder {
        val documentBuilder = Document.Builder()

        if (hints.isNotEmpty()) {
            val prefix = Document.Builder()
                .append(Doc.Builder.line())
                .append(Doc.Builder.space())
                .append(pipePrefix(maxLineNumberLength, charSet))

            for (hint in hints) {
                documentBuilder.append(prefix)
                    .append(Doc.Builder("Hint:").colors(FgColor.CYAN, null, Modifier.BOLD))
                    .append(Doc.Builder.space())
                    .append(hint.pretty().color(FgColor.CYAN, null).align())
            }
        }

        return documentBuilder
    }

    private fun splitInlineMarkers(
        markers: List<Map.Entry<Position, Marker<Msg>>>,
        inlineMarkers: MutableMap<Int, MutableList<Map.Entry<Position, Marker<Msg>>>>,
        multilineMarkers: MutableList<Map.Entry<Position, Marker<Msg>>>
    ) {
        for (marker in markers) {
            val pos = marker.key

            if (pos.startLine != pos.endLine) {
                multilineMarkers.add(0, marker)
            } else {
                val list =
                    if (inlineMarkers.containsKey(pos.startLine)) inlineMarkers[pos.startLine]!!
                    else mutableListOf()

                list += marker
                inlineMarkers[pos.startLine] = list
            }
        }
    }

    private fun pipePrefix(max: Int, charSet: CharSet): Document.Builder =
        Document.Builder()
            .append(pad(max, ' ', Doc.Builder.empty(), ::identity))
            .append(Doc.Builder.space())
            .append(Doc.Builder(charSet.pipe).colors(FgColor.GRAY, null, Modifier.BOLD))

    private fun dotPrefix(max: Int, charSet: CharSet): Document.Builder =
        Document.Builder()
            .append(pad(max, ' ', Doc.Builder.empty(), ::identity))
            .append(Doc.Builder.space())
            .append(Doc.Builder(charSet.dot).colors(FgColor.GRAY, null, Modifier.BOLD))

    private fun linePrefix(maxLineNumberLength: Int, line: Int, charSet: CharSet): Document.Builder =
        Document.Builder()
            .append(
                pad(
                    maxLineNumberLength - line.toString().length,
                    ' ',
                    Doc.Builder.empty()
                ) { it.colors(FgColor.GRAY, null) }
            ).append(Doc.Builder.space().colors(FgColor.GRAY, null))
            .append(Doc.Builder(line).colors(FgColor.GRAY, null))
            .append(Doc.Builder.space().colors(FgColor.GRAY, null))
            .append(Doc.Builder(charSet.pipe).colors(FgColor.GRAY, null))

    private fun constructMultilineMarkerMessage(
        entry: Map.Entry<Position, Marker<Msg>>,
        charSet: CharSet,
        isError: Boolean,
        isLast: Boolean
    ): Document.Builder =
        Document.Builder()
            .append(
                Doc.Builder(if (isLast) "${charSet.lowerRightPipe}${charSet.line} " else "${charSet.pipeWithRightLine}${charSet.line} ")
                    .colors(entry.value.markerColor(isError), null)
            ).append(entry.value.msg.pretty().align())

    private fun <T> processLineStart(
        pos: Position,
        specialPrefix: Document.Builder,
        dotPrefixedDocument: Document.Builder,
        pipes: MutableList<Pair<Position, Doc.Builder<T>>>
    ): Document.Builder {
        pipes.sortBy { it.first.startColumn }

        val res = processAllColumn(pipes)
        val documentBuilder = Document.Builder()
            .append(dotPrefixedDocument)
            .append(Doc.Builder.space())
            .append(specialPrefix)

        for (docBuilder in res.second) {
            documentBuilder.append(docBuilder)
        }
        documentBuilder.append(pad(pos.startColumn - res.first, ' ', Doc.Builder.empty(), ::identity))

        return documentBuilder
    }

    private fun processAllColumn(
        marks: List<Pair<Position, Doc.Builder<*>>>
    ): Pair<Int, List<Doc.Builder<*>>> {
        val docs = mutableListOf<Doc.Builder<*>>()
        var ret = 1

        var i = 0
        while (i < marks.size) {
            val (pos, docBuilder) = marks[i]

            if (ret == pos.startColumn) {
                docs += docBuilder
                i++
            } else if (ret < pos.startColumn) {
                docs += Doc.Builder.space()
            } else {
                docs += Doc.Builder.space()
                i++
            }

            ret++
        }

        return ret to docs
    }

    private inner class MarkerDistincter : (Map.Entry<Position, Marker<Msg>>) -> Boolean {
        private var previous: Map.Entry<Position, Marker<Msg>>? = null

        override fun invoke(p1: Map.Entry<Position, Marker<Msg>>): Boolean {
            if (previous != null) {
                val (pos1, _) = previous!!
                val (pos2, _) = p1

                if (pos1.startLine == pos2.startLine && pos1.startColumn == pos2.startColumn) {
                    return false
                }
            }

            previous = p1
            return true
        }
    }
}