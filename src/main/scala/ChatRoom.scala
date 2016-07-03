import ChatRoom.ChatMessage
import akka.actor.{Actor, ActorRef, Props}
import akka.io.Tcp._
import akka.util.ByteString

object ChatRoom {
  val PATH = "chat-room"
  case class ChatMessage(from:String, message:String)
}

/** [[ChatRoom]] will create [[UserSession]] when receiving new connection and forward [[ChatMessage]] to them */
class ChatRoom extends Actor {

  override def receive: Receive = {
    case Connected(remote, local) =>
      println("new connnection. remote:" + remote + " local:" + local)
      val connection = sender()
      val userSession = context.actorOf(Props(classOf[UserSession],connection))
      connection ! Register(userSession)
    case chatMessage @ ChatMessage(name:String, message:String) =>
      println("chatRoom:" + chatMessage)
      context.children.foreach(_ forward chatMessage)
    case msg => println("chatroom receive unexpected message:" + msg)
  }

}

/**
  * [[UserSession]] will buffer received data from tcp connection and send [[ChatMessage]] to [[ChatRoom]].
  * [[UserSession]] will write message to remote client when receiving a [[ChatMessage]]
  * [[UserSession]] will close tcp connection and stop itself when receiving a "bye" command from remote client
  */
class UserSession(connection:ActorRef) extends Actor {
  import akka.io.Tcp.{PeerClosed, Received, Write}

  // FIXME should be a fixed size buffer, prevent out of memory problem
  var buffer = ""

  override def receive: Receive = {
    case ChatMessage(name:String, message:String) =>
      println("user received:" + message + ", connection:" + connection)
      connection ! Write(ByteString("\r\n" + message.trim))
    case Received(data) => {
      buffer += data.utf8String
      val isNewLine: Boolean = data.utf8String == "\n" || data.utf8String == "\r\n"
      if (isNewLine) {
        processBuffer()
      }
    }
    case PeerClosed => {
      println("connection closed")
      context stop self
    }
  }

  private def processBuffer(): Unit = {
    if ("bye" == buffer.trim.toLowerCase) {
      println("receive bye")
      sender() ! Close
      context stop self
    } else {
      context.parent ! ChatMessage(self.path.name, buffer)
      buffer = ""
    }
  }

}
