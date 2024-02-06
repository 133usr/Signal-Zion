package seraph.zion.signal.mms;

import android.content.ContentUris;
import android.net.Uri;

import seraph.zion.signal.attachments.AttachmentId;

public class PartUriParser {

  private final Uri uri;

  public PartUriParser(Uri uri) {
    this.uri = uri;
  }

  public AttachmentId getPartId() {
    return new AttachmentId(getId());
  }

  private long getId() {
    return ContentUris.parseId(uri);
  }

  private long getUniqueId() {
    return Long.parseLong(uri.getPathSegments().get(1));
  }

}
