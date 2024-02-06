package seraph.zion.signal.mediasend.v2.gallery

import android.content.Context
import seraph.zion.signal.mediasend.Media
import seraph.zion.signal.mediasend.MediaFolder
import seraph.zion.signal.mediasend.MediaRepository

class MediaGalleryRepository(context: Context, private val mediaRepository: MediaRepository) {
  private val context: Context = context.applicationContext

  fun getFolders(onFoldersRetrieved: (List<MediaFolder>) -> Unit) {
    mediaRepository.getFolders(context) { onFoldersRetrieved(it) }
  }

  fun getMedia(bucketId: String, onMediaRetrieved: (List<Media>) -> Unit) {
    mediaRepository.getMediaInBucket(context, bucketId) { onMediaRetrieved(it) }
  }
}
