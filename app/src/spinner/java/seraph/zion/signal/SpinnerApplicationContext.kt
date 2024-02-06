package seraph.zion.signal

import android.content.ContentValues
import android.os.Build
import org.signal.core.util.logging.AndroidLogger
import org.signal.core.util.logging.Log
import org.signal.spinner.Spinner
import org.signal.spinner.Spinner.DatabaseConfig
import org.signal.spinner.SpinnerLogger
import seraph.zion.signal.database.AttachmentTransformer
import seraph.zion.signal.database.DatabaseMonitor
import seraph.zion.signal.database.GV2Transformer
import seraph.zion.signal.database.GV2UpdateTransformer
import seraph.zion.signal.database.IsStoryTransformer
import seraph.zion.signal.database.JobDatabase
import seraph.zion.signal.database.KeyValueDatabase
import seraph.zion.signal.database.KyberKeyTransformer
import seraph.zion.signal.database.LocalMetricsDatabase
import seraph.zion.signal.database.LogDatabase
import seraph.zion.signal.database.MegaphoneDatabase
import seraph.zion.signal.database.MessageBitmaskColumnTransformer
import seraph.zion.signal.database.MessageRangesTransformer
import seraph.zion.signal.database.ProfileKeyCredentialTransformer
import seraph.zion.signal.database.QueryMonitor
import seraph.zion.signal.database.RecipientTransformer
import seraph.zion.signal.database.SignalDatabase
import seraph.zion.signal.database.TimestampTransformer
import seraph.zion.signal.keyvalue.SignalStore
import seraph.zion.signal.logging.PersistentLogger
import seraph.zion.signal.recipients.Recipient
import seraph.zion.signal.util.AppSignatureUtil
import seraph.zion.signal.util.FeatureFlags
import java.util.Locale

class SpinnerApplicationContext : ApplicationContext() {
  override fun onCreate() {
    super.onCreate()

    try {
      Class.forName("dalvik.system.CloseGuard")
        .getMethod("setEnabled", Boolean::class.javaPrimitiveType)
        .invoke(null, true)
    } catch (e: ReflectiveOperationException) {
      throw RuntimeException(e)
    }

    Spinner.init(
      this,
      mapOf(
        "Device" to { "${Build.MODEL} (Android ${Build.VERSION.RELEASE}, API ${Build.VERSION.SDK_INT})" },
        "Package" to { "$packageName (${AppSignatureUtil.getAppSignature(this)})" },
        "App Version" to { "${BuildConfig.VERSION_NAME} (${BuildConfig.CANONICAL_VERSION_CODE}, ${BuildConfig.GIT_HASH})" },
        "Profile Name" to { (if (SignalStore.account().isRegistered) Recipient.self().profileName.toString() else "none") },
        "E164" to { SignalStore.account().e164 ?: "none" },
        "ACI" to { SignalStore.account().aci?.toString() ?: "none" },
        "PNI" to { SignalStore.account().pni?.toString() ?: "none" },
        Spinner.KEY_ENVIRONMENT to { BuildConfig.FLAVOR_environment.uppercase(Locale.US) }
      ),
      linkedMapOf(
        "signal" to DatabaseConfig(
          db = { SignalDatabase.rawDatabase },
          columnTransformers = listOf(
            MessageBitmaskColumnTransformer,
            GV2Transformer,
            GV2UpdateTransformer,
            IsStoryTransformer,
            TimestampTransformer,
            ProfileKeyCredentialTransformer,
            MessageRangesTransformer,
            KyberKeyTransformer,
            RecipientTransformer,
            AttachmentTransformer
          )
        ),
        "jobmanager" to DatabaseConfig(db = { JobDatabase.getInstance(this).sqlCipherDatabase }, columnTransformers = listOf(TimestampTransformer)),
        "keyvalue" to DatabaseConfig(db = { KeyValueDatabase.getInstance(this).sqlCipherDatabase }),
        "megaphones" to DatabaseConfig(db = { MegaphoneDatabase.getInstance(this).sqlCipherDatabase }),
        "localmetrics" to DatabaseConfig(db = { LocalMetricsDatabase.getInstance(this).sqlCipherDatabase }),
        "logs" to DatabaseConfig(
          db = { LogDatabase.getInstance(this).sqlCipherDatabase },
          columnTransformers = listOf(TimestampTransformer)
        )
      ),
      linkedMapOf(
        StorageServicePlugin.PATH to StorageServicePlugin()
      )
    )

    Log.initialize({ FeatureFlags.internalUser() }, AndroidLogger(), PersistentLogger(this), SpinnerLogger())

    DatabaseMonitor.initialize(object : QueryMonitor {
      override fun onSql(sql: String, args: Array<Any>?) {
        Spinner.onSql("signal", sql, args)
      }

      override fun onQuery(distinct: Boolean, table: String, projection: Array<String>?, selection: String?, args: Array<Any>?, groupBy: String?, having: String?, orderBy: String?, limit: String?) {
        Spinner.onQuery("signal", distinct, table, projection, selection, args, groupBy, having, orderBy, limit)
      }

      override fun onDelete(table: String, selection: String?, args: Array<Any>?) {
        Spinner.onDelete("signal", table, selection, args)
      }

      override fun onUpdate(table: String, values: ContentValues, selection: String?, args: Array<Any>?) {
        Spinner.onUpdate("signal", table, values, selection, args)
      }
    })
  }
}
