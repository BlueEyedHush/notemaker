package notemaker.client

import java.util

/**
 * Created by blueeyedhush on 12/4/14.
 */

class Node(var x: Int, var y: Int) {}
case class NodeCreatedContent(val x : Int, val y : Int) extends MessageContent {}

object NodeManager {
  def createNode(n : Node) : Unit = {
    nodeList.add(n)
    val msgObj = new GenericMessage(mtype = "NodeCreated", content = new NodeCreatedContent(x = n.x, y = n.y))
    NetworkingService.send(msgObj)
  }

  def getNodes() = {
    nodeList
  }

  private val nodeList = new util.LinkedList[Node]()
}
