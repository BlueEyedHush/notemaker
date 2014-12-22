package notemaker.client

/**
 * Created by blueeyedhush on 12/1/14.
 */

import java.beans.EventHandler
import java.util.logging.Logger
import javafx.application.Application
import javafx.event
import javafx.scene.input.MouseEvent

import scala.collection.immutable.List
import scalafx.event.ActionEvent
import scalafx.scene.Scene
import scalafx.scene.control.{Button, ScrollPane}
import scalafx.scene.layout.VBox
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import scalafx.Includes._

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

    NodeManager.createNode(new Node(1445, -4673))
    NodeManager.createNode(new Node(1445, -467))
  }

  @Override
  override def start(stage : javafx.stage.Stage) : Unit = {
    stage.titleProperty().setValue("NoteMaker")
    stage.setWidth(600)
    stage.setHeight(400)

    //Sample items:
    val button = new Button(){
      id = "clickme"
      text() = "clicker"
      onAction = {
        e: ActionEvent => println(e + "Clicked")
      }
    }
    val rectangle = new Rectangle() {
      x = 10
      y = 20
      width = 200
      height = 200
      fill = Color.Yellow
    }
    var sequence = List()


    //Our inifinty sheet:
    val scrollPane = new ScrollPane{
      content = new VBox() {
        content = sequence
      }
      onMouseClicked() = new event.EventHandler[MouseEvent] {
        override def handle( event: MouseEvent ): Unit ={
          if (event.getClickCount == 2) {
            println("double clicked " + event.getX + " " +  event.getY)
          }
        }
      }
    }

    //this function is alfa
    def createInfobox(x : Int, y : Int) : Rectangle = {
      val rect = new Rectangle(){
        width = 200
        height = 200
        this.x = x.toDouble
        this.y = y.toDouble
        fill = Color.BLUE
      }
      rect
    }

    val scene = new Scene() {
      root = scrollPane
      }


    stage.setScene(scene)
    stage.show()
  }

  @Override
  override def stop() : Unit = {
    NetworkingService.disconnect()
  }
}


