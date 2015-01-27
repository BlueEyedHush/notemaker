import org.junit.{Test, Assert}
import notemaker.client._

/**
 * Created by blueeyedhush on 12/17/14.
 */
class NetworkingServiceTest {
  @Test
  def isOrdinaryMessageDecodingWorking() = {
    val Mesg : String = "{\"mtype\":\"NodeCreated\",\"content\":{\"type\":\"NodeCreatedContent\",\"id\":0,\"x\":1,\"y\":2,\"text\":\"test\"}}"
    val List : List[GenericMessage] = MessageDispatcher.decodeMessage(Mesg)
    val firstExpected = new GenericMessage(mtype = "NodeCreated", content = new NodeCreatedContent(0,1,2,"test"))
    Assert.assertTrue(List.contains(firstExpected))
  }


  @Test
  def isContainerMessageDecodingWorking() = {
    val Mesg : String = "{\"mtype\":\"Container\",\"content\":{\"type\":\"ContainerContent\",\"mlist\":[{\"mtype\":\"NodeCreated\",\"content\":{\"type\":\"NodeCreatedContent\",\"id\":0,\"x\":1,\"y\":2,\"text\":\"test\"}},{\"mtype\":\"NodeCreated\",\"content\":{\"type\":\"NodeCreatedContent\",\"id\":0,\"x\":3,\"y\":4,\"text\":\"test\"}}]}}"
    val List : List[GenericMessage] = MessageDispatcher.decodeMessage(Mesg)
    val firstExpected = new GenericMessage(mtype = "NodeCreated", content = new NodeCreatedContent(0,1,2,"test"))
    val secondExpected = new GenericMessage(mtype = "NodeCreated", content = new NodeCreatedContent(0,3,4,"test"))
    Assert.assertTrue(List.contains(firstExpected))
    Assert.assertTrue(List.contains(secondExpected))
  }

}

