package notemaker.client

import scala.collection.immutable.List

/**
 * Created by blueeyedhush on 12/4/14.
 */

class Node(val id : Int, var x: Int, var y: Int, var Text : String = "HelloW!") {}
case class NodeCreatedContent(val id : Int, val x : Int, val y : Int) extends MessageContent
case class NodeMovedContent(val id : Int, val x : Int, val y : Int) extends MessageContent
case class NodeDeletedContent(val id: Int) extends MessageContent
case class NodeMessageContent(val id: Int, val Text: String) extends MessageContent

object NodeManager {
  def init() = {
    NetworkingService.dispatchers().add(new NodeCreatedDispatcher)
    NetworkingService.dispatchers().add(new NodeMovedDispatcher)
    NetworkingService.dispatchers().add(new NodeDeletedDispatcher)
  }

  def createNode(posX : Int, posY : Int) : Unit = {
    val n = new Node(id = IdManager.getID(), x = posX, y = posY)
    val msgObj = new GenericMessage(mtype = "NodeCreated", content = new NodeCreatedContent(id = n.id, x = n.x, y = n.y))
    NetworkingService.send(msgObj)
    this.registerNode(n)
  }

  def moveNode(id : Int, newX : Int, newY : Int) : Unit = {
    val cnt = new NodeMovedContent(id, newX, newY)
    val msgObj = new GenericMessage(mtype = "NodeMoved", content = cnt)
    NetworkingService.send(msgObj)
    registerNodeMoved(cnt)
  }


  def deleteNode(id : Int) : Unit = {
    val msgObj = new GenericMessage(mtype = "NodeDeleted", content = new NodeDeletedContent(id))
    NetworkingService.send(msgObj)
    registerNodeDeleted(id)
  }

  def sendText(id: Int, message: String) : Unit = {
    val msgObj = new GenericMessage(mtype = "TextSending", content = new NodeMessageContent(id, message))
    NetworkingService.send(msgObj)
    registerNodeMessage(id, message) //#TODO
  }



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

  var nodeMovedListener : ((Node) => Unit) = null
  private def invokeNodeMovedListener(n : Node) = {
    nodeMovedListener match {
      case null => ()
      case _ => nodeMovedListener(n)
    }
  }

  var nodeDeletedListener : ((Node) => Unit) = null
  private def invokeNodeDeletedListener(n : Node) = {
    nodeDeletedListener match {
      case null => ()
      case _ => nodeDeletedListener(n)
    }
  }

  var nodeUpdatedMessageListener : ((Node) => Unit) = null
  private def invokeUpdatedTextListener(n : Node) = {
    nodeUpdatedMessageListener match {
      case null => ()
      case _ => nodeUpdatedMessageListener(n)
    }
  }

  private def registerNode(nc : NodeCreatedContent) : Unit = {
    this.registerNode(new Node(id = nc.id, x = nc.x, y = nc.y))
  }

  private def registerNode(n : Node) : Unit = {
    nodeList = n :: nodeList
    invokeListener(n)
    ()
  }



  private def registerNodeMoved(nm: NodeMovedContent) : Unit = {
    NotemakerApp.logger.info("Node moved!")

    var found : Node = null
    val nlist = nodeList.filter(
      (n: Node) => n.id match {
        case i if i == nm.id => {
          found = n
          false
        }
        case i if i != nm.id => {
          true
        }
      }
    )

    if(found != null) {
      found.x = nm.x
      found.y = nm.y
      nodeList = found :: nlist

      invokeNodeMovedListener(found)
    } else {
      NotemakerApp.logger.warning("Tried to move node which ID is not known locally!")
    }
  }

  private def registerNodeDeleted(nid: Int) : Unit = {
    NotemakerApp.logger.info("Node deleted!")

    var found : Node = null
    val nlist = nodeList.filter(
      (n: Node) => n.id match {
        case i if i == nid => {
          found = n
          false
        }
        case i if i != nid => {
          true
        }
      }
    )

    if(found != null) {
      nodeList = nlist

      invokeNodeDeletedListener(found)
    } else {
      NotemakerApp.logger.warning("Tried to delete node which ID is not known locally!")
    }
  }

  private def registerNodeMessage(nid: Int, message: String) : Unit = {
    NotemakerApp.logger.info("Message sent!")

    var found : Node = null
    val nlist = nodeList.filter(
      (n: Node) => n.id match {
        case i if i == nid => {
          found = n
          n.Text = message
          true
        }
        case i if i != nid => {
          true
        }
      }
    )

    if(found != null) {
      nodeList = nlist

      invokeUpdatedTextListener(found)
    } else {
      NotemakerApp.logger.warning("Tried to update text on node which ID is not known locally!")
    }
  }

  private var nodeList = List[Node]()

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

  private class NodeMovedDispatcher extends MessageDispatcher {
    override def dispatch(msg : String) : Unit = {
      val mlist = MessageDispatcher.decodeMessage(msg).filter((m: GenericMessage) => m.mtype == "NodeMoved")
      val cont : List[NodeMovedContent] = mlist.map((m: GenericMessage) => m.content.asInstanceOf[NodeMovedContent])
      MessageDispatcher.executeOnJavaFXThread(() => {
        cont.foreach((c: NodeMovedContent) => NodeManager.registerNodeMoved(c)) // invoke logic function
      })
    }
  }

  private class NodeDeletedDispatcher extends MessageDispatcher {
    override def dispatch(msg : String) : Unit = {
      val mlist = MessageDispatcher.decodeMessage(msg).filter((m: GenericMessage) => m.mtype == "NodeDeleted")
      val cont : List[NodeDeletedContent] = mlist.map((m: GenericMessage) => m.content.asInstanceOf[NodeDeletedContent])
      MessageDispatcher.executeOnJavaFXThread(() => {
        cont.foreach((c: NodeDeletedContent) => NodeManager.registerNodeDeleted(c.id)) // invoke logic function
      })
    }
  }
}


