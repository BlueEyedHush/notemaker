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
    ServerConnectionTest.scon.send("{\"mtype\":\"Test\", \"content\":{}}")
    Assert.assertEquals(ServerConnectionTest.scon.receiveWait(5000), "{\"mtype\":\"Test\", \"content\":{}}")
  }

  @Test
  def isCaseClassToJsonConvWorking() = {
    implicit val format = DefaultFormats
    val msgObj = new GenericMessage(mtype = "NodeCreated", content = new NodeCreatedContent(x = 1445, y= -467))
    val json = org.json4s.native.Serialization.write(msgObj)
    Assert.assertEquals("{\"mtype\":\"NodeCreated\",\"content\":{\"x\":1445,\"y\":-467}}", json)
  }
}
