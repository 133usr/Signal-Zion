package seraph.zion.signal.mms;


import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import seraph.zion.signal.attachments.Attachment;
import seraph.zion.signal.util.StorageUtil;

public class DocumentSlide extends Slide {

  public DocumentSlide(@NonNull Attachment attachment) {
    super(attachment);
  }

  public DocumentSlide(@NonNull Context context, @NonNull Uri uri,
                       @NonNull String contentType,  long size,
                       @Nullable String fileName)
  {
    super(constructAttachmentFromUri(context, uri, contentType, size, 0, 0, true, StorageUtil.getCleanFileName(fileName), null, null, null, null, false, false, false, false));
  }

  @Override
  public boolean hasDocument() {
    return true;
  }

}
