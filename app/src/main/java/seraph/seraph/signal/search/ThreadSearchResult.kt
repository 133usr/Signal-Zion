package seraph.zion.signal.search

import seraph.zion.signal.database.model.ThreadRecord

data class ThreadSearchResult(val results: List<ThreadRecord>, val query: String)
