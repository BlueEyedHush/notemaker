package notemaker.client

import scalafx.Includes._
import javafx.event
import javafx.scene.input.MouseEvent
import scalafx.scene.layout.Pane
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle

/**
 * Created by blueeyedhush on 12/23/14.
 */
object JfxWorksheet extends Pane {
  var sequence: Seq[Rectangle] = Seq()

  NodeManager.nodeListener = (n : Node) => {
    JfxWorksheet.createNode(n.x.toDouble, n.y.toDouble)
    ()
  }

  def createNode(x1 : Double, x2 : Double) = {
    val node = new JfxNode(x1, x2)
    sequence = sequence :+ node
    content.add(node)
  }

  onMouseClicked() = new event.EventHandler[MouseEvent] {
    override def handle( event: MouseEvent ): Unit ={
      if (event.getClickCount == 2) {
        val x = event.getX
        val y = event.getY
        NodeManager.createNode(new Node(x.toInt, y.toInt))
        //createNode(x,y) - callback will be called and from there GUI node representation will be created
        ()
      }
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
  onMousePressed = new event.EventHandler[MouseEvent] {
    // Rectangle focus handler
    override def handle(event: MouseEvent): Unit = {
      fill = Color.Black
      tempX = event.getX.toInt - x.toInt
      tempY = event.getY.toInt - y.toInt
    }
  }
  onMouseReleased = new event.EventHandler[MouseEvent] {
    override def handle(event: MouseEvent): Unit ={
      fill = Color.WhiteSmoke
    }
  }
  onMouseDragged = new event.EventHandler[MouseEvent] {
    override def handle(event: MouseEvent): Unit = {
      //println(tempX.toString + " " + tempY.toString)
      x = event.getX.toInt - tempX
      y = event.getY.toInt - tempY
    }
  }
}
