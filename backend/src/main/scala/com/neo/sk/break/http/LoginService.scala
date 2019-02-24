package com.neo.sk.break.http

import java.util.concurrent.atomic.AtomicInteger

import com.neo.sk.utils.ServiceUtils
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.neo.sk.break.http.SessionBase.UserSession
import com.neo.sk.break.models.Dao.UserDao
import org.slf4j.LoggerFactory
import com.neo.sk.break.shared.UserProtocol._
import com.neo.sk.break.shared.ptcl._
import com.neo.sk.break.protocol.protocol._

import scala.concurrent.Future
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global


/**
  * User: gaohan
  * Date: 2019/1/22
  * Time: 11:06 AM
  */
trait LoginService extends ServiceUtils with SessionBase{

  import io.circe._
  import io.circe.generic.auto._

  private val log = LoggerFactory.getLogger(getClass)
  val idGenerator = new AtomicInteger(1000)

  val loginRoute = (path("login")  & post){
    pathEndOrSingleSlash {
      getFromResource("html/index.html")
    } ~
   entity(as[Either[Error, UserLoginReq]]){
     case Left(e) =>
       log.warn(s"some error: $e")
       complete(parseError)
     case Right(req) =>
       dealFutureResult(
         UserDao.getPasswordByName(req.account).map{pwdInDB=>
           if(pwdInDB.size == 0){
             complete(ErrorRsp(100002,"userName not exist"))//返回的是一个Future.route类型的值
           }else{
             val pass:String = pwdInDB(0)
             if(pass==req.password){
//               val session = SessionBase.UserSession(req.account,req.password,,1l).toSessionMap
//               addSession(session){
                 log.info(s"user ${req.account} login success")
                 complete(SuccessRsp())
               //}
             }else{
               complete(ErrorRsp(100103,"pwd is wrong"))
             }
           }
         }
       )
   }
}
  private def toLogin = (path("toLogin")& get){
    complete(SuccessRsp())
  }

  private def toRegister =(path("toRegister")& get){
    complete(SuccessRsp())
  }

   private def toSecretLogin = (path("toSecretLogin") & get){
     dealGetReq(
       Future{
         val a = s"player${idGenerator.getAndIncrement().toLong}"
       complete(SecretIdRsp(a, 0, ""))
       }
     )
   }


  val indexRoute =
    pathPrefix("index"){
      pathEndOrSingleSlash {
        getFromResource("html/index.html")
      } ~
      toLogin ~ toRegister ~ toSecretLogin
    }



}
