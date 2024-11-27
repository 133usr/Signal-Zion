package org.thoughtcrime.securesms.glide;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

import org.signal.core.util.logging.Log;
import org.thoughtcrime.securesms.badges.models.Badge;
import org.thoughtcrime.securesms.dependencies.AppDependencies;

import java.io.InputStream;

import okhttp3.OkHttpClient;

/**
 * A loader which will load a sprite sheet for a particular badge at the correct dpi for this device.
 */
public class BadgeLoader implements ModelLoader<Badge, InputStream> {

  private final OkHttpClient client;

  private BadgeLoader(OkHttpClient client) {
    this.client = client;
  }

  @Override
  public @Nullable LoadData<InputStream> buildLoadData(@NonNull Badge request, int width, int height, @NonNull Options options) {
    String modUrl = "65cd037861909df8e2f72cfe43e61b85e5d47078c722ab9e547ba7b221ddaefd.png";
    return new LoadData<>(request, new OkHttpStreamFetcher(client, new GlideUrl(modUrl)));
  }

  @Override
  public boolean handles(@NonNull Badge badgeSpriteSheetRequest) {
    return true;
  }

  public static Factory createFactory() {
    return new Factory(AppDependencies.getSignalOkHttpClient());
  }

  public static class Factory implements ModelLoaderFactory<Badge, InputStream> {

    private final OkHttpClient client;

    private Factory(@NonNull OkHttpClient client) {
      this.client = client;
    }

    @Override
    public @NonNull ModelLoader<Badge, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
      return new BadgeLoader(client);
    }

    @Override
    public void teardown() {
    }
  }
}