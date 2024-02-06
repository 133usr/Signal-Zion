package seraph.zion.signal.groups.ui.addmembers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import seraph.zion.signal.contacts.SelectedContact;
import seraph.zion.signal.database.SignalDatabase;
import seraph.zion.signal.dependencies.ApplicationDependencies;
import seraph.zion.signal.groups.GroupId;
import seraph.zion.signal.recipients.RecipientId;

final class AddMembersRepository {

  private final Context context;
  private final GroupId groupId;

  AddMembersRepository(@NonNull GroupId groupId) {
    this.groupId = groupId;
    this.context = ApplicationDependencies.getApplication();
  }

  @WorkerThread
  RecipientId getOrCreateRecipientId(@NonNull SelectedContact selectedContact) {
    return selectedContact.getOrCreateRecipientId(context);
  }

  @WorkerThread
  String getGroupTitle() {
    return SignalDatabase.groups().requireGroup(groupId).getTitle();
  }
}
