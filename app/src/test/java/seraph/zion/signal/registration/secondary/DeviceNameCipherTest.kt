package seraph.zion.signal.registration.secondary

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test
import seraph.zion.signal.crypto.IdentityKeyUtil
import seraph.zion.signal.database.loaders.DeviceListLoader
import seraph.zion.signal.devicelist.protos.DeviceName
import java.nio.charset.Charset

class DeviceNameCipherTest {

  @Test
  fun encryptDeviceName() {
    val deviceName = "xXxCoolDeviceNamexXx"
    val identityKeyPair = IdentityKeyUtil.generateIdentityKeyPair()

    val encryptedDeviceName = DeviceNameCipher.encryptDeviceName(deviceName.toByteArray(Charset.forName("UTF-8")), identityKeyPair)

    val plaintext = DeviceListLoader.decryptName(DeviceName.ADAPTER.decode(encryptedDeviceName), identityKeyPair)

    assertThat(String(plaintext, Charset.forName("UTF-8")), `is`(deviceName))
  }
}
