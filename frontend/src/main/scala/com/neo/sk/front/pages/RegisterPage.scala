package com.neo.sk.front.pages

import org.scalajs.dom
import org.scalajs.dom.html.Input
import com.neo.sk.break.shared.ptcl._
import com.neo.sk.break.shared.UserProtocol._
import scala.concurrent.ExecutionContext.Implicits.global
import io.circe.generic.auto._
import io.circe.syntax._
import com.neo.sk.front.{Routes,Index}
import com.neo.sk.front.utils.{Http,JsFunc}
import com.neo.sk.front.style.RegisterStyle._

/**
  * User: gaohan
  * Date: 2019/1/23
  * Time: 3:58 PM
  */
object RegisterPage extends Index{

  def submit(): Unit = {
    val account = dom.window.document.getElementById("account").asInstanceOf[Input].value
    val password1 = dom.window.document.getElementById("password1").asInstanceOf[Input].value
    val password2 = dom.window.document.getElementById("password2").asInstanceOf[Input].value
    val nickName = ""
    if(password1 != password2){
      JsFunc.alert(s"两次密码不一致，请重新输入")
    }else{
      val registerData = UserRegisterReq(account, nickName, password1).asJson.noSpaces
      Http.postJsonAndParse[SuccessRsp](Routes.Register.register, registerData).map {
        case Right(rsp) =>
          if (rsp.errCode == 0) {
            JsFunc.alert("注册成功！")
            dom.window.location.hash = s"#/login"
          } else if (rsp.errCode == 100001) {
            JsFunc.alert("用户名已存在!")
          } else {
            println(rsp.errCode)
            JsFunc.alert("注册失败，请稍后再试！")
          }

        case Left(error) =>
          println(s"login error: $error")
          JsFunc.alert("网络异常,请重新输入！")
      }
    }
  }

  def app: xml.Node = {
    <div>
      <div>
        <div class={head.htmlClass}>注册新用户</div>
      </div>
      <div>
        <pre class={container.htmlClass}>账   户: <input class={input.htmlClass} id="account"></input>
        </pre>
        <div class={container.htmlClass}>
          <pre>密   码: <input class={input.htmlClass} id="password1" type="password"></input>
          </pre>
        </div>
        <div class={container.htmlClass}>
          <pre>确认密码: <input class={input.htmlClass} id="password2" type="password"></input>
          </pre>
        </div>
      </div>
      <button class={button.htmlClass} onclick={() => submit()}>注册</button>
    </div>
  }

}
