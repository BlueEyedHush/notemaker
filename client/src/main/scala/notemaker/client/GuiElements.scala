package notemaker.client

import javafx.geometry.VPos

import scalafx.Includes._
import scalafx.scene.effect.DropShadow
import scalafx.scene.input.{KeyCode, MouseEvent}
import scalafx.scene.layout.{StackPane, BackgroundImage, Region, Pane}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import scalafx.scene.Group
import scalafx.scene.text._


/**
 * Created by blueeyedhush on 12/23/14.
 */
object JfxWorksheet extends Pane {

//  this.setStyle("-fx-background-image: url('background.jpg')")
  var sequence: Seq[InfoBox] = Seq[InfoBox]()
  var focusedIB : InfoBox = null
  var supressFocusedIBList : Boolean = false
  content = sequence

  NodeManager.nodeCreatedListener.fun = (n : Node) => {
    JfxWorksheet.createNode(n, n.x.toDouble, n.y.toDouble)
    ()
  }
  NodeManager.nodeMovedListener.fun = (n : Node) => {
    if(!(supressFocusedIBList && focusedIB.node.id == n.id))
      JfxWorksheet.moveNode(n.id, n.x.toDouble, n.y.toDouble)
    ()
  }
  NodeManager.nodeDeletedListener.fun = (n : Node) => {
    if(!(supressFocusedIBList && focusedIB.node.id == n.id))
      JfxWorksheet.delNode(n.id)
    ()
  }
  NodeManager.nodeUpdatedMessageListener.fun = (n : Node) => {
    if(!(supressFocusedIBList && focusedIB.node.id == n.id))
      JfxWorksheet.updateInfobox(n.id, n.Text)
    ()
  }

  case class ExtractionResult(val found : InfoBox, val rest : Seq[InfoBox])
  def extractInfoboxById(id : Int) = {
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

    new ExtractionResult(found, list)
  }

  def delNode(nid : Int) : Unit = {
    println("Deleting nodes")
    val extrRes = extractInfoboxById(nid)

    sequence = extrRes.rest
    content.remove(extrRes.found)
  }

  def updateInfobox(id : Int, text: String) : Unit = {
    println("Updating node")
    var list = sequence.filter(
      (ib : InfoBox) => ib match {
        case i if i.node.id == id => {
          ib.setText(text)
          true
        }
        case i if i.node.id != id => {
          true
        }
      }
    )
  }

  def createNode(n : Node, x1 : Double, x2 : Double) = {
    val node = new InfoBox(n, x1, x2)
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

  def setFocus(infoBox: InfoBox) = {
    focusedIB = infoBox
    content.remove(infoBox)
    for(elem <- sequence) if (elem != infoBox) elem.rectangle.fill = Color.LightGrey
    content.add(infoBox)
    infoBox.rectangle.fill = Color.Grey
  }

  def insertKey(key: String): Unit ={
//    println(key)
    if(key == "ENTER") {
      NodeManager.sendText(focusedIB.node.id, focusedIB.text.getText) //#TODO
    }
    ()
  }

  def handleKey(key: KeyCode): Unit = {
//    println(key.toString)
    key.toString match {
      case "DELETE" if focusedIB != null =>
        delNode(focusedIB.node.id)
        NodeManager.deleteNode(focusedIB.node.id)
        focusedIB = null
      case a: String if focusedIB != null => insertKey(a)
      case _ => ()
    }
  }

  def handleMouse(event: MouseEvent): Unit = {
    if (event.getClickCount == 2 && event.getButton.toString == "PRIMARY") {
      NodeManager.createNode(event.getX.toInt - 25, event.getY.toInt - 25)
      //createNode(x,y) - callback will be called and from there GUI node representation will be created
    }
  }



//  onMouseClicked = (event : MouseEvent) => {
//    handleMouse(event)
//    println("Jfxworksheet got mouse")
//  }
//  onMouseDragged = (even/t : MouseEvent) => {
//    println(event.getX + " : " + event.getY)
//    JfxWorksheet.setLayoutX(this.getLayoutX.toInt + event.getX.toInt - tempX)
//    JfxWorksheet.setLayoutY(this.getLayoutY.toInt + event.getY.toInt - tempY)
//  }
}
