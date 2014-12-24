/**
 * Created by blueeyedhush on 12/1/14.
 */

import org.junit.{BeforeClass, Test, Assert}
import com.typesafe.config.ConfigFactory

object ConfigTest {
  var conf : com.typesafe.config.Config = null

  @BeforeClass
  def createConfig() = {
    conf = ConfigFactory.load()
  }
}

class ConfigTest {
  @Test
  def arePropertiesRetrieved() = {
    Assert.assertEquals(323, ConfigTest.conf.getInt("configtest.TestIntP"))
    Assert.assertEquals(1.0, ConfigTest.conf.getDouble("configtest.TestDoubleP"), 0.000001)
    Assert.assertEquals("Test", ConfigTest.conf.getString("configtest.TestStringP"))
  }
}
