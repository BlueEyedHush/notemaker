/**
 * Created by blueeyedhush on 12/6/14.
 */

import org.junit.{AfterClass, BeforeClass, Test, Assert}
import notemaker.client._

object MessageDispatcherTest {
  @BeforeClass
  def init() : Unit = {
    Configuration.load()
    NetworkingService.connect(Configuration.config.getString("networking.serverip"),
      Configuration.config.getInt("networking.port"))
  }

  @AfterClass
  def cleanup() : Unit = {
    NetworkingService.disconnect()
  }
}

class MessageDispatcherTest {
  @Test
  def isDispatcherFired() : Unit = {
    NetworkingService.dispatchers().add(new MessageDispatcher {
      override def dispatch(msg: String) : Unit = {
        Assert.assertEquals(msg, "{testresponse}")
      }
    })

    NetworkingService.send("{testquery}")
  }
}
