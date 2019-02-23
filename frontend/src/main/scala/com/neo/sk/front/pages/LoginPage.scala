package com.neo.sk.front.pages

import org.scalajs.dom
import org.scalajs.dom.html.Input
import com.neo.sk.break.shared.UserProtocol._
import com.neo.sk.break.shared.ptcl._
import io.circe.generic.auto._
import io.circe.syntax._
import scala.concurrent.ExecutionContext.Implicits.global
import com.neo.sk.front.{Routes,Index}
import com.neo.sk.front.utils.{Http,JsFunc}
import com.neo.sk.front.style.LoginStyle._
import com.neo.sk.front.scalajs.NetGameHolder._



/**
  * User: gaohan
  * Date: 2019/1/23
  * Time: 3:57 PM
  */
object LoginPage extends Index{

  def register(): Unit = {
    dom.window.location.hash = s"#/register"
  }

  def login(): Unit = {
   val account = dom.window.document.getElementById("account").asInstanceOf[Input].value
   val password = dom.window.document.getElementById("password").asInstanceOf[Input].value
   //val nickName = dom.window.document.getElementById("nickName").asInstanceOf[Input].value
   val loginData = UserLoginReq(account, password).asJson.noSpaces

   Http.postJsonAndParse[SuccessRsp](Routes.Login.login, loginData).map {
     case Right(rsp) =>
       if(rsp.errCode == 0){
         JsFunc.alert("登录成功")
         joinGame(account)
        // dom.window.location.hash = s"#/gaming"
       }else if (rsp.errCode == 100001){
         JsFunc.alert("密码错误，")
       } else if(rsp.errCode == 100002){
         JsFunc.alert("该用户不存在，")
       } else{
         JsFunc.alert("登录失败，请稍后再试")
       }
     case Left(error) =>
       println(s"login error: ${error}")
       JsFunc.alert("登录失败，请输入正确的账号")
   }
  }

  def app: xml.Node  = {
    <div>
      <div>
        <div class={head.htmlClass}>Welcom To Brick </div>
      </div>
      <div>
        <div class={container.htmlClass}>账户：<input class={input.htmlClass} id="account"></input>
        </div>
        <div class={container.htmlClass}>
          <pre>密码：<input class={input.htmlClass} id="password" type ="password"></input>
          </pre>
        </div>
      </div>
      <button class={button.htmlClass} onclick={() => login()}>登录</button>
    </div>
  }

}
