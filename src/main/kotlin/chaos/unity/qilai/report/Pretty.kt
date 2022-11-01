package chaos.unity.qilai.report

import chaos.unity.qilai.doc.Document

interface Pretty<E> {
    fun pretty(): Document.Builder
}