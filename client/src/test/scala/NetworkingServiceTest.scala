import org.junit.{Test, Assert}
import notemaker.client._

/**
 * Created by blueeyedhush on 12/17/14.
 */
class NetworkingServiceTest {
  @Test
  def isOrdinaryMessageDecodingWorking() = {
    val Mesg : String = "{\"mtype\":\"NodeCreated\",\"content\":{\"type\":\"NodeCreatedContent\",\"x\":1,\"y\":2}}"
    val List : List[GenericMessage] = MessageDispatcher.decodeMessage(Mesg)
    val firstExpected = new GenericMessage(mtype = "NodeCreated", content = new NodeCreatedContent(1,2))
    Assert.assertTrue(List.contains(firstExpected))
  }


  @Test
  def isContainerMessageDecodingWorking() = {
    val Mesg : String = "{\"mtype\":\"Container\",\"content\":{\"type\":\"ContainerContent\",\"mlist\":[{\"mtype\":\"NodeCreated\",\"content\":{\"type\":\"NodeCreatedContent\",\"x\":1,\"y\":2}},{\"mtype\":\"NodeCreated\",\"content\":{\"type\":\"NodeCreatedContent\",\"x\":3,\"y\":4}}]}}"
    //val Mesg : String = "{\"mtype\":\"Container\",\"content\":{\"type\":\"ContainerContent\",\"mlist\":[{\"mtype\":\"NodeCreated\",\"content\":{\"type\":\"NodeCreatedContent\",\"x\":1,\"y\":2}}]}}"
    val List : List[GenericMessage] = MessageDispatcher.decodeMessage(Mesg)
    val firstExpected = new GenericMessage(mtype = "NodeCreated", content = new NodeCreatedContent(1,2))
    val secondExpected = new GenericMessage(mtype = "NodeCreated", content = new NodeCreatedContent(3,4))
    Assert.assertTrue(List.contains(firstExpected))
    Assert.assertTrue(List.contains(secondExpected))
  }

}

