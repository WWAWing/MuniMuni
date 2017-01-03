
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.JSApp
import scala.util.{Failure, Success}

object Main extends JSApp {
  def main(): Unit = {
    MuniStages.load().onComplete {
      case Success(f) => f match {
        case Right(s) => s.foreach(stage => println(stage.stage))
        case Left(e) => println(e)
      }
      case Failure(e) => println(e)
    }
  }
}
