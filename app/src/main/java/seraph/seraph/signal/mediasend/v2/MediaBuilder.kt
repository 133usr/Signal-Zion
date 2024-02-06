package seraph.zion.signal.mediasend.v2

import android.net.Uri
import seraph.zion.signal.database.AttachmentTable
import seraph.zion.signal.mediasend.Media
import java.util.Optional

object MediaBuilder {
  fun buildMedia(
    uri: Uri,
    mimeType: String = "",
    date: Long = 0L,
    width: Int = 0,
    height: Int = 0,
    size: Long = 0L,
    duration: Long = 0L,
    borderless: Boolean = false,
    videoGif: Boolean = false,
    bucketId: Optional<String> = Optional.empty(),
    caption: Optional<String> = Optional.empty(),
    transformProperties: Optional<AttachmentTable.TransformProperties> = Optional.empty()
  ) = Media(uri, mimeType, date, width, height, size, duration, borderless, videoGif, bucketId, caption, transformProperties)
}
