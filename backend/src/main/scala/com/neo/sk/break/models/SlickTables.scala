package com.neo.sk.break.models

// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object SlickTables extends {
  val profile = slick.jdbc.PostgresProfile
} with SlickTables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait SlickTables {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = tUser.schema
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table tUser
   *  @param account Database column account SqlType(varchar)
   *  @param nickname Database column nickname SqlType(varchar)
   *  @param password Database column password SqlType(varchar) */
  case class rUser(account: String, nickname: String, password: String)
  /** GetResult implicit for fetching rUser objects using plain SQL queries */
  implicit def GetResultrUser(implicit e0: GR[String]): GR[rUser] = GR{
    prs => import prs._
    rUser.tupled((<<[String], <<[String], <<[String]))
  }
  /** Table description of table user. Objects of this class serve as prototypes for rows in queries. */
  class tUser(_tableTag: Tag) extends profile.api.Table[rUser](_tableTag, "user") {
    def * = (account, nickname, password) <> (rUser.tupled, rUser.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(account), Rep.Some(nickname), Rep.Some(password)).shaped.<>({r=>import r._; _1.map(_=> rUser.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column account SqlType(varchar) */
    val account: Rep[String] = column[String]("account")
    /** Database column nickname SqlType(varchar) */
    val nickname: Rep[String] = column[String]("nickname")
    /** Database column password SqlType(varchar) */
    val password: Rep[String] = column[String]("password")
  }
  /** Collection-like TableQuery object for table tUser */
  lazy val tUser = new TableQuery(tag => new tUser(tag))
}
