package notemaker.client

/**
 * Created by blueeyedhush on 12/4/14.
 */
object SessionManager {
  def connect(ip : String, port : Int) : ServerConnection = {
    connection = ServerConnection.open(ip, port)
    connection
  }

  def disconnect() : Unit = {
    connection match {
      case null => Unit
      case conn: ServerConnection => conn.close()
    }
  }

  var connection : ServerConnection = null
}
