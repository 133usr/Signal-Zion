/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package seraph.zion.signal.jobs

import seraph.zion.signal.database.SignalDatabase
import seraph.zion.signal.dependencies.ApplicationDependencies
import seraph.zion.signal.jobmanager.Job
import seraph.zion.signal.jobmanager.impl.NetworkConstraint
import java.util.concurrent.TimeUnit

/**
 * Cleans database of stale group rings which can occur if the device or application
 * crashes while an incoming ring is happening.
 */
class GroupRingCleanupJob private constructor(parameters: Parameters) : BaseJob(parameters) {
  companion object {
    const val KEY = "GroupRingCleanupJob"

    @JvmStatic
    fun enqueue() {
      ApplicationDependencies.getJobManager().add(
        GroupRingCleanupJob(
          Parameters.Builder()
            .addConstraint(NetworkConstraint.KEY)
            .setLifespan(TimeUnit.HOURS.toMillis(1))
            .setMaxInstancesForFactory(1)
            .setQueue(KEY)
            .build()
        )
      )
    }
  }

  override fun serialize(): ByteArray? = null

  override fun getFactoryKey(): String = KEY

  override fun onFailure() = Unit

  override fun onRun() {
    SignalDatabase.calls.getLatestRingingCalls().forEach {
      ApplicationDependencies.getSignalCallManager().peekGroupCall(it.peer)
    }

    SignalDatabase.calls.markRingingCallsAsMissed()
  }

  override fun onShouldRetry(e: Exception): Boolean = false

  class Factory : Job.Factory<GroupRingCleanupJob> {
    override fun create(parameters: Parameters, serializedData: ByteArray?): GroupRingCleanupJob {
      return GroupRingCleanupJob(parameters)
    }
  }
}
