package seraph.zion.signal.mms;


import android.content.Context;

import androidx.annotation.NonNull;

import seraph.zion.signal.attachments.Attachment;

public class MmsSlide extends ImageSlide {

  public MmsSlide(@NonNull Attachment attachment) {
    super(attachment);
  }

  @NonNull
  @Override
  public String getContentDescription(Context context) {
    return "MMS";
  }

}
