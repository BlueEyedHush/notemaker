package notemaker.client

/**
 * Created by blueeyedhush on 12/1/14.
 */

import java.util.logging.Logger
import javafx.application.Application

import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.scene.input.KeyEvent


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

    val scene = new Scene() {
      root = JfxWorksheet
      onKeyPressed = (event : KeyEvent) => {
        JfxWorksheet.handleKey(event.getCode)
        if(event.getCode.toString == "A") JfxWorksheet.testIt
      }
   }

    stage.setScene(scene)
    stage.show()
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


