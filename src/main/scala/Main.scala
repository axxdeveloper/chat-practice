import akka.actor.{ActorSystem, Props}

/**
  * @author <a href="https://github.com/shooeugenesea">isaac</a>
  */
object Main {
  def main(args: Array[String]) {
    val system = ActorSystem()
    system.actorOf(Props[ChatServer], ChatServer.PATH)
  }
}
