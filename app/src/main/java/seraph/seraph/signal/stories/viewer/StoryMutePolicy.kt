package seraph.zion.signal.stories.viewer

import seraph.zion.signal.dependencies.ApplicationDependencies
import seraph.zion.signal.util.AppForegroundObserver

/**
 * Stories are to start muted, and once unmuted, remain as such until the
 * user backgrounds the application.
 */
object StoryMutePolicy : AppForegroundObserver.Listener {
  var isContentMuted: Boolean = true

  fun initialize() {
    ApplicationDependencies.getAppForegroundObserver().addListener(this)
  }

  override fun onBackground() {
    isContentMuted = true
  }
}
