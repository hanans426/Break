package com.neo.sk.break.models.Dao

import com.neo.sk.utils.DBUtil.db
import slick.jdbc.PostgresProfile.api._
import com.neo.sk.break.models.SlickTables._
/**
  * User: gaohan
  * Date: 2019/1/22
  * Time: 4:25 PM
  */
object UserDao {
  def getPasswordByName(account:String) = db.run{
    tUser.filter(_.account === account).map(_.password).result
  }

  def addUser(account:String, nickName: String, password: String) = db.run{
    tUser += rUser(account,nickName,password)
  }

}
