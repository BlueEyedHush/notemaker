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
