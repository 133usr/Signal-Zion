package seraph.zion.signal.components.settings.app.subscription.receipts.list

import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import seraph.zion.signal.database.SignalDatabase
import seraph.zion.signal.database.model.DonationReceiptRecord

class DonationReceiptListPageRepository {
  fun getRecords(type: DonationReceiptRecord.Type?): Single<List<DonationReceiptRecord>> {
    return Single.fromCallable {
      SignalDatabase.donationReceipts.getReceipts(type)
    }.subscribeOn(Schedulers.io())
  }
}
