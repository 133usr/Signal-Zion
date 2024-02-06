package seraph.zion.signal.absbackup.backupables

import org.signal.core.util.logging.Log
import seraph.zion.signal.absbackup.AndroidBackupItem
import seraph.zion.signal.absbackup.protos.SvrAuthToken
import seraph.zion.signal.keyvalue.SignalStore
import java.io.IOException

/**
 * This backs up the not-secret KBS Auth tokens, which can be combined with a PIN to prove ownership of a phone number in order to complete the registration process.
 */
object SvrAuthTokens : AndroidBackupItem {
  private const val TAG = "KbsAuthTokens"

  override fun getKey(): String {
    return TAG
  }

  override fun getDataForBackup(): ByteArray {
    val proto = SvrAuthToken(tokens = SignalStore.svr().authTokenList)
    return proto.encode()
  }

  override fun restoreData(data: ByteArray) {
    if (SignalStore.svr().authTokenList.isNotEmpty()) {
      return
    }

    try {
      val proto = SvrAuthToken.ADAPTER.decode(data)

      SignalStore.svr().putAuthTokenList(proto.tokens)
    } catch (e: IOException) {
      Log.w(TAG, "Cannot restore KbsAuthToken from backup service.")
    }
  }
}
