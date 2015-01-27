package notemaker.client

/**
 * Created by blueeyedhush on 12/1/14.
 */

import java.util.logging.Logger
import javafx.application.Application

import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.scene.control.ScrollPane
import scalafx.scene.input.{MouseEvent, KeyEvent}
import scalafx.scene.layout.Pane

object Core {
  def initialize() : Unit = {
    Configuration.load()
    val serverip : String = Configuration.config.getString("networking.serverip")
    val port : Int = Configuration.config.getInt("networking.port")

    NetworkingService.connect(serverip, port)
    NetworkingService.dispatchers().add(new LoggingDispatcher)
    IdManager.init()
    NodeManager.init()
    ()
  }

  def cleanup() : Unit = {
    NetworkingService.disconnect()
    ()
  }
}

object NotemakerApp {
  /*
  use this one for logging instead of System.out.prinln
  @ToDo: Probably should be moved to Core. And then GUI can use that or create one for itself
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


//    StackPane layout = new StackPane();
//    layout.getChildren().setAll(
//      new ImageView(backgroundImage),
//      createKillButton()
//    );

        val scene = new Scene() {
      root = JfxWorksheet
      onKeyPressed = (event: KeyEvent) => JfxWorksheet.handleKey(event.getCode)
    }

//    val scrollPane = createScrollPane(JfxWorksheet)
//    scrollPane.onMouseClicked = (event: MouseEvent) => {
//      println("Scrollpane got mouse")
//      JfxWorksheet.handleMouse(event)
//    }
//    val scene = new Scene(scrollPane)
    scene.onKeyPressed = (event: KeyEvent) => JfxWorksheet.handleKey(event.getCode)
    scene.onMouseClicked = (event: MouseEvent) => JfxWorksheet.handleMouse(event)
    stage.setScene(scene)
    stage.show()
//    scrollPane.prefWidthProperty().bind(scene.widthProperty())
//    scrollPane.prefHeightProperty().bind(scene.heightProperty())
//    scrollPane.setHvalue(scrollPane.getHmin() + (scrollPane.getHmax() - scrollPane.getHmin())/2)
//    scrollPane.setVvalue(scrollPane.getVmin() + (scrollPane.getVmax() - scrollPane.getVmin())/2)

    Core.initialize()
  }

  @Override
  override def stop() : Unit = {
    Core.cleanup()
  }

  private def createScrollPane(layout: Pane): ScrollPane = {
    var scroll: ScrollPane = new ScrollPane()
    scroll = new ScrollPane();
    scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
    scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
    scroll.setPannable(true);
    scroll.setPrefSize(800, 600);
    scroll.setContent(layout);
    scroll
  }
}


