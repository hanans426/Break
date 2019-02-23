package com.neo.sk.break.http

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.neo.sk.break.models.Dao.UserDao
import com.neo.sk.utils.ServiceUtils
import org.slf4j.LoggerFactory
import com.neo.sk.break.shared.UserProtocol._
import com.neo.sk.break.protocol.protocol._
import com.neo.sk.break.shared.ptcl.{SuccessRsp,ErrorRsp}

import scala.concurrent.Future
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global

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

   val registerRoute = (path("register") & post) {
     pathEndOrSingleSlash {
       getFromResource("html/index.html")
     } ~
    entity(as[Either[Error, UserRegisterReq]]) {
      case Left(e) =>
        log.warn(s"some error: $e")
        complete(parseError)
      case Right(req) =>
        dealFutureResult(
          UserDao.getPasswordByName(req.account).map(pwdInDB =>
            if(pwdInDB.isEmpty){
              UserDao.addUser(req.account,req.nickName,req.password)
              complete(SuccessRsp())
            } else{
              complete(ErrorRsp(100001,"userName is exist"))//返回的是一个Future.route类型的值
            }

          )
        )
    }
  }

}