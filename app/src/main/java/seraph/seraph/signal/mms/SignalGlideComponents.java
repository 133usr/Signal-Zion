package seraph.zion.signal.mms;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.UnitModelLoader;
import com.bumptech.glide.load.resource.bitmap.BitmapDrawableEncoder;
import com.bumptech.glide.load.resource.bitmap.Downsampler;
import com.bumptech.glide.load.resource.bitmap.StreamBitmapDecoder;
import com.bumptech.glide.load.resource.gif.ByteBufferGifDecoder;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.load.resource.gif.StreamGifDecoder;

import org.signal.glide.apng.decode.APNGDecoder;
import seraph.zion.signal.badges.models.Badge;
import seraph.zion.signal.blurhash.BlurHash;
import seraph.zion.signal.blurhash.BlurHashModelLoader;
import seraph.zion.signal.blurhash.BlurHashResourceDecoder;
import seraph.zion.signal.contacts.avatars.ContactPhoto;
import seraph.zion.signal.crypto.AttachmentSecret;
import seraph.zion.signal.crypto.AttachmentSecretProvider;
import seraph.zion.signal.giph.model.ChunkedImageUrl;
import seraph.zion.signal.glide.BadgeLoader;
import seraph.zion.signal.glide.ChunkedImageUrlLoader;
import seraph.zion.signal.glide.ContactPhotoLoader;
import seraph.zion.signal.glide.GiftBadgeModel;
import seraph.zion.signal.glide.OkHttpUrlLoader;
import seraph.zion.signal.glide.cache.ApngBufferCacheDecoder;
import seraph.zion.signal.glide.cache.ApngFrameDrawableTranscoder;
import seraph.zion.signal.glide.cache.ApngStreamCacheDecoder;
import seraph.zion.signal.glide.cache.EncryptedApngCacheEncoder;
import seraph.zion.signal.glide.cache.EncryptedBitmapResourceEncoder;
import seraph.zion.signal.glide.cache.EncryptedCacheDecoder;
import seraph.zion.signal.glide.cache.EncryptedCacheEncoder;
import seraph.zion.signal.glide.cache.EncryptedGifDrawableResourceEncoder;
import seraph.zion.signal.glide.cache.WebpSanDecoder;
import seraph.zion.signal.mms.AttachmentStreamUriLoader.AttachmentModel;
import seraph.zion.signal.mms.DecryptableStreamUriLoader.DecryptableUri;
import seraph.zion.signal.stickers.StickerRemoteUri;
import seraph.zion.signal.stickers.StickerRemoteUriLoader;
import seraph.zion.signal.stories.StoryTextPostModel;
import seraph.zion.signal.util.ConversationShortcutPhoto;

import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * The core logic for {@link SignalGlideModule}. This is a separate class because it uses
 * dependencies defined in the main Gradle module.
 */
public class SignalGlideComponents implements RegisterGlideComponents {

  @Override
  public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
    AttachmentSecret attachmentSecret = AttachmentSecretProvider.getInstance(context).getOrCreateAttachmentSecret();
    byte[]           secret           = attachmentSecret.getModernKey();

    registry.prepend(File.class, File.class, UnitModelLoader.Factory.getInstance());

    registry.prepend(InputStream.class, Bitmap.class, new WebpSanDecoder());

    registry.prepend(InputStream.class, new EncryptedCacheEncoder(secret, glide.getArrayPool()));

    registry.prepend(File.class, Bitmap.class, new EncryptedCacheDecoder<>(secret, new StreamBitmapDecoder(new Downsampler(registry.getImageHeaderParsers(), context.getResources().getDisplayMetrics(), glide.getBitmapPool(), glide.getArrayPool()), glide.getArrayPool())));

    StreamGifDecoder streamGifDecoder = new StreamGifDecoder(registry.getImageHeaderParsers(), new ByteBufferGifDecoder(context, registry.getImageHeaderParsers(), glide.getBitmapPool(), glide.getArrayPool()), glide.getArrayPool());
    registry.prepend(InputStream.class, GifDrawable.class, streamGifDecoder);
    registry.prepend(GifDrawable.class, new EncryptedGifDrawableResourceEncoder(secret));
    registry.prepend(File.class, GifDrawable.class, new EncryptedCacheDecoder<>(secret, streamGifDecoder));

    EncryptedBitmapResourceEncoder encryptedBitmapResourceEncoder = new EncryptedBitmapResourceEncoder(secret);
    registry.prepend(Bitmap.class, new EncryptedBitmapResourceEncoder(secret));
    registry.prepend(BitmapDrawable.class, new BitmapDrawableEncoder(glide.getBitmapPool(), encryptedBitmapResourceEncoder));

    ApngBufferCacheDecoder apngBufferCacheDecoder = new ApngBufferCacheDecoder();
    ApngStreamCacheDecoder apngStreamCacheDecoder = new ApngStreamCacheDecoder(apngBufferCacheDecoder);

    registry.prepend(InputStream.class, APNGDecoder.class, apngStreamCacheDecoder);
    registry.prepend(ByteBuffer.class, APNGDecoder.class, apngBufferCacheDecoder);
    registry.prepend(APNGDecoder.class, new EncryptedApngCacheEncoder(secret));
    registry.prepend(File.class, APNGDecoder.class, new EncryptedCacheDecoder<>(secret, apngStreamCacheDecoder));
    registry.register(APNGDecoder.class, Drawable.class, new ApngFrameDrawableTranscoder());

    registry.prepend(BlurHash.class, Bitmap.class, new BlurHashResourceDecoder());
    registry.prepend(StoryTextPostModel.class, Bitmap.class, new StoryTextPostModel.Decoder());

    registry.append(StoryTextPostModel.class, StoryTextPostModel.class, UnitModelLoader.Factory.getInstance());
    registry.append(ConversationShortcutPhoto.class, Bitmap.class, new ConversationShortcutPhoto.Loader.Factory(context));
    registry.append(ContactPhoto.class, InputStream.class, new ContactPhotoLoader.Factory(context));
    registry.append(DecryptableUri.class, InputStream.class, new DecryptableStreamUriLoader.Factory(context));
    registry.append(AttachmentModel.class, InputStream.class, new AttachmentStreamUriLoader.Factory());
    registry.append(ChunkedImageUrl.class, InputStream.class, new ChunkedImageUrlLoader.Factory());
    registry.append(StickerRemoteUri.class, InputStream.class, new StickerRemoteUriLoader.Factory());
    registry.append(BlurHash.class, BlurHash.class, new BlurHashModelLoader.Factory());
    registry.append(Badge.class, InputStream.class, BadgeLoader.createFactory());
    registry.append(GiftBadgeModel.class, InputStream.class, GiftBadgeModel.createFactory());
    registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory());
  }
}
