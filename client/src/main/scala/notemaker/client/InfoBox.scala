package notemaker.client

import javafx.geometry.VPos
import scalafx.Includes._
import scalafx.scene.Group
import scalafx.scene.control.TextArea
import scalafx.scene.effect.DropShadow
import scalafx.scene.input.{KeyEvent, MouseEvent}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import scalafx.scene.text.{TextAlignment, Text}

/**
 * Created by kuba on 10.01.15.
 */
class InfoBox(val node : Node, var x1 : Double, var x2 : Double) extends Group {
  var text : TextArea = null
  var rectangle : Rectangle = null
  this.setLayoutX(x1.toInt)
  this.setLayoutY(x2.toInt)

  var tempX: Int = 0
  var tempY: Int = 0
  var savedX: Int = 0
  var savedY: Int = 0

    this.text = new TextArea(){
      onKeyPressed = (event : KeyEvent) => JfxWorksheet.handleKey(event.getCode)
      onMouseClicked = (event: MouseEvent) => handleClick
    }

  def handleClick = JfxWorksheet.setFocus(this)

  text.setPrefSize(200, 200)
  text.setLayoutY(20)
  text.setMaxWidth(600)
  text.setWrapText(true)
  text.setStyle("-fx-border-color: transparent; -fx-border-width: 0; "
    + "-fx-border-radius: 0; -fx-focus-color: transparent;");

    this.rectangle = new Rectangle(){
    }
    rectangle.width = text.getPrefWidth
    rectangle.height = text.getPrefHeight + 20
    rectangle.setFill(Color.LightGrey)
    val background = new DropShadow(){
        color = Color.LightGrey
        width = rectangle.getWidth + 2
        height = rectangle.getHeight + 2
        offsetX = -1
        offsetY = -1
    }

  this.getChildren().addAll(rectangle, text)

  onMousePressed = (event : MouseEvent) => {
      JfxWorksheet.setFocus(this)
      savedX = this.getLayoutX.toInt
      savedY = this.getLayoutY.toInt
      tempX = event.getX.toInt
      tempY = event.getY.toInt
      text.effect = background
  }
  onMouseReleased = (event : MouseEvent) => {
      text.effect = null
      if (JfxWorksheet.checkCollisions(this)) {
          this.setLayoutX(savedX)
          this.setLayoutY(savedY)
      } else {
        // @ToDo: Refactor
        if(this.getLayoutX.toInt != savedX || this.getLayoutY.toInt != savedY) {
          NodeManager.moveNode(node.id, this.getLayoutX.toInt, this.getLayoutY.toInt)
        }
      }
  }
  onMouseDragged = (event: MouseEvent) => {
      if(JfxWorksheet.checkCollisions(this))
          background.color = Color.Red
      else
          background.color = Color.Grey
      this.setLayoutX(this.getLayoutX.toInt + event.getX.toInt - tempX)
      this.setLayoutY(this.getLayoutY.toInt + event.getY.toInt - tempY)
//      NodeManager.moveNode(node.id, this.getLayoutX.toInt, this.getLayoutY.toInt)
  }

  /* Member functions */

  def move(newX : Double, newY : Double) : Unit = {
    this.setLayoutX(newX)
    this.setLayoutY(newY)
  }
  def setText(message: String) : Unit = {
    println("setting text - dupa testing")
    this.text.setText(message)
  }
}