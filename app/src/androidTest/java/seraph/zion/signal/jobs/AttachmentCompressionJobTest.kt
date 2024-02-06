/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package seraph.zion.signal.jobs

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.signal.core.util.StreamUtil
import seraph.zion.signal.attachments.UriAttachment
import seraph.zion.signal.database.AttachmentTable
import seraph.zion.signal.database.SignalDatabase
import seraph.zion.signal.database.UriAttachmentBuilder
import seraph.zion.signal.dependencies.ApplicationDependencies
import seraph.zion.signal.jobmanager.Job
import seraph.zion.signal.mms.SentMediaQuality
import seraph.zion.signal.providers.BlobProvider
import seraph.zion.signal.testing.SignalActivityRule
import seraph.zion.signal.testing.assertIs
import seraph.zion.signal.util.MediaUtil
import java.util.Optional
import java.util.concurrent.CountDownLatch

@RunWith(AndroidJUnit4::class)
class AttachmentCompressionJobTest {

  @get:Rule
  val harness = SignalActivityRule()

  @Test
  fun testCompressionJobsWithDifferentTransformPropertiesCompleteSuccessfully() {
    val imageBytes: ByteArray = InstrumentationRegistry.getInstrumentation().context.resources.assets.open("images/sample_image.png").use {
      StreamUtil.readFully(it)
    }

    val blob = BlobProvider.getInstance().forData(imageBytes).createForSingleSessionOnDisk(ApplicationDependencies.getApplication())

    val firstPreUpload = createAttachment(1, blob, AttachmentTable.TransformProperties.empty())
    val firstDatabaseAttachment = SignalDatabase.attachments.insertAttachmentForPreUpload(firstPreUpload)

    val firstCompressionJob: AttachmentCompressionJob = AttachmentCompressionJob.fromAttachment(firstDatabaseAttachment, false, -1)

    var secondCompressionJob: AttachmentCompressionJob? = null
    var firstJobResult: Job.Result? = null
    var secondJobResult: Job.Result? = null

    val secondJobLatch = CountDownLatch(1)
    val jobThread = Thread {
      firstCompressionJob.setContext(ApplicationDependencies.getApplication())
      firstJobResult = firstCompressionJob.run()

      secondJobLatch.await()

      secondCompressionJob!!.setContext(ApplicationDependencies.getApplication())
      secondJobResult = secondCompressionJob!!.run()
    }

    jobThread.start()
    val secondPreUpload = createAttachment(1, blob, AttachmentTable.TransformProperties.forSentMediaQuality(Optional.empty(), SentMediaQuality.HIGH))
    val secondDatabaseAttachment = SignalDatabase.attachments.insertAttachmentForPreUpload(secondPreUpload)
    secondCompressionJob = AttachmentCompressionJob.fromAttachment(secondDatabaseAttachment, false, -1)

    secondJobLatch.countDown()

    jobThread.join()

    firstJobResult!!.isSuccess assertIs true
    secondJobResult!!.isSuccess assertIs true
  }

  private fun createAttachment(id: Long, uri: Uri, transformProperties: AttachmentTable.TransformProperties): UriAttachment {
    return UriAttachmentBuilder.build(
      id,
      uri = uri,
      contentType = MediaUtil.IMAGE_JPEG,
      transformProperties = transformProperties
    )
  }
}
