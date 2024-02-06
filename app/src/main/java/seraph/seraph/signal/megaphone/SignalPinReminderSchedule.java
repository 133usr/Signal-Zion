package seraph.zion.signal.megaphone;

import seraph.zion.signal.keyvalue.SignalStore;

final class SignalPinReminderSchedule implements MegaphoneSchedule {

  @Override
  public boolean shouldDisplay(int seenCount, long lastSeen, long firstVisible, long currentTime) {
    if (SignalStore.svr().hasOptedOut()) {
      return false;
    }

    if (!SignalStore.svr().hasPin()) {
      return false;
    }

    if (!SignalStore.pinValues().arePinRemindersEnabled()) {
      return false;
    }

    if (!SignalStore.account().isRegistered()) {
      return false;
    }

    long lastSuccessTime = SignalStore.pinValues().getLastSuccessfulEntryTime();
    long interval        = SignalStore.pinValues().getCurrentInterval();

    return currentTime - lastSuccessTime >= interval;
  }
}
