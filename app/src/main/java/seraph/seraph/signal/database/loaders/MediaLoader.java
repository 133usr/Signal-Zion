package seraph.zion.signal.database.loaders;

import android.content.Context;

import seraph.zion.signal.util.AbstractCursorLoader;

public abstract class MediaLoader extends AbstractCursorLoader {

  MediaLoader(Context context) {
    super(context);
  }

  public enum MediaType {
    GALLERY,
    DOCUMENT,
    AUDIO,
    ALL
  }
}
