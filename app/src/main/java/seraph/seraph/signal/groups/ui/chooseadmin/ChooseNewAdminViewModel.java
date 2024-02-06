package seraph.zion.signal.groups.ui.chooseadmin;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.annimon.stream.Stream;

import seraph.zion.signal.dependencies.ApplicationDependencies;
import seraph.zion.signal.groups.GroupId;
import seraph.zion.signal.groups.LiveGroup;
import seraph.zion.signal.groups.ui.GroupChangeResult;
import seraph.zion.signal.groups.ui.GroupMemberEntry;
import seraph.zion.signal.recipients.RecipientId;
import org.signal.core.util.concurrent.SimpleTask;

import java.util.Collections;
import java.util.List;
import java.util.Set;

final class ChooseNewAdminViewModel extends ViewModel {

  private final GroupId.V2                                        groupId;
  private final ChooseNewAdminRepository                          repository;
  private final LiveGroup                                         liveGroup;
  private final MutableLiveData<Set<GroupMemberEntry.FullMember>> selection;

  public ChooseNewAdminViewModel(@NonNull GroupId.V2 groupId, @NonNull ChooseNewAdminRepository repository) {
    this.groupId    = groupId;
    this.repository = repository;

    liveGroup = new LiveGroup(groupId);
    selection = new MutableLiveData<>(Collections.emptySet());
  }

  @NonNull LiveData<List<GroupMemberEntry.FullMember>> getNonAdminFullMembers() {
    return liveGroup.getNonAdminFullMembers();
  }

  @NonNull LiveData<Set<GroupMemberEntry.FullMember>> getSelection() {
    return selection;
  }

  void setSelection(@NonNull Set<GroupMemberEntry.FullMember> selection) {
    this.selection.setValue(selection);
  }

  void updateAdminsAndLeave(@NonNull Consumer<GroupChangeResult> consumer) {
    //noinspection ConstantConditions
    List<RecipientId> recipientIds = Stream.of(selection.getValue()).map(entry -> entry.getMember().getId()).toList();
    SimpleTask.run(() -> repository.updateAdminsAndLeave(groupId, recipientIds), consumer::accept);
  }

  static final class Factory implements ViewModelProvider.Factory {

    private final GroupId.V2 groupId;

    Factory(@NonNull GroupId.V2 groupId) {
      this.groupId = groupId;
    }

    @Override
    public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
      //noinspection ConstantConditions
      return modelClass.cast(new ChooseNewAdminViewModel(groupId, new ChooseNewAdminRepository(ApplicationDependencies.getApplication())));
    }
  }
}
