package seraph.zion.signal.database.loaders;

import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import seraph.zion.signal.database.MediaTable;
import seraph.zion.signal.database.SignalDatabase;
import seraph.zion.signal.recipients.Recipient;
import seraph.zion.signal.recipients.RecipientId;

/**
 * It is more efficient to use the {@link ThreadMediaLoader} if you know the thread id already.
 */
public final class RecipientMediaLoader extends MediaLoader {

  @Nullable private final RecipientId           recipientId;
  @NonNull  private final MediaType          mediaType;
  @NonNull  private final MediaTable.Sorting sorting;

  public RecipientMediaLoader(@NonNull Context context,
                              @Nullable RecipientId recipientId,
                              @NonNull MediaType mediaType,
                              @NonNull MediaTable.Sorting sorting)
  {
    super(context);
    this.recipientId = recipientId;
    this.mediaType   = mediaType;
    this.sorting     = sorting;
  }

  @Override
  public Cursor getCursor() {
    if (recipientId == null || recipientId.isUnknown()) return null;

    long threadId = SignalDatabase.threads().getOrCreateThreadIdFor(Recipient.resolved(recipientId));

    return ThreadMediaLoader.createThreadMediaCursor(context, threadId, mediaType, sorting);
  }

}
