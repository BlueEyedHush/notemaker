package notemaker.client

/**
 * Created by blueeyedhush on 12/1/14.
 */

import java.beans.EventHandler
import java.util.logging.Logger
import javafx.application.Application
import javafx.event
import javafx.scene.input.MouseEvent

import com.oracle.jrockit.jfr.InvalidEventDefinitionException

import scala.collection.immutable.List
import scalafx.animation.{Timeline, AnimationTimer}
import scalafx.event.ActionEvent
import scalafx.scene.Scene
import scalafx.scene.control.{Button, ScrollPane}
import scalafx.scene.layout.VBox
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import scalafx.Includes._
import scalafx.scene.layout.Pane

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
      width = 200
      height = 200
      x = x1.toInt
      y = y1.toInt
      fill = Color.WhiteSmoke
    }
    rect
  }


  @Override
  override def start(stage : javafx.stage.Stage) : Unit = {
    stage.titleProperty().setValue("NoteMaker")
    stage.setWidth(600)
    stage.setHeight(400)

    //end of sample items
    var sequence: Seq[Rectangle] = Seq()

    //Our inifinty sheet:
    val scrollPane = new Pane{
      onMouseClicked() = new event.EventHandler[MouseEvent] {
        override def handle( event: MouseEvent ): Unit ={
          if (event.getClickCount == 2) {
//                       println("double clicked " + event.getX + " " +  event.getY)
//                      NodeManager.createNode(new Node(event.getX.toInt, event.getY.toInt))
            sequence = sequence :+ createInfobox(event.getX(), event.getY())
            content.removeAll()
            for(elem <- sequence: Seq[Rectangle]) content.add(elem)
          }
        }
      }
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


