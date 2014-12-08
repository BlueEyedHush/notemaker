package notemaker.client

import java.util

/**
 * Created by blueeyedhush on 12/4/14.
 */

class Node(var x: Int, var y: Int) {}
case class NodeCreatedContent(x : Int, y : Int) extends MessageContent {}

object NodeManager {
  def createNode(n : Node) : Unit = {
    nodeList.add(n)
    val msgObj = new GenericMessage(mtype = "NodeCreated", content = new NodeCreatedContent(x = n.x, y = n.y))
    //val mesg : String = new StringBuilder("{\"mtype\" : \"NodeCreated\", \"content\" : { \"x\" : ").append(n.x).append(", \"y\" : ").append(n.y).append("} }").toString()
    NetworkingService.send(msgObj)
  }

  def getNodes() = {
    nodeList
  }

  //def registerNodeCreatedCallback(cf: (Node) => Unit) = ???

  private val nodeList = new util.LinkedList[Node]()
}
