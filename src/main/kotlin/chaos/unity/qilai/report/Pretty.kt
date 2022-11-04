package chaos.unity.qilai.report

import chaos.unity.qilai.doc.Document

interface Pretty<E> where E: Pretty<E> {
    fun pretty(): Document.Builder
}