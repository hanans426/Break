package com.neo.sk.break.shared

/**
  * User: gaohan
  * Date: 2019/2/9
  * Time: 21:10
  */
object ptcl {

  trait CommonRsp {
    val errCode: Int
    val msg: String
  }


  final case class ErrorRsp(
    errCode: Int,
    msg: String
  ) extends CommonRsp

  final case class SuccessRsp(
    errCode: Int = 0,
    msg: String = "ok"
  ) extends CommonRsp

  case class SecretIdRsp (
    secretId: String,
    errCode: Int,
    msg: String
  ) extends CommonRsp

}
