package seraph.zion.signal.jobs

import android.os.Build
import org.signal.core.util.logging.Log
import seraph.zion.signal.dependencies.ApplicationDependencies
import seraph.zion.signal.jobmanager.Job
import seraph.zion.signal.jobmanager.JsonJobData
import seraph.zion.signal.recipients.Recipient
import seraph.zion.signal.recipients.RecipientId
import seraph.zion.signal.transport.RetryLaterException
import seraph.zion.signal.util.ConversationUtil
import seraph.zion.signal.util.ConversationUtil.Direction
import seraph.zion.signal.util.TextSecurePreferences
import kotlin.time.Duration.Companion.seconds

/**
 * Updates the ranking of a shortcut by providing hints for when we send/receive messages to different recipients.
 */
class ConversationShortcutRankingUpdateJob private constructor(
  parameters: Parameters,
  private val recipient: Recipient,
  private val direction: Direction
) : BaseJob(parameters) {

  companion object {
    private val TAG = Log.tag(ConversationShortcutRankingUpdateJob::class.java)

    const val KEY = "ConversationShortcutRankingUpdateJob"

    private const val KEY_RECIPIENT = "recipient"
    private const val KEY_REPORTED_SIGNAL = "reported_signal"

    @JvmStatic
    fun enqueueForOutgoingIfNecessary(recipient: Recipient) {
      if (Build.VERSION.SDK_INT >= 34) {
        ApplicationDependencies.getJobManager().add(ConversationShortcutRankingUpdateJob(recipient, Direction.OUTGOING))
      }
    }

    @JvmStatic
    fun enqueueForIncomingIfNecessary(recipient: Recipient) {
      if (Build.VERSION.SDK_INT >= 34) {
        ApplicationDependencies.getJobManager().add(ConversationShortcutRankingUpdateJob(recipient, Direction.INCOMING))
      }
    }
  }

  private constructor(recipient: Recipient, direction: Direction) : this(
    Parameters.Builder()
      .setQueue("ConversationShortcutRankingUpdateJob::${recipient.id.serialize()}")
      .setMaxInstancesForQueue(1)
      .setMaxAttempts(3)
      .build(),
    recipient,
    direction
  )

  override fun serialize(): ByteArray? {
    return JsonJobData.Builder()
      .putString(KEY_RECIPIENT, recipient.id.serialize())
      .putInt(KEY_REPORTED_SIGNAL, direction.serialize())
      .serialize()
  }

  override fun getFactoryKey() = KEY

  override fun onRun() {
    if (TextSecurePreferences.isScreenLockEnabled(context)) {
      Log.i(TAG, "Screen lock enabled. Clearing shortcuts.")
      ConversationUtil.clearAllShortcuts(context)
      return
    }

    val success: Boolean = ConversationUtil.pushShortcutForRecipientSync(context, recipient, direction)

    if (!success) {
      Log.w(TAG, "Failed to update shortcut for ${recipient.id}. Possibly retrying.")
      throw RetryLaterException()
    }
  }

  override fun onShouldRetry(e: Exception): Boolean {
    return e is RetryLaterException
  }

  override fun getNextRunAttemptBackoff(pastAttemptCount: Int, exception: Exception): Long {
    return 30.seconds.inWholeMilliseconds
  }

  override fun onFailure() = Unit

  class Factory : Job.Factory<ConversationShortcutRankingUpdateJob?> {
    override fun create(parameters: Parameters, serializedData: ByteArray?): ConversationShortcutRankingUpdateJob {
      val data = JsonJobData.deserialize(serializedData)
      val recipient: Recipient = Recipient.resolved(RecipientId.from(data.getString(KEY_RECIPIENT)))
      val direction: Direction = Direction.deserialize(data.getInt(KEY_REPORTED_SIGNAL))

      return ConversationShortcutRankingUpdateJob(parameters, recipient, direction)
    }
  }
}
