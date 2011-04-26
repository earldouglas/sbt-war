import sbt._

import Keys._
import Project.Initialize

object WebBuild extends Build {
  lazy val projects = Seq(root, web)
  
  lazy val root = Project("root", file(".")) aggregate(web) dependsOn(web)

  val generateJettyRun = TaskKey[Seq[File]]("generate-jetty-run")

  def generateJettyRunTask: Initialize[Task[Seq[File]]] = (scalaSource in Compile) map {
    (srcDir) =>
    Seq("6", "7").map{
      n =>
      val target = srcDir / ("LazyJettyRun" + n + ".scala")
      generateJettyRun(srcDir / "LazyJettyRun.scala.templ", target, n, srcDir / ("jetty" + n + ".imports"))
      target
    }
  }

  def generateJettyRun(in: Path, out: Path, version: String, importsPath: Path) {
    val template = IO.read(in asFile)
    val imports = IO.read(importsPath asFile)
    IO.write(out asFile, processJettyTemplate(template, version, imports))
  }
  def processJettyTemplate(template: String, version: String, imports: String): String =
    template.replaceAll("""\Q${jetty.version}\E""", version).replaceAll("""\Q${jetty.imports}\E""", imports)
  
  lazy val web = Project("web", file("web")) settings(
    generateJettyRun <<= generateJettyRunTask,
    sources in Compile <<= (generateJettyRun, sources in Compile) map {(generated, sources) => (sources ++ generated) distinct}
  )
}
