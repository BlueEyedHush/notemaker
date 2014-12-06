package notemaker.client

import java.util

/**
 * Created by blueeyedhush on 12/4/14.
 */

class Node(var x: Int, var y: Int) {}

object NodeManager {
  def createNode(n : Node) : Unit = {
    nodeList.add(n)
    val mesg : String = new StringBuilder("{nodeCreated x=").append(n.x).append(" y=").append(n.y).toString()
    NetworkingService.send(mesg)
  }

  def getNodes() = {
    nodeList
  }

  //def registerNodeCreatedCallback(cf: (Node) => Unit) = ???

  private val nodeList = new util.LinkedList[Node]()
}
