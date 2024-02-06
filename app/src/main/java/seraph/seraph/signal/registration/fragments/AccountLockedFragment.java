package seraph.zion.signal.registration.fragments;

import androidx.lifecycle.ViewModelProvider;

import seraph.zion.signal.R;
import seraph.zion.signal.registration.viewmodel.BaseRegistrationViewModel;
import seraph.zion.signal.registration.viewmodel.RegistrationViewModel;

public class AccountLockedFragment extends BaseAccountLockedFragment {

  public AccountLockedFragment() {
    super(R.layout.account_locked_fragment);
  }

  @Override
  protected BaseRegistrationViewModel getViewModel() {
    return new ViewModelProvider(requireActivity()).get(RegistrationViewModel.class);
  }

  @Override
  protected void onNext() {
    requireActivity().finish();
  }
}
