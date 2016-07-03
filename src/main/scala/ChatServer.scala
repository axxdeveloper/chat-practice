import akka.actor.{Actor, Props}

object ChatServer {
  val PATH = "socket-server"
  val PORT = 12345
}

/**
  * This class will listen port and forward new connection to [[ChatRoom]]
  */
class ChatServer extends Actor {

  import java.net.InetSocketAddress
  import akka.io.{IO, Tcp}
  import Tcp._
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress("0.0.0.0", ChatServer.PORT))

  val chatRoom = context.actorOf(Props[ChatRoom], ChatRoom.PATH)

  override def receive: Receive = {
    case Bound(localAddress) => println("bound address:" + localAddress)

    case CommandFailed(_: Bind) => {
      println("bind port fail")
      context stop self
    }

    case connected @ Connected(remote, local) =>
      chatRoom forward connected
  }

}

