package seraph.zion.signal.stories.viewer

import android.net.Uri
import seraph.zion.signal.blurhash.BlurHash
import seraph.zion.signal.database.model.MmsMessageRecord
import seraph.zion.signal.recipients.RecipientId
import seraph.zion.signal.stories.StoryTextPostModel

data class StoryViewerState(
  val pages: List<RecipientId> = emptyList(),
  val previousPage: Int = -1,
  val page: Int = -1,
  val crossfadeSource: CrossfadeSource,
  val crossfadeTarget: CrossfadeTarget? = null,
  val skipCrossfade: Boolean = false,
  val noPosts: Boolean = false
) {
  sealed class CrossfadeSource {
    object None : CrossfadeSource()
    class ImageUri(val imageUri: Uri, val imageBlur: BlurHash?) : CrossfadeSource()
    class TextModel(val storyTextPostModel: StoryTextPostModel) : CrossfadeSource()
  }

  sealed class CrossfadeTarget {
    object None : CrossfadeTarget()
    data class Record(val messageRecord: MmsMessageRecord) : CrossfadeTarget()
  }
}
