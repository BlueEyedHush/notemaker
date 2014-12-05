package notemaker.client

/**
 * Created by blueeyedhush on 12/1/14.
 */

import java.util.logging.Logger

object Application {
  val logger : java.util.logging.Logger = Logger.getLogger("Global")

  def main(args : Array[String]) = {
    logger.info("Application started")
    Configuration.load()
    SessionManager.connect(Configuration.config.getString("networking.serverip"),
      Configuration.config.getInt("networking.port"))

    NodeManager.createNode(new Node(1445, -4673))

    SessionManager.disconnect()
  }
}
