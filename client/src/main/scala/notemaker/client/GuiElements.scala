package notemaker.client

import com.sun.xml.internal.bind.v2.TODO

import scalafx.Includes._
import scalafx.event
import scalafx.scene.effect.{DropShadow, Lighting, BoxBlur, Shadow}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.Pane
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle

/**
 * Created by blueeyedhush on 12/23/14.
 */
object JfxWorksheet extends Pane {
  var sequence: Seq[Rectangle] = Seq()
  content = sequence

  NodeManager.nodeListener = (n : Node) => {
    JfxWorksheet.createNode(n.x.toDouble, n.y.toDouble)
    ()
  }

  def clearNodes : Unit = {
    println("Deleting nodes")
    content.remove(0, sequence.length)
    sequence = sequence.filter(_ => false)
  }

  def createNode(x1 : Double, x2 : Double) = {
    val node = new JfxNode(x1, x2)
    sequence = node +: sequence
    content.add(node)
  }
  def refreshContent = {
    content.remove(0, sequence.length)
    for(elem <- sequence) content.add(elem)
  }
  def setFocus(jfxNode: JfxNode) = {
    val temp = sequence.indexOf(jfxNode)
    println("Element " + temp + ", at seq lenght " + sequence.length)
    jfxNode.fill = Color.LightGrey
    sequence = sequence.filter(!_.equals(jfxNode)) :+ jfxNode
    refreshContent
  }

  onMouseClicked = (event : MouseEvent) => {
    if(event.getClickCount == 2) {
      NodeManager.createNode(new Node(event.getX.toInt, event.getY.toInt))
      //createNode(x,y) - callback will be called and from there GUI node representation will be created
    }
  }
}

class JfxNode(x1 : Double, y1 : Double) extends Rectangle {
  width = 50
  height = 50
  x = x1.toInt
  y = y1.toInt
  fill = Color.WhiteSmoke
  var tempX: Int = 0
  var tempY: Int = 0
  onMousePressed = (event : MouseEvent) => {
    //Rectangle focus handler
    JfxWorksheet.setFocus(this)
//    fill = Color.LightGrey
    tempX = event.getX.toInt - x.toInt
    tempY = event.getY.toInt - y.toInt
    effect = new DropShadow() {
      color = Color.Grey
      height = 52
      width = 52
      offsetX = -1
      offsetY = -1
    }
  }
  onMouseReleased = (event : MouseEvent) => {
//      fill = Color.WhiteSmoke
      effect = null
  }
  onMouseDragged = (event: MouseEvent) => {
    x = event.getX.toInt - tempX
    y = event.getY.toInt - tempY
  }
}
