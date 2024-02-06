/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package seraph.zion.signal.backup.v2.stream

import org.junit.Assert.assertEquals
import org.junit.Test
import seraph.zion.signal.backup.v2.proto.AccountData
import seraph.zion.signal.backup.v2.proto.Frame
import seraph.zion.signal.util.Util
import org.whispersystems.signalservice.api.backup.BackupKey
import org.whispersystems.signalservice.api.push.ServiceId.ACI
import java.io.ByteArrayOutputStream
import java.util.UUID

class EncryptedBackupReaderWriterTest {

  @Test
  fun `can read back all of the frames we write`() {
    val key = BackupKey(Util.getSecretBytes(32))
    val aci = ACI.from(UUID.randomUUID())

    val outputStream = ByteArrayOutputStream()

    val frameCount = 10_000
    EncryptedBackupWriter(key, aci, outputStream, append = { outputStream.write(it) }).use { writer ->
      for (i in 0 until frameCount) {
        writer.write(Frame(account = AccountData(username = "username-$i")))
      }
    }

    val ciphertext: ByteArray = outputStream.toByteArray()

    val frames: List<Frame> = EncryptedBackupReader(key, aci, ciphertext.size.toLong()) { ciphertext.inputStream() }.use { reader ->
      reader.asSequence().toList()
    }

    assertEquals(frameCount, frames.size)

    for (i in 0 until frameCount) {
      assertEquals("username-$i", frames[i].account?.username)
    }
  }
}
