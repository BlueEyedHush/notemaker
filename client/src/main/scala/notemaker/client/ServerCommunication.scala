package notemaker.client

import java.io.{InputStreamReader, PrintWriter, BufferedReader}
import java.net.Socket

/**
 * Created by blueeyedhush on 12/1/14.
 */
object ServerConnection {
  def open() : ServerConnection = {
    val s = new Socket(Configuration.config.getString("networking.serverip"), Configuration.config.getInt("networking.port"))

    new ServerConnection(
      new BufferedReader(new InputStreamReader(s.getInputStream)),
      new PrintWriter(s.getOutputStream, true),
      s
    )
  }
}

class ServerConnection(val incoming : BufferedReader, val outcoming : PrintWriter, val socket : Socket) {

}
