package seraph.zion.signal.wallpaper.crop;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import org.signal.core.util.logging.Log;
import seraph.zion.signal.database.SignalDatabase;
import seraph.zion.signal.dependencies.ApplicationDependencies;
import seraph.zion.signal.keyvalue.SignalStore;
import seraph.zion.signal.recipients.RecipientId;
import seraph.zion.signal.wallpaper.ChatWallpaper;
import seraph.zion.signal.wallpaper.WallpaperStorage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

final class WallpaperCropRepository {

  private static final String TAG = Log.tag(WallpaperCropRepository.class);

  @Nullable private final RecipientId recipientId;
  private final           Context     context;

  public WallpaperCropRepository(@Nullable RecipientId recipientId) {
    this.context     = ApplicationDependencies.getApplication();
    this.recipientId = recipientId;
  }

  @WorkerThread
  @NonNull ChatWallpaper setWallPaper(byte[] bytes) throws IOException {
    try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
      ChatWallpaper wallpaper = WallpaperStorage.save(context, inputStream, "webp");

      if (recipientId != null) {
        Log.i(TAG, "Setting image wallpaper for " + recipientId);
        SignalDatabase.recipients().setWallpaper(recipientId, wallpaper);
      } else {
        Log.i(TAG, "Setting image wallpaper for default");
        SignalStore.wallpaper().setWallpaper(context, wallpaper);
      }

      return wallpaper;
    }
  }
}
