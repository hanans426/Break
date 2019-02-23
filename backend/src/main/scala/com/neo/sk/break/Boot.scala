package com.neo.sk.break

import akka.actor.{ActorSystem, Scheduler}
import akka.actor.typed.ActorRef
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.util.Timeout
import akka.actor.typed.scaladsl.adapter._
import akka.dispatch.MessageDispatcher
import scala.util.{Failure,Success}
import scala.language.postfixOps
import com.neo.sk.break.http.HttpService

/**
  * User: Taoz
  * Date: 8/26/2016
  * Time: 10:25 PM
  */
object Boot extends HttpService {

  import concurrent.duration._
  import  com.neo.sk.break.common.AppSettings._

  override implicit val system: ActorSystem = ActorSystem("break", config)
  override implicit val executor: MessageDispatcher = system.dispatchers.lookup("akka.actor.my-blocking-dispatcher")
  override implicit val materializer: ActorMaterializer = ActorMaterializer()

  override implicit val scheduler: Scheduler = system.scheduler

  override implicit val timeout: Timeout = Timeout(20 seconds) // for actor asks

  val log: LoggingAdapter = Logging(system, getClass)
//  val delayer = system.spawn(Delayer.start, "Delayer")
//  val userManager: ActorRef[UserManager.Command] = system.spawn(UserManager.behaviors,"UserManager")
//  val roomManager: ActorRef[RoomManager.Command] = system.spawn(RoomManager.behaviors,"RoomManager")
//  val watchManager: ActorRef[WatcherManager.Command] = system.spawn(WatcherManager.behaviors, "WatchManager")
//  val authActor: ActorRef[AuthActor.Command] = system.spawn(AuthActor.behaviors, "AuthActor")
//  CountUtils.initCount()
  
	def main(args: Array[String]) {
    log.info("Starting.")
    val binding = Http().bindAndHandle(routes,httpInterface,httpPort)
    binding.onComplete{
      case Success(b) =>
        val localAddress = b.localAddress
        log.info(s"Server is listening on http://localhost:${localAddress.getPort}/break/index")
      case Failure(e) =>
        log.info(s"Binding failed with ${e.getMessage}")
    }
    log.info("Done.")
  }






}
