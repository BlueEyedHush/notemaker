/**
 * Created by blueeyedhush on 12/1/14.
 */

import org.json4s.DefaultFormats
import org.junit.{BeforeClass, Test, Assert}
import notemaker.client._

object ServerConnectionTest {
  var scon : ServerConnection = null

  @BeforeClass
  def create() = {
    Configuration.load()
    scon = new ServerConnection(Configuration.config.getString("networking.serverip"),
    Configuration.config.getInt("networking.port"))
  }
}

class ServerConnectionTest {
  @Test
  def canClientCommunicateWithServer() = {
    ServerConnectionTest.scon.send("{\"mtype\":\"Test\",\"content\":{}}")
    Assert.assertNotEquals(ServerConnectionTest.scon.receiveWait(5000), null)
  }

  @Test
  def isCaseClassToJsonConvWorking() = {
    implicit val format = DefaultFormats
    val msgObj = new GenericMessage(mtype = "NodeCreated", content = new NodeCreatedContent(id = 0, x = 1445, y= -467,"test"))
    val json = org.json4s.native.Serialization.write(msgObj)
    Assert.assertEquals("{\"mtype\":\"NodeCreated\",\"content\":{\"id\":0,\"x\":1445,\"y\":-467,\"text\":\"test\"}}", json)
  }
}
