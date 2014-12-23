package notemaker.client

/**
 * Created by blueeyedhush on 12/1/14.
 */

import java.beans.EventHandler
import scalafx.Includes._
import java.util.logging.Logger
import javafx.application.Application
import javafx.event
import javafx.scene.input.{KeyEvent, MouseEvent}

import com.oracle.jrockit.jfr.InvalidEventDefinitionException

import scala.collection.immutable.List
import scalafx.animation.{Timeline, AnimationTimer}
import scalafx.event.ActionEvent
import scalafx.scene.Scene
import scalafx.scene.control.{TextField, TextArea, Button, ScrollPane}
import scalafx.scene.layout.VBox
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import scalafx.scene.layout.Pane
import scalafx.scene.text.Text


object NotemakerApp {
  /*
  use this one for logging instead of System.out.prinln
   */
  val logger : java.util.logging.Logger = Logger.getLogger("Global")

  def main(args: Array[String]) = {
    /*
    launch requires *class*, not object, therefore we need NotemakerApp class and object, instead of *just* object
    attempt to pass object to this function (or classOf[object]) would result in an error
     */
    Application.launch(classOf[NotemakerApp], args : _*)
  }
}

class NotemakerApp extends Application {
  @Override
  override def init() : Unit = {
    NotemakerApp.logger.info("Application started")

    Configuration.load()
    val serverip : String = Configuration.config.getString("networking.serverip")
    val port : Int = Configuration.config.getInt("networking.port")

    NetworkingService.connect(serverip, port)
    NetworkingService.dispatchers().add(new LoggingDispatcher)
    NodeManager.init()

    // Old version - is this useless?
    //    NodeManager.createNode(new Node(1445, -4673))
    //    NodeManager.createNode(new Node(1445, -467))
  }


  //this function is alfa
  def createInfobox(x1 : Double, y1 : Double) : Rectangle = {
    NodeManager.createNode(new Node(x1.toInt, y1.toInt))
    val rect = new Rectangle(){
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
          println(tempX.toString + " " + tempY.toString)
          x = event.getX.toInt - tempX
          y = event.getY.toInt - tempY
        }
      }
    }
    rect
  }


  @Override
  override def start(stage : javafx.stage.Stage) : Unit = {
    stage.titleProperty().setValue("NoteMaker")
    stage.setWidth(800)
    stage.setHeight(600)


    var sequence: Seq[Rectangle] = Seq()

    //Our inifinty sheet:
    val scrollPane = new Pane{

      onMouseClicked() = new event.EventHandler[MouseEvent] {
        override def handle( event: MouseEvent ): Unit ={
          if (event.getClickCount == 2) {
            val newBox = createInfobox(event.getX(), event.getY())
            sequence = sequence :+ newBox
            newBox.onMouseClicked()
            content.add(newBox)
          }
        }
      }
    }

    //Keyhandler - for testing reasons
    val scene = new Scene() {
      root = JfxWorksheet
      /*
      onKeyPressed() = new event.EventHandler[KeyEvent] {
        override def handle(event: KeyEvent): Unit = {
          println(event.getCode.toString)
          for(elem <- JfxWorksheet.sequence: Seq[Rectangle]) print(elem.getX.toString())
        }
      }
      */
    }

    stage.setScene(scene)
    stage.show()

  }

  @Override
  override def stop() : Unit = {
    NetworkingService.disconnect()
  }
}


