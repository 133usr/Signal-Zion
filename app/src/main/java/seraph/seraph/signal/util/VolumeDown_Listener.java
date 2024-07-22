/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package seraph.zion.signal.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.widget.Toast;

public class VolumeDown_Listener implements KeyEvent.Callback {
  private static final int REQUIRED_VOLUME_DOWN_COUNT = 7;
  private static final long RESET_DELAY_MILLIS = 2000; // 2 seconds
  private int volumeDownCount = 0;
  private Context context;
  private Handler handler = new Handler(Looper.getMainLooper());
  private Runnable resetCounterRunnable;

  public VolumeDown_Listener(Context context) {
    this.context = context;
    resetCounterRunnable = new Runnable() {
      @Override
      public void run() {
        volumeDownCount = 0;
      }
    };
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
      volumeDownCount++;
      if (volumeDownCount == REQUIRED_VOLUME_DOWN_COUNT) {
        // Perform your action when volume down is pressed 7 times
        showToast("Volume Down pressed 7 times!");
        // Reset the count for future presses after a delay
        handler.removeCallbacks(resetCounterRunnable);
        handler.postDelayed(resetCounterRunnable, RESET_DELAY_MILLIS);
        return true; // Consume the volume down key event
      } else {
        return false; // Ignore the volume down key event
      }
    } else if (keyCode == KeyEvent.KEYCODE_BACK) {
      // Handle back key event if needed
      return false; // Allow the back key event to propagate
    }
    return false;
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    return true;
  }

  @Override
  public boolean onKeyLongPress(int keyCode, KeyEvent event) {
    return true;
  }

  @Override
  public boolean onKeyMultiple(int keyCode, int count, KeyEvent event) {
    return true;
  }

  private void showToast(String message) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
  }
}
