package seraph.zion.signal.search

import seraph.zion.signal.recipients.Recipient

data class ContactSearchResult(val results: List<Recipient>, val query: String)
