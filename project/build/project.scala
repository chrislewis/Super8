import sbt._

class Project(info: ProjectInfo) extends AppengineProject(info) with JRebel {
  val uf_version = "0.3.3"
  val h_version = "0.0.4-SNAPSHOT"
  
  lazy val lift_json = "net.liftweb" %% "lift-json" % "2.2-RC4"
  
  lazy val c_codec = "commons-codec" % "commons-codec" % "1.5"
  
  // unfiltered
  lazy val uff = "net.databinder" %% "unfiltered-filter" % uf_version
  lazy val ufj = "net.databinder" %% "unfiltered-jetty" % uf_version
  lazy val ufjs = "net.databinder" %% "unfiltered-json" % uf_version
  
  val h_datastore = "net.thegodcode" %% "highchair-datastore" % h_version
  val h_remote = "net.thegodcode" %% "highchair-remote" % h_version

  // testing
  lazy val uf_spec = "net.databinder" %% "unfiltered-spec" % uf_version % "test"
  lazy val specs = "org.scala-tools.testing" %% "specs" %"1.6.6" % "test"
}
