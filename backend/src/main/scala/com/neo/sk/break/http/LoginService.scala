package com.neo.sk.break.http

import com.neo.sk.utils.ServiceUtils
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import org.slf4j.LoggerFactory
import com.neo.sk.break.protocol.CommonErrorCode._
import com.neo.sk.break.UserProtocol._


/**
  * User: gaohan
  * Date: 2019/1/22
  * Time: 11:06 AM
  */
trait LoginService extends ServiceUtils with SessionBase{

  import io.circe._
  import io.circe.generic.auto._

  private val log = LoggerFactory.getLogger(getClass)

  private val login = (path("login") & pathEndOrSingleSlash & post){
   entity(as[Either[Error, UserLoginReq]]){
     case Left(e) =>
       log.warn(s"some error: $e")
       complete(parseError(e.getMessage))
     //case Right(req) =>
//       dealFutureResult(
//
//       )

   }
  }
  val loginRoutes: Route =
    pathPrefix("login"){
      login
    }

}
