package seraph.zion.signal.stories.viewer.post

import android.graphics.Typeface
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import org.signal.core.util.Base64
import seraph.zion.signal.database.SignalDatabase
import seraph.zion.signal.database.model.MmsMessageRecord
import seraph.zion.signal.database.model.databaseprotos.StoryTextPost
import seraph.zion.signal.dependencies.ApplicationDependencies
import seraph.zion.signal.fonts.TextFont
import seraph.zion.signal.fonts.TextToScript
import seraph.zion.signal.fonts.TypefaceCache

class StoryTextPostRepository {
  fun getRecord(recordId: Long): Single<MmsMessageRecord> {
    return Single.fromCallable {
      SignalDatabase.messages.getMessageRecord(recordId) as MmsMessageRecord
    }.subscribeOn(Schedulers.io())
  }

  fun getTypeface(recordId: Long): Single<Typeface> {
    return getRecord(recordId).flatMap {
      val model = StoryTextPost.ADAPTER.decode(Base64.decode(it.body))
      val textFont = TextFont.fromStyle(model.style)
      val script = TextToScript.guessScript(model.body)

      TypefaceCache.get(ApplicationDependencies.getApplication(), textFont, script)
    }
  }
}
