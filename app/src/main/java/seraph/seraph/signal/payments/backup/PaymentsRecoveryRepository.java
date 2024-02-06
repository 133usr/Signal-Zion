package seraph.zion.signal.payments.backup;

import androidx.annotation.NonNull;

import seraph.zion.signal.keyvalue.SignalStore;
import seraph.zion.signal.payments.Mnemonic;

public final class PaymentsRecoveryRepository {
  public @NonNull Mnemonic getMnemonic() {
    return SignalStore.paymentsValues().getPaymentsMnemonic();
  }
}
