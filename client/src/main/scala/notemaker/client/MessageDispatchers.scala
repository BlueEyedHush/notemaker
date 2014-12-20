package notemaker.client

import javafx.application.Platform

import org.json4s.{ShortTypeHints, DefaultFormats}

/**
 * Created by blueeyedhush on 12/6/14.
 */

/*
  WARNING!

  All dispatchers are executed on Reciever thread, not JavaFX Application Thread
 */

object MessageDispatcher {
  val customFormat : CustomFormat = new CustomFormat()

  /*IntelliJ highlists as an error, but it is correct*/
  protected class CustomFormat extends DefaultFormats {
    override val typeHints = new ShortTypeHints(List(
      classOf[NodeCreatedContent], classOf[ContainerContent]
    ))
    override val typeHintFieldName = "type"
  }

  def executeOnJavaFXThread(code: ()=> Unit) : Unit = {
    Platform.runLater(new Runnable {
      override def run(): Unit = code()
    })
  }

  def decodeMessage(Mesg : String) : List[GenericMessage] = {
    implicit val formats = MessageDispatcher.customFormat
    val list = org.json4s.native.Serialization.read[GenericMessage](Mesg)
    list.mtype match {
      case "Container" => list.content.asInstanceOf[ContainerContent].mlist
      case _ => {
        val coll = list :: Nil
        coll
      }
    }
  }
}

trait MessageDispatcher {
  def dispatch(msg: String) : Unit = {}
}

class LoggingDispatcher extends MessageDispatcher {
  override def dispatch(msg: String) = {
    //NotemakerApp.logger.info(msg)

    val mesgList = MessageDispatcher.decodeMessage(msg)

    for(m : GenericMessage <- mesgList) {
      m.mtype match {
        case "NodeCreated" => {
          val cont : NodeCreatedContent = m.content.asInstanceOf[NodeCreatedContent]
          NotemakerApp.logger.info(new StringBuilder("Received NodeCreatedContent: x = ").append(cont.x).append(", y = ").append(cont.y).toString())
        }
        case _ =>
          NotemakerApp.logger.info("Unknown message received")
      }
    }
  }
}

