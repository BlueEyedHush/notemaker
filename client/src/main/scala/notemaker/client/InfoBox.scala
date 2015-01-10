package notemaker.client

import javafx.geometry.VPos
import scalafx.Includes._
import scalafx.scene.Group
import scalafx.scene.effect.DropShadow
import scalafx.scene.input.MouseEvent
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import scalafx.scene.text.{TextAlignment, Text}

/**
 * Created by kuba on 10.01.15.
 */
class InfoBox(x1:Double, x2: Double) extends Group {
    var napis : Text = null
    var rectangle : Rectangle = null
    this.setLayoutX(x1.toInt)
    this.setLayoutY(x2.toInt)

    var tempX: Int = 0
    var tempY: Int = 0
    var savedX: Int = 0
    var savedY: Int = 0
    val background = new DropShadow(){
        color = Color.LightGrey
        width = 52
        height = 52
        offsetX = -1
        offsetY = -1
    }
    this.napis = new Text("Hello world")
    napis.setTextAlignment(TextAlignment.Left)
    napis.setFill(Color.Black)
    napis.setTextOrigin(VPos.TOP)

    this.rectangle = new Rectangle(){
    }
    rectangle.width = 50
    rectangle.height = 50
    rectangle.setFill(Color.LightGrey)


    onMousePressed = (event : MouseEvent) => {
        JfxWorksheet.setFocus(this)
        savedX = this.getLayoutX.toInt
        savedY = this.getLayoutY.toInt
        tempX = event.getX.toInt
        tempY = event.getY.toInt
        rectangle.effect = background
    }
    onMouseReleased = (event : MouseEvent) => {
        rectangle.effect = null
        if (JfxWorksheet.checkCollisions(this)) {
            this.setLayoutX(savedX)
            this.setLayoutY(savedY)
        }
    }
    onMouseDragged = (event: MouseEvent) => {
        if(JfxWorksheet.checkCollisions(this))
            background.color = Color.Red
        else
            background.color = Color.Grey
        this.setLayoutX(this.getLayoutX.toInt + event.getX.toInt - tempX)
        this.setLayoutY(this.getLayoutY.toInt + event.getY.toInt - tempY)
    }

    this.getChildren().addAll(rectangle, napis)
}