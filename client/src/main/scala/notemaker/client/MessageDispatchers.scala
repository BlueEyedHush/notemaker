package notemaker.client

import javafx.application.Platform

/**
 * Created by blueeyedhush on 12/6/14.
 */

/*
  WARNING!

  All dispatchers are executed on Reciever thread, not JavaFX Application Thread
 */

trait MessageDispatcher {
  def dispatch(msg: String) : Unit = {}

  protected def executeOnJavaFXThread(code: ()=> Unit) : Unit = {
    Platform.runLater(new Runnable {
      override def run(): Unit = code()
    })
  }
}

class LoggingDispatcher extends MessageDispatcher {
  override def dispatch(msg: String) = {
    NotemakerApp.logger.info(msg)
  }
}

