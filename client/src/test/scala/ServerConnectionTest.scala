/**
 * Created by blueeyedhush on 12/1/14.
 */
import org.junit.{BeforeClass, Test, Assert}
import notemaker.client._

object ServerConnectionTest {
  var scon : ServerConnection = null

  @BeforeClass
  def create() = {
    Configuration.load()
    scon = ServerConnection.open(Configuration.config.getString("networking.serverip"),
    Configuration.config.getInt("networking.port"))
  }
}

class ServerConnectionTest {
  @Test
  def canClientCommunicateWithServer() = {
    ServerConnectionTest.scon.outcoming.print("{testquery}")
    ServerConnectionTest.scon.outcoming.flush()
    ServerConnectionTest.scon.socket.setSoTimeout(5000)
    Assert.assertEquals(ServerConnectionTest.scon.incoming.readLine(), "{testresponse}")
  }
}
