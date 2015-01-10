package notemaker.client

import com.typesafe.config.{ConfigFactory, Config}

/**
 * Created by blueeyedhush on 12/1/14.
 */
object Configuration {
  var config : Config = null;

  def load() = {
    config = ConfigFactory.load()
  }
}

/*
@ToDo: Make it non blocking
 */

case class IdPoolContent(val first : Int = 0, val last : Int = 0) extends MessageContent

object IdManager {
  def init() = {
    NetworkingService.dispatchers().add(new IDsReceiver())
  }

  def getID() =  {
    if(pool.left() <= 0) {
      requestPool()
    }

    // requestPool() ensures that pool is not null
    val id = pool.next()
    pool.advance()
    id
  }

  private def requestPool() = {
    pool = null
    NetworkingService.send(new GenericMessage(mtype = "IdPool", content = new IdPoolContent(0,0)))
    while(pool == null) {
      Thread.sleep(100)
      NotemakerApp.logger.info("Waiting for IDs...")
    }
  }

  private def registerPool(p : Pool): Unit = {
    pool = p
  }

  private class Pool(val first : Int, val last : Int) {
    def left() = {
      last - _next
    }

    def advance() = {
      _next += 1
    }

    def next() : Int = {
      _next
    }

    private var _next : Int = first + 1
  }

  private var pool : Pool = new Pool(0,0)

  private class IDsReceiver extends MessageDispatcher {
    override def dispatch(msg : String) = {
      val mlist : List[GenericMessage] = MessageDispatcher.decodeMessage(msg).filter( (m: GenericMessage) => m.mtype == "IdPool" )

      if(!mlist.isEmpty) {
        val content : IdPoolContent = mlist.head.content.asInstanceOf[IdPoolContent]
        /*
        @ToDo: We are modifying contents the other thread might be reading ATM. Probably some synchro/lock should be added
        (But that other thread reads variable in loop, so even on inconsistent read, it might read correctly the next time)
         */
        IdManager.registerPool(new Pool(content.first, content.last))
        //MessageDispatcher.executeOnJavaFXThread(() => IdManager.registerPool(new Pool(content.first, content.last)))
      }
    }
  }
}
