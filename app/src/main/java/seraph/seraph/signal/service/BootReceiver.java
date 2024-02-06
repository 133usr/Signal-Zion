package seraph.zion.signal.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import seraph.zion.signal.dependencies.ApplicationDependencies;
import seraph.zion.signal.jobs.MessageFetchJob;

public class BootReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    ApplicationDependencies.getJobManager().add(new MessageFetchJob());
  }
}
