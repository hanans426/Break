package com.neo.sk.break.http

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{ActorSystem, Scheduler}
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.stream.{ActorAttributes, Materializer, Supervision}
import akka.stream.scaladsl.Flow
import akka.util.{ByteString, Timeout}
import com.neo.sk.break.core.PlayGround
import com.neo.sk.break.shared.Protocol.{GameMessage, TextInfo, UserAction}
import com.neo.sk.utils.ServiceUtils
import org.seekloud.byteobject.ByteObject._
import org.seekloud.byteobject.MiddleBufferInJvm
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContextExecutor

/**
  * User: gaohan
  * Date: 2019/2/14
  * Time: 21:57
  */
trait GameService extends ServiceUtils with SessionBase{

  implicit val system: ActorSystem

  implicit val executor: ExecutionContextExecutor

  implicit val materializer: Materializer

  implicit val timeout: Timeout

  implicit val scheduler: Scheduler

  lazy val playGround = PlayGround.create(system)

  private[this] val log = LoggerFactory.getLogger("GameService")

  val idGenerator = new AtomicInteger(1000)
  val gameRoute = {
    //println("gamingRoute")
    (path("gaming") & get) {
      getFromResource("html/index.html")
      parameter('id.as[String]) { id =>

        if(id.contains("player")) {
          handleWebSocketMessages(webSocketChatFlow(s"player${idGenerator.getAndIncrement().toLong}"))
        } else{
          handleWebSocketMessages(webSocketChatFlow(id))

        }
      }

    }
    }


    def webSocketChatFlow(id: String): Flow[Message, Message, Any] =
      Flow[Message]
        .collect {
          case TextMessage.Strict(msg) =>
            println("textMessage")
            log.info(s"msg from webSocket: $msg")
            TextInfo("", msg)

          case BinaryMessage.Strict(bmsg) =>
            val buffer = new MiddleBufferInJvm(bmsg.asByteBuffer)
           // println("bmsg")
            val msg =
              bytesDecode[UserAction](buffer) match {
                case Right(v) =>
                  v
                case Left(e) =>
                  println(s"decode error: ${e.message}")
                  TextInfo("", "decode error")
              }
            msg

        }
        .via(playGround.joinGame(id)
        .map {
          case message: GameMessage =>
            val sendBuffer = new MiddleBufferInJvm(4096000)
            val msg = ByteString(
              //encoded process
              message.fillMiddleBuffer(sendBuffer).result())
            //msgLength += msg.length
            BinaryMessage.Strict(msg)

        }.withAttributes(ActorAttributes.supervisionStrategy(decider)))

    val decider: Supervision.Decider = {
      e: Throwable =>
        e.printStackTrace()
        println(s"WS stream failed with $e")
        Supervision.Resume
    }


  }
