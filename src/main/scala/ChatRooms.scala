import ChatRooms.{CreateChatRoom, GetChatRooms, ResponseChatRoom}
import akka.actor.{Actor, ActorRef, Props}
import akka.actor.Actor.Receive

class ChatRooms extends Actor {
  override def receive: Receive = {
    case CreateChatRoom(name) =>
      val chatRoom = context.child(name).getOrElse( context.actorOf(Props[ChatRoom], name) )
      sender() ! ResponseChatRoom(chatRoom)
    case GetChatRooms() =>

  }
}

object ChatRooms {
  case class CreateChatRoom(name:String)
  case class ResponseChatRoom(actor:ActorRef)
  case class GetChatRooms()
}