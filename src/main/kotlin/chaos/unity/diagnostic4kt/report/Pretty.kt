package chaos.unity.diagnostic4kt.report

import chaos.unity.diagnostic4kt.doc.Document

interface Pretty<E> where E: Pretty<E> {
    fun pretty(): Document.Builder
}