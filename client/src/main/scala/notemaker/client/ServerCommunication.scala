package notemaker.client

import java.io._
import java.net.{SocketTimeoutException, Socket}
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.concurrent.{BlockingQueue, ConcurrentLinkedQueue, TimeUnit, LinkedBlockingQueue}
import javafx.application.Platform

import org.json4s.DefaultFormats

/**
 * Created by blueeyedhush on 12/1/14.
 */

class ServerConnection(ip: String, port: Int) {
  private val socket : Socket = new Socket(ip, port)
  private val outcoming : OutputStream = socket.getOutputStream()
  private val incoming : InputStream = socket.getInputStream()

  def close() = {
    incoming.close()
    outcoming.close()
    socket.close()
  }

  private val bLength = new Array[Byte](4)
  private val bb = ByteBuffer.allocate(4)
  def send(message: String) = {
    val bArray : Array[Byte] = message.getBytes(Charset.forName("UTF-8"))
    val bLength = new Array[Byte](4)
    bb.putInt(bArray.length)
    bb.flip()
    bb.get(bLength)
    bb.flip() // prepare for next write
    outcoming.write(bLength)
    outcoming.write(bArray)
    outcoming.flush()
  }

  private var byteBuff : Array[Byte] = new Array[Byte](30)
  private def getByteBuffer(desiredSize : Int) : Array[Byte] = {
    if (desiredSize > byteBuff.length) {
      byteBuff = new Array[Byte](desiredSize)
    }
    byteBuff
  }

  private val bLength1 = new Array[Byte](4)
  private var len : Int = 0
  private var read : Int = 0
  def receiveWait(Timeout : Int) : String = {
    socket.setSoTimeout(Timeout)
    try {
      read = 0
      while(read < 4)
        read += incoming.read(bLength1, read-1, 4 - read)

      len = ByteBuffer.wrap(bLength1).getInt()

      val readBuffer = getByteBuffer(len)
      read = 0
      while(read < len)
        read += incoming.read(readBuffer, read-1, len - read)

      return new String(readBuffer, 0, len, "UTF-8")
    } catch {
      case te : SocketTimeoutException => null
    }
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
    while(!isCancelled()) {
      msg = conn.receiveWait(500)

      if(msg != null) {
        for (disp <- dispatchers) {
          disp.dispatch(msg)
        }
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
    conn = new ServerConnection(serverip, port)

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