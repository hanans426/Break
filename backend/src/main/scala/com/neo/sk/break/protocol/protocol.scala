package com.neo.sk.break.protocol

import com.neo.sk.break.protocol.CommonErrorCode.ErrorRsp

/**
  * User: gaohan
  * Date: 2019/1/22
  * Time: 3:37 PM
  */
object protocol {

  trait Response
  case class CommonRsp(
    errCode: Int = 0,
    msg: String = "ok"
  ) extends Response

  //val parseError = ErrorRsp(100001, "parse error")

}
