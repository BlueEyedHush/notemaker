package notemaker.client

/**
 * Created by blueeyedhush on 12/1/14.
 */

import java.util.logging.Logger

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.ScrollPane
import scalafx.scene.layout.StackPane
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle

object Application extends JFXApp {
  val logger : java.util.logging.Logger = Logger.getLogger("Global")

  logger.info("Application started")
  Configuration.load()
  SessionManager.connect(Configuration.config.getString("networking.serverip"),
    Configuration.config.getInt("networking.port"))

  NodeManager.createNode(new Node(1445, -4673))

  stage = new PrimaryStage {
    title = "Simple ScalaFX App"
    scene = new Scene() {
      root = new StackPane {
        content = new ScrollPane() {
          padding = Insets(20)
          pannable = true
          content = new Rectangle() {
            width = 200
            height = 200
            fill = Color.DEEPSKYBLUE
          }
        }
      }
    }
  }

  SessionManager.disconnect()
}
