package notemaker.client

import javafx.geometry.VPos
import scalafx.Includes._
import scalafx.scene.Group
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

    var tempX: Int = 0
    var tempY: Int = 0
    var savedX: Int = 0
    var savedY: Int = 0
    onMousePressed = (event : MouseEvent) => {
        savedX = this.getLayoutX.toInt
        savedY = this.getLayoutY.toInt
        tempX = event.getX.toInt
        tempY = event.getY.toInt
    }
    onMouseReleased = (event : MouseEvent) => {

    }
    onMouseDragged = (event: MouseEvent) => {
        this.setLayoutX(this.getLayoutX.toInt + event.getX.toInt - tempX)
        this.setLayoutY(this.getLayoutY.toInt + event.getY.toInt - tempY)
    }


    this.napis = new Text("Hello world")
    napis.setTextAlignment(TextAlignment.Left)
    napis.setFill(Color.Black)
    napis.setTextOrigin(VPos.TOP)

    this.rectangle = new Rectangle()
    rectangle.width = x1
    rectangle.height = x2
    rectangle.setFill(Color.Yellow)

    this.getChildren().addAll(rectangle, napis)


}
