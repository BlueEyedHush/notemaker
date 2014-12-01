/**
 * Created by blueeyedhush on 12/1/14.
 */
import org.junit.{BeforeClass, Test, Assert}
import notemaker.client._

object ServerConnectionTest {
  var scon : ServerConnection = null

  @BeforeClass
  def create() = {
    scon = ServerConnection.open()
  }
}

class ServerConnectionTest {
  @Test
  def canClientCommunicateWithServer() = {
    ServerConnectionTest.scon.outcoming.print("{testquery}")
    Assert.assertEquals(ServerConnectionTest.scon.incoming.readLine(), "{testresponse}")
  }
}
