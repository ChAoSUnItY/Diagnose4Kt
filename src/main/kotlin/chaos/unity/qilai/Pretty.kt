package chaos.unity.qilai

import chaos.unity.qilai.doc.Document

interface Pretty<E> {
    fun pretty(): Document
}