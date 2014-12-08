package notemaker.client

import java.io.{InputStreamReader, PrintWriter, BufferedReader}
import java.net.{SocketTimeoutException, Socket}
import java.util.concurrent.{BlockingQueue, ConcurrentLinkedQueue, TimeUnit, LinkedBlockingQueue}
import javafx.application.Platform

import org.json4s.DefaultFormats

/**
 * Created by blueeyedhush on 12/1/14.
 */
object ServerConnection {
  def open(ip: String, port: Int) : ServerConnection = {
    val s = new Socket(ip, port)

    new ServerConnection(
      new BufferedReader(new InputStreamReader(s.getInputStream)),
      new PrintWriter(s.getOutputStream, true),
      s
    )
  }
}

class ServerConnection(val incoming : BufferedReader, val outcoming : PrintWriter, val socket : Socket) {
  def close() = {
    socket.close()
  }

  def send(message: String) = {
    outcoming.print(message)
    outcoming.flush()
  }
}

/*
Sender and Receiver work in tandem
both Sender and Receiver must be initalized with the same ServerConnection
these classes does not close the ServerConnection
 */

/*
to send message just put it in message queue
 */

class Sender(private val conn : ServerConnection) extends javafx.concurrent.Task[Unit] {
  /* @ToDo:
  adding to this queue will block when the queue is full - that is *highly*
  undesired - UI should not block when queue is full.
   */
  val messageQueue : java.util.concurrent.BlockingQueue[String] = new LinkedBlockingQueue[String]()

  def call() : Unit = {
    var msg : String = null;

    while(!isCancelled()) {
      try {
        msg = messageQueue.poll(100, TimeUnit.MILLISECONDS)
        if(msg != null)
          conn.send(msg)
      } catch {
        case ie : InterruptedException => {}
      }
    }
  }
}

class Receiver(private val conn: ServerConnection) extends javafx.concurrent.Task[Unit] {
  import scala.collection.JavaConversions._
  val dispatchers = new ConcurrentLinkedQueue[MessageDispatcher]()

  def call() : Unit = {

    var msg : String = null
    conn.socket.setSoTimeout(500)
    while(!isCancelled()) {
      try {
        msg = conn.incoming.readLine()

        for(disp <- dispatchers) {
          disp.dispatch(msg)
        }
      } catch {
        case te : SocketTimeoutException => {}
      }
    }
  }
}

abstract class MessageContent() {}
case class GenericMessage(mtype : String, content : MessageContent) {}

object NetworkingService {

  private var conn : ServerConnection = null
  private var senderTask : Sender = null
  private var receiverTask : Receiver = null
  private var sender : Thread = null
  private var receiver : Thread = null

  def connect(serverip : String, port : Int) = {
    /*
    ServerConnection is constructed and belong to JavaFX Application Thread
    but it is used exclusively from Sender and Receiver
     */
    conn = ServerConnection.open(serverip, port)

    senderTask = new Sender(conn)
    receiverTask = new Receiver(conn)

    sender = new Thread(senderTask)
    sender.setDaemon(true)
    receiver = new Thread(receiverTask)
    receiver.setDaemon(true)

    receiver.start()
    sender.start()
  }

  def send(msg : String) : Unit = {
    senderTask.messageQueue.add(msg)
    NotemakerApp.logger.info("Sending: " + msg)
  }

  def send(msg : GenericMessage) : Unit = {
    implicit val formats = DefaultFormats
    val Json : String = org.json4s.native.Serialization.write(msg)
    this.send(Json)
  }

  def dispatchers() : ConcurrentLinkedQueue[MessageDispatcher] = {
    receiverTask match {
      case null => null
      case disp => disp.dispatchers
    }
  }

  def disconnect() = {
    conn.close()
  }
}