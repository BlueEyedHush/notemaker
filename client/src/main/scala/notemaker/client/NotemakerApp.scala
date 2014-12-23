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
  }

  @Override
  override def start(stage : javafx.stage.Stage) : Unit = {
    stage.titleProperty().setValue("NoteMaker")
    stage.setWidth(800)
    stage.setHeight(600)

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

    NodeManager.nodeListener = (n : Node) => {
        JfxWorksheet.createNode(n.x.toDouble, n.y.toDouble)
        ()
      }

    initializeLogic()
  }

  protected def initializeLogic() : Unit = {
    Configuration.load()
    val serverip : String = Configuration.config.getString("networking.serverip")
    val port : Int = Configuration.config.getInt("networking.port")

    NetworkingService.connect(serverip, port)
    NetworkingService.dispatchers().add(new LoggingDispatcher)
    NodeManager.init()
    ()
  }

  @Override
  override def stop() : Unit = {
    NetworkingService.disconnect()
  }
}


