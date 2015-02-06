/*
package notemaker.client

//*
// * Created by kuba on 06.02.15.
import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props


class CommunicActor extends Actor {
  def receive = {
    case "stop" => {
      context.stop(self)
      ()
    }
    case time: Long => {
      Thread.sleep(time)
      println("woke and going to sleep")
//      NodeManager.sendText(JfxWorksheet.focusedIB.node.id, JfxWorksheet.focusedIB.text.getText)
      self ! time
      ()
    }
  }
}
*/
