package notemaker.client

import javafx.geometry.VPos

import scalafx.Includes._
import scalafx.scene.effect.DropShadow
import scalafx.scene.input.{KeyCode, MouseEvent}
import scalafx.scene.layout.{Region, Pane}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import scalafx.scene.Group
import scalafx.scene.text._




/**
 * Created by blueeyedhush on 12/23/14.
 */
object JfxWorksheet extends Pane {
  var sequence: Seq[InfoBox] = Seq()
  content = sequence

  NodeManager.nodeListener = (n : Node) => {
    JfxWorksheet.createNode(n, n.x.toDouble, n.y.toDouble)
    ()
  }
  NodeManager.nodeMovedListener = (n : Node) => {
    JfxWorksheet.moveNode(n.id, n.x.toDouble, n.y.toDouble)
    ()
  }

  def delNode : Unit = {
    println("Deleting nodes")
    content.remove(0, sequence.length)
    sequence = sequence.filter(_ != sequence.last)
    for(elem <- sequence) {
      content.add(elem)
    }
  }

  def createNode(n : Node, x1 : Double, x2 : Double) = {
    val node = new InfoBox(x1, x2)
    node.node = n
    sequence = node +: sequence
    content.add(node)
  }

  def moveNode(id : Int, newX : Double, newY : Double) : Unit = {
    var found : InfoBox = null
    var list = sequence.filter(
      (ib : InfoBox) => ib match {
        case i if i.node.id == id => {
          found = i
          false
        }
        case i if i.node.id != id => {
          true
        }
      }
    )

    if(found != null) {
      found.move(newX, newY)
    }
  }

  def refreshContent = {
    content.remove(0, sequence.length)
    for(elem <- sequence) {
      elem.rectangle.fill = Color.LightGrey
      content.add(elem)
    }
  }
  def setFocus(infoBox: InfoBox) = {
    val temp = sequence.indexOf(infoBox)
    sequence = sequence.filter(!_.equals(infoBox)) :+ infoBox
    refreshContent
    infoBox.rectangle.fill = Color.Grey
  }
  def checkCollisions(infoBox: InfoBox): Boolean = { // old JfxNode
    if(sequence.length == 1) false
    else {
      sequence.filter(!_.equals(infoBox)).map(e => (e.getLayoutX.toInt < (infoBox.getLayoutX.toInt + infoBox.rectangle.width.toInt)) && (
        (e.getLayoutX.toInt + e.rectangle.width.toInt) > infoBox.getLayoutX.toInt) &&
        (e.getLayoutY.toInt < (infoBox.getLayoutY.toInt + infoBox.rectangle.height.toInt) &&
          ((e.getLayoutY.toInt + e.rectangle.height.toInt > infoBox.getLayoutY.toInt)))).reduce(_ || _)
    }
  }

  def handleKey(key: KeyCode): Unit ={
    key.toString match{
      case "DELETE" => delNode
      case _ => ()
    }

  }

  onMouseClicked = (event : MouseEvent) => {
    if(event.getClickCount == 2) {
      NodeManager.createNode(event.getX.toInt - 25, event.getY.toInt - 25)
      //createNode(x,y) - callback will be called and from there GUI node representation will be created
    }
  }

}
