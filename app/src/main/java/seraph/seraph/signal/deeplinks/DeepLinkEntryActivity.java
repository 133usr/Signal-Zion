package seraph.zion.signal.deeplinks;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import seraph.zion.signal.MainActivity;
import seraph.zion.signal.PassphraseRequiredActivity;

public class DeepLinkEntryActivity extends PassphraseRequiredActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState, boolean ready) {
    Intent intent = MainActivity.clearTop(this);
    Uri    data   = getIntent().getData();
    intent.setData(data);
    startActivity(intent);
  }
}
