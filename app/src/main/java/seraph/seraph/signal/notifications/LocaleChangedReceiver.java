package seraph.zion.signal.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import seraph.zion.signal.jobs.EmojiSearchIndexDownloadJob;

public class LocaleChangedReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    NotificationChannels.getInstance().onLocaleChanged();
    EmojiSearchIndexDownloadJob.scheduleImmediately();
  }
}
