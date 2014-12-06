package notemaker.client

/**
 * Created by blueeyedhush on 12/1/14.
 */

import java.util.logging.Logger

import javafx.application.Application
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.ScrollPane
import scalafx.scene.layout.StackPane
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle

object NotemakerApp {
  val logger : java.util.logging.Logger = Logger.getLogger("Global")

  def main(args: Array[String]) = {
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

    NodeManager.createNode(new Node(1445, -4673))
  }

  @Override
  override def start(stage : javafx.stage.Stage) : Unit = {
    stage.titleProperty().setValue("NoteMaker")

    val scene = new Scene() {
      root = new StackPane {
        content = new ScrollPane() {
          padding = Insets(20)
          pannable = true
          content = new Rectangle() {
            width = 200
            height = 200
            fill = Color.Yellow
          }
        }
      }
    }

    stage.setScene(scene)
    stage.show()
  }

  @Override
  override def stop() : Unit = {
    NetworkingService.disconnect()
  }
}


