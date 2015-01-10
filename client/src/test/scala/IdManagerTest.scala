/**
 * Created by blueeyedhush on 1/10/15.
 */

import javafx.application.Platform
import javafx.stage.Stage

import notemaker.client.{IdManager, Core, NotemakerApp}
import org.junit.{AfterClass, BeforeClass, Test, Assert}

object IdManagerTest {
  @BeforeClass
  def init() : Unit = {
    val FXLauncher = new Thread(new Runnable {
      override def run(): Unit = {
        javafx.application.Application.launch(classOf[JavaFXLauncher], "")
      }
    })

    FXLauncher.start()
    FXLauncher.join()
  }

  var id1 : Int = 0
  var id2 : Int = 0
  var id3 : Int = 0

  private class JavaFXLauncher extends javafx.application.Application {
    override def start(stage : Stage) : Unit = {
      Core.initialize()
      id1 = IdManager.getID()
      id2 = IdManager.getID()
      id3 = IdManager.getID()
      Core.cleanup()
      Platform.exit()
    }
  }
}

class IdManagerTest {
  @Test
  def canGetId() = {
    Assert.assertNotEquals(IdManagerTest.id1, IdManagerTest.id2)
    Assert.assertNotEquals(IdManagerTest.id2, IdManagerTest.id3)
    Assert.assertNotEquals(IdManagerTest.id1, IdManagerTest.id3)
  }
}
