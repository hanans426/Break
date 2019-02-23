package com.neo.sk.front

/**
  * User: gaohan
  * Date: 2019/2/9
  * Time: 12:08
  */
object Routes {

  val base = "/break"
  object Index {
    val toLogin = base +"/toLogin"
    val toRegister = base + "/toRegister"
    val toSecretLogin = base + "/toSecretLogin"
  }

  object Register {
    val register = base + "/register"
  }

  object Login {
    val login= base + "/login"
    val loginOut = base + "/loginOut"
  }

  object Game {
    val baseUrl = base + "/gaming"
  }


}
