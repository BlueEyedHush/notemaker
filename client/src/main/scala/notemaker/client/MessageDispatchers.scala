package notemaker.client

import javafx.application.Platform

import org.json4s.{ShortTypeHints, DefaultFormats}

/**
 * Created by blueeyedhush on 12/6/14.
 */

/*
  WARNING!

  All dispatchers are executed on Reciever thread, not JavaFX Application Thread
  Therefore, you cannot change Scene Graph directly from them - you must use
  executeOnJavaFXThread()
 */

object MessageDispatcher {
  val customFormat : CustomFormat = new CustomFormat()

  protected class CustomFormat extends DefaultFormats {
    override val typeHints = new ShortTypeHints(List(
      classOf[NodeCreatedContent], classOf[ContainerContent],
      classOf[IdPoolContent]
    ))
    override val typeHintFieldName = "type"
  }

  def executeOnJavaFXThread(code: ()=> Unit) : Unit = {
    Platform.runLater(new Runnable {
      override def run(): Unit = code()
    })
  }

  /*
  helper method to decode JSON string to list of appropriate case classes
  if only single message is received, one-element list is returned
  if container message is received, list of contained messages is received
   the same thing (list with single message) is returned for both single message
   and container message enclosing only one message
   */
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

/*
this trait is meant to be extended by every class which wants to listen to TCP messages
 */
trait MessageDispatcher {
  /*
  you are free to deal with msg however you want
  but decodeMessage() simplifies the process and is recommended for Notemaker client - Notemaker
  server communication
   */
  def dispatch(msg: String) : Unit = {}
}

/*
example dispatcher
 */
class LoggingDispatcher extends MessageDispatcher {
  override def dispatch(msg: String) = {
    //NotemakerApp.logger.info(msg)

    val mesgList = MessageDispatcher.decodeMessage(msg)

    for(m : GenericMessage <- mesgList) {
      m.mtype match {
        case "NodeCreated" => {
          val cont : NodeCreatedContent = m.content.asInstanceOf[NodeCreatedContent]
          NotemakerApp.logger.info(new StringBuilder("Received NodeCreatedContent: x = ").append(cont.x).append(", y = ").append(cont.y).append(", id = ").append(cont.id).toString())
        }
        case "IdPool" => {
          val cont = m.content.asInstanceOf[IdPoolContent]
          NotemakerApp.logger.info(new StringBuilder("Received new pool. First: ").append(cont.first).append(", last: ").append(cont.last).toString())
        }
        case _ =>
          NotemakerApp.logger.info("Unknown message received")
      }
    }
  }
}

