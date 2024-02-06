package seraph.zion.signal.database

import android.net.Uri
import seraph.zion.signal.attachments.UriAttachment
import seraph.zion.signal.audio.AudioHash
import seraph.zion.signal.blurhash.BlurHash
import seraph.zion.signal.stickers.StickerLocator

object UriAttachmentBuilder {
  fun build(
    id: Long,
    uri: Uri = Uri.parse("content://$id"),
    contentType: String,
    transferState: Int = AttachmentTable.TRANSFER_PROGRESS_PENDING,
    size: Long = 0L,
    fileName: String = "file$id",
    voiceNote: Boolean = false,
    borderless: Boolean = false,
    videoGif: Boolean = false,
    quote: Boolean = false,
    caption: String? = null,
    stickerLocator: StickerLocator? = null,
    blurHash: BlurHash? = null,
    audioHash: AudioHash? = null,
    transformProperties: AttachmentTable.TransformProperties? = null
  ): UriAttachment {
    return UriAttachment(
      uri,
      contentType,
      transferState,
      size,
      fileName,
      voiceNote,
      borderless,
      videoGif,
      quote,
      caption,
      stickerLocator,
      blurHash,
      audioHash,
      transformProperties
    )
  }
}
