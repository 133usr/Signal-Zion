package seraph.zion.signal.mediasend.v2.gallery

import seraph.zion.signal.util.adapter.mapping.MappingModel

data class MediaGalleryState(
  val bucketId: String?,
  val bucketTitle: String?,
  val items: List<MappingModel<*>> = listOf()
)
