package seraph.zion.signal.jobmanager.impl

import android.app.job.JobInfo
import seraph.zion.signal.jobmanager.Constraint
import seraph.zion.signal.keyvalue.SignalStore

/**
 * Constraint that, when added, means that a job cannot be performed while a change number operation is in progress.
 */
object ChangeNumberConstraint : Constraint {

  const val KEY = "ChangeNumberConstraint"

  override fun isMet(): Boolean {
    return !SignalStore.misc().isChangeNumberLocked
  }

  override fun getFactoryKey(): String = KEY

  override fun applyToJobInfo(jobInfoBuilder: JobInfo.Builder) = Unit

  class Factory : Constraint.Factory<ChangeNumberConstraint> {
    override fun create(): ChangeNumberConstraint {
      return ChangeNumberConstraint
    }
  }
}
