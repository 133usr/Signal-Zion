package seraph.zion.signal.jobmanager.impl;

import android.app.job.JobInfo;

import androidx.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;
import seraph.zion.signal.events.WebRtcViewModel;
import seraph.zion.signal.jobmanager.Constraint;

/**
 * Constraint met when the user is not in an active, connected call.
 */
public final class NotInCallConstraint implements Constraint {

  public static final String KEY = "NotInCallConstraint";

  @Override
  public boolean isMet() {
    return isNotInConnectedCall();
  }

  @Override
  public @NonNull String getFactoryKey() {
    return KEY;
  }

  public static boolean isNotInConnectedCall() {
    WebRtcViewModel viewModel = EventBus.getDefault().getStickyEvent(WebRtcViewModel.class);
    return viewModel == null || (viewModel.getState() != WebRtcViewModel.State.CALL_CONNECTED && viewModel.getState() != WebRtcViewModel.State.CALL_RECONNECTING);
  }

  @Override
  public void applyToJobInfo(@NonNull JobInfo.Builder jobInfoBuilder) {
  }

  public static class Factory implements Constraint.Factory<NotInCallConstraint> {
    @Override
    public NotInCallConstraint create() {
      return new NotInCallConstraint();
    }
  }
}
