package seraph.zion.signal

import org.signal.core.util.concurrent.SignalExecutors
import org.signal.core.util.logging.AndroidLogger
import org.signal.core.util.logging.Log
import org.signal.libsignal.protocol.logging.SignalProtocolLoggerProvider
import seraph.zion.signal.database.LogDatabase
import seraph.zion.signal.dependencies.ApplicationDependencies
import seraph.zion.signal.dependencies.ApplicationDependencyProvider
import seraph.zion.signal.dependencies.InstrumentationApplicationDependencyProvider
import seraph.zion.signal.logging.CustomSignalProtocolLogger
import seraph.zion.signal.logging.PersistentLogger
import seraph.zion.signal.testing.InMemoryLogger

/**
 * Application context for running instrumentation tests (aka androidTests).
 */
class SignalInstrumentationApplicationContext : ApplicationContext() {

  val inMemoryLogger: InMemoryLogger = InMemoryLogger()

  override fun initializeAppDependencies() {
    val default = ApplicationDependencyProvider(this)
    ApplicationDependencies.init(this, InstrumentationApplicationDependencyProvider(this, default))
    ApplicationDependencies.getDeadlockDetector().start()
  }

  override fun initializeLogging() {
    Log.initialize({ true }, AndroidLogger(), PersistentLogger(this), inMemoryLogger)

    SignalProtocolLoggerProvider.setProvider(CustomSignalProtocolLogger())

    SignalExecutors.UNBOUNDED.execute {
      Log.blockUntilAllWritesFinished()
      LogDatabase.getInstance(this).logs.trimToSize()
    }
  }
}
