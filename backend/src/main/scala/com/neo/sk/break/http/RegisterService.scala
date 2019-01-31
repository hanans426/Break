package com.neo.sk.break.http

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.neo.sk.break.protocol.CommonErrorCode.parseError
import com.neo.sk.utils.ServiceUtils
import org.slf4j.LoggerFactory
import com.neo.sk.break.UserProtocol._


import scala.concurrent.Future
import scala.language.postfixOps
/**
  * User: gaohan
  * Date: 2019/1/22
  * Time: 11:03 AM
  */
trait RegisterService extends ServiceUtils with SessionBase {

  import io.circe._

  import io.circe.generic.auto._


  implicit val system: ActorSystem

  //implicit val materialize: Materializer

  private[this] val log = LoggerFactory.getLogger("RegisterService")

  private val register = (path("register") & post) {
    entity(as[Either[Error, UserLoginReq]]) {
      case Left(e) =>
        log.warn(s"some error: $e")
        complete(parseError(e.getMessage))
      //case Right(req) =>
      //       dealFutureResult(

    }
  }


  val registerRoutes: Route =
    pathPrefix("register") {
      register
    }

  //  val linkRoute = (pathPrefix("link") & get) {
  //    playGameRoute ~ playGameClientRoute ~watchGameRoute ~ watchRecordRoute
  //  }

}