package seraph.zion.signal.messagedetails;

import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;

import org.signal.core.util.concurrent.SignalExecutors;
import seraph.zion.signal.database.DatabaseObserver;
import seraph.zion.signal.database.MessageTable;
import seraph.zion.signal.database.NoSuchMessageException;
import seraph.zion.signal.database.SignalDatabase;
import seraph.zion.signal.database.model.MessageId;
import seraph.zion.signal.database.model.MessageRecord;
import seraph.zion.signal.dependencies.ApplicationDependencies;

final class MessageRecordLiveData extends LiveData<MessageRecord> {

  private final DatabaseObserver.Observer observer;
  private final MessageId                 messageId;

  MessageRecordLiveData(MessageId messageId) {
    this.messageId = messageId;
    this.observer  = this::retrieveMessageRecordActual;
  }

  @Override
  protected void onActive() {
    SignalExecutors.BOUNDED_IO.execute(this::retrieveMessageRecordActual);
  }

  @Override
  protected void onInactive() {
    ApplicationDependencies.getDatabaseObserver().unregisterObserver(observer);
  }

  @WorkerThread
  private synchronized void retrieveMessageRecordActual() {
    try {
      MessageRecord record = SignalDatabase.messages().getMessageRecord(messageId.getId());

      if (record.isPaymentNotification()) {
        record = SignalDatabase.payments().updateMessageWithPayment(record);
      }

      postValue(record);
      ApplicationDependencies.getDatabaseObserver().registerVerboseConversationObserver(record.getThreadId(), observer);
    } catch (NoSuchMessageException ignored) {
      postValue(null);
    }
  }
}
