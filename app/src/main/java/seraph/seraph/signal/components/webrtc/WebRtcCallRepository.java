package seraph.zion.signal.components.webrtc;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.core.util.Consumer;

import org.signal.core.util.concurrent.SignalExecutors;
import seraph.zion.signal.database.GroupTable;
import seraph.zion.signal.database.SignalDatabase;
import seraph.zion.signal.database.identity.IdentityRecordList;
import seraph.zion.signal.dependencies.ApplicationDependencies;
import seraph.zion.signal.recipients.Recipient;

import java.util.Collections;
import java.util.List;

class WebRtcCallRepository {

  private final Context      context;

  WebRtcCallRepository(@NonNull Context context) {
    this.context      = context;
  }

  @WorkerThread
  void getIdentityRecords(@NonNull Recipient recipient, @NonNull Consumer<IdentityRecordList> consumer) {
    SignalExecutors.BOUNDED.execute(() -> {
      List<Recipient> recipients;

      if (recipient.isGroup()) {
        recipients = SignalDatabase.groups().getGroupMembers(recipient.requireGroupId(), GroupTable.MemberSet.FULL_MEMBERS_EXCLUDING_SELF);
      } else {
        recipients = Collections.singletonList(recipient);
      }

      consumer.accept(ApplicationDependencies.getProtocolStore().aci().identities().getIdentityRecords(recipients));
    });
  }
}
