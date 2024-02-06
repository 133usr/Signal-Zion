package seraph.zion.signal.stories.settings.create

import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import seraph.zion.signal.database.SignalDatabase
import seraph.zion.signal.recipients.RecipientId
import seraph.zion.signal.stories.Stories

class CreateStoryWithViewersRepository {
  fun createList(name: CharSequence, members: Set<RecipientId>): Single<RecipientId> {
    return Single.create<RecipientId> {
      val result = SignalDatabase.distributionLists.createList(name.toString(), members.toList())
      if (result == null) {
        it.onError(Exception("Null result, due to a duplicated name."))
      } else {
        Stories.onStorySettingsChanged(result)
        it.onSuccess(SignalDatabase.recipients.getOrInsertFromDistributionListId(result))
      }
    }.subscribeOn(Schedulers.io())
  }
}
