package chaos.unity.qilai.report

import chaos.unity.qilai.Pretty
import chaos.unity.qilai.doc.Document
import kotlin.math.max

class Report<Msg>(
    private val isError: Boolean,
    private val msg: Msg,
    private val markers: Map<Position, Marker<Msg>>,
    private val hints: List<Marker<Msg>> = listOf()
) where Msg : Pretty<Msg> {
    fun <T> pretty(withUnicode: Boolean) {
        val docBuilder = Document.Builder<T>()
        val sortedMarkers = markers.entries
            .sortedBy { it.key.startLine }
        val maxLineWidth = (if (sortedMarkers.isEmpty()) null else sortedMarkers.last())
            ?.key
            ?.endLine
            ?.toString()
            ?.length
            ?.let {
                max(3, it)
            } ?: 3

    }

    private fun <T> groupMarkersPerFile(
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
            }.mapIndexed { i, entries  ->
                (i == 0) to entries
            }
    }
}