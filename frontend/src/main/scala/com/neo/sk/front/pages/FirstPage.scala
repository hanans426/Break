package com.neo.sk.front.pages


import com.neo.sk.break.shared.ptcl.SuccessRsp
import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent.ExecutionContext.Implicits.global
import com.neo.sk.front.{Index, Routes}
import com.neo.sk.front.style.LoginStyle._
import com.neo.sk.front.utils.{Http,JsFunc}
import org.scalajs.dom
import com.neo.sk.front.scalajs.NetGameHolder._
import com.neo.sk.break.shared.ptcl._


/**
  * User: gaohan
  * Date: 2019/2/10
  * Time: 19:56
  */
object FirstPage extends Index {


  //val idGenerator = new AtomicInteger(1000)

  def toLogin(): Unit = {
//    Http.getAndParse[SuccessRsp](Routes.Index.toLogin).map {
//      case Right(rsp) =>
//        if (rsp.errCode == 0) {
//          JsFunc.alert("快登录吧")
//          dom.window.location.hash = s"#/login"
//        } else {
//          JsFunc.alert("出错了，请重新尝试")
//        }
//      case Left(error) =>
//           println(s"toLogin error: ${error}")
//    }
    dom.window.location.hash = s"#/login"
  }

  def toRegister(): Unit = {
    dom.window.location.hash = s"#/register"
  }

  def secretLogin():Unit = {

    Http.getAndParse[SecretIdRsp](Routes.Index.toSecretLogin).map{
      case Right(rsp) =>
        if(rsp.errCode == 0) {
          val secretId = rsp.secretId
          joinGame(secretId)
        }else {
          JsFunc.alert("匿名登录失败")
        }
      case Left(e) =>
        println(s"secretLogin error:${e}")
        JsFunc.alert("啊哦，匿名登录还有点问题，请您注册后登录哦")

    }
  }


  def app:xml.Node = {
    <div>
      <div>
        <div class={head.htmlClass}>Welcom To Brick </div>
      </div>
      <div>
        <button class={firstButton1.htmlClass} onclick={() => toLogin()}>已有账号，去登录</button>
      </div>
      <div>
        <button class={firstButton2.htmlClass} onclick={() => toRegister()}>木有账号，去注册</button>
      </div>
      <div>
        <button class={firstButton3.htmlClass} onclick={() => secretLogin()}>匿名登录</button>
      </div>
    </div>
  }

}
