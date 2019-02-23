package com.neo.sk.break.http

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.typed.javadsl.Behaviors.Supervise
import akka.actor.{ActorRef, ActorSystem, Scheduler}
import akka.http.javadsl.server.Route
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorAttributes, Materializer, OverflowStrategy, Supervision}
import akka.util.{ByteString, Timeout}
import org.seekloud.byteobject.MiddleBufferInJvm
import org.seekloud.byteobject.ByteObject._
import com.neo.sk.break.core.PlayGround

import scala.concurrent.ExecutionContextExecutor

/**
  * User: Taoz
  * Date: 8/26/2016
  * Time: 10:27 PM
  */
trait HttpService extends
  RegisterService with
  LoginService with
  ResourceService with
  GameService {


  implicit val system: ActorSystem

  implicit val executor: ExecutionContextExecutor

  implicit val materializer: Materializer

  implicit val timeout: Timeout

  implicit val scheduler: Scheduler

   override val idGenerator = new AtomicInteger(1000)


  val routes =
      pathPrefix("break"){
       // getFromResource("html/index.html")
        registerRoute ~
        loginRoute ~
        indexRoute ~
        gameRoute ~
        resourceRoutes
      }

}
