package notemaker.client

import java.util

/**
 * Created by blueeyedhush on 12/4/14.
 */

class Node(val id : Int, var x: Int, var y: Int) {}
case class NodeCreatedContent(val id : Int, val x : Int, val y : Int) extends MessageContent {}

object NodeManager {
  def init() = {
    NetworkingService.dispatchers().add(new NodeCreatedDispatcher())
  }

  def createNode(posX : Int, posY : Int) : Unit = {
    val n = new Node(id = IdManager.getID(), x = posX, y = posY)
    val msgObj = new GenericMessage(mtype = "NodeCreated", content = new NodeCreatedContent(id = n.id, x = n.x, y = n.y))
    NetworkingService.send(msgObj)
    this.registerNode(n)
  }

  //@TODO checkit please!
  def removeNode(id : Int) : Unit = {
//    val msgObj = new GenericMessage(mtype = "NodeDeleted", content = new NodeDeletedContent())
//    NetworkingService.send(msgObj)
//    this.unregisterNode(n)
  }
  //@TODO end

  def getNodes() = {
    nodeList
  }

  var nodeListener : ((Node) => Unit) = null
  private def invokeListener(n : Node) = {
    nodeListener match {
      case null => ()
      case _ => nodeListener(n)
    }
  }

  private def registerNode(nc : NodeCreatedContent) : Unit = {
    this.registerNode(new Node(id = nc.id, x = nc.x, y = nc.y))
  }

  private def registerNode(n : Node) : Unit = {
    nodeList.add(n)
    invokeListener(n)
    ()
  }
  //@TODO checkit please!
  private def unregisterNode(n: Node) : Unit = {
//    nodeList.remove(n)
    ()
  }
  //@TODO end

  private val nodeList = new util.LinkedList[Node]()

  private class NodeCreatedDispatcher extends MessageDispatcher {
    override def dispatch(msg : String) : Unit = {
      val mlist = MessageDispatcher.decodeMessage(msg).filter(
        (m : GenericMessage) => {
          m.mtype match {
            case "NodeCreated" => true
            case _ => false
          }
        }
      )

      mlist.foreach(
        (m : GenericMessage) => {
          System.out.println("New node created!")
          MessageDispatcher.executeOnJavaFXThread(
            () => {
              NodeManager.registerNode(m.content.asInstanceOf[NodeCreatedContent])
            }
          )
        }
      )
    }
  }
}


