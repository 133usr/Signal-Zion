package seraph.zion.signal.stories.settings.my

import seraph.zion.signal.database.model.DistributionListPrivacyMode

data class MyStoryPrivacyState(val privacyMode: DistributionListPrivacyMode? = null, val connectionCount: Int = 0)
