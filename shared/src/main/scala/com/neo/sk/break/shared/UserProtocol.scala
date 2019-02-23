package com.neo.sk.break.shared

/**
  * User: gaohan
  * Date: 2019/1/22
  * Time: 2:24 PM
  */
object UserProtocol {

  case class UserRegisterReq(
    account: String,
    nickName: String,
    password: String
  )

  case class UserLoginReq (
    account: String,
    password: String
  )

//  trait CommonRsp {
//    val errCode: Int
//    val msg: String
//  }
//
//  final case class ErrorRsp (
//    errCode: Int,
//    msg: String
//  ) extends CommonRsp
//
//  final case class SuccessRap (
//    errCode: Int,
//    msg: String
//  ) extends CommonRsp

}
