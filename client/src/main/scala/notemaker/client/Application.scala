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
  }
}
