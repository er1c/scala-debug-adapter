package dap

//import bloop.data.{Platform, Project}
//import bloop.engine.State
//import bloop.engine.tasks.{RunMode, Tasks}
//import bloop.data.JdkConfig
//import bloop.testing.{LoggingEventHandler, TestInternals}
//import ch.epfl.scala.bsp.ScalaMainClass
import monix.eval.Task
import java.nio.file.Path
import bloop.logging.Logger

abstract class DebuggeeRunner {
  def logger: Logger
  def run(logger: DebugSessionLogger): Task[ExitStatus]
  def classFilesMappedTo(origin: Path, lines: Array[Int], columns: Array[Int]): List[Path]
}




//

//
//object DebuggeeRunner {
//  def forMainClass(
//      projects: Seq[Project],
//      mainClass: ScalaMainClass,
//      state: State
//  ): Either[String, DebuggeeRunner] = {
//    projects match {
//      case Seq() => Left(s"No projects specified for main class: [$mainClass]")
//      case Seq(project) =>
//        project.platform match {
//          case jvm: Platform.Jvm =>
//            Right(new MainClassDebugAdapter(project, mainClass, jvm.config, state))
//          case platform =>
//            Left(s"Unsupported platform: ${platform.getClass.getSimpleName}")
//        }
//
//      case projects => Left(s"Multiple projects specified for main class [$mainClass]: $projects")
//    }
//  }
//
//  def forTestSuite(
//      projects: Seq[Project],
//      filters: List[String],
//      state: State
//  ): Either[String, DebuggeeRunner] = {
//    projects match {
//      case Seq() => Left(s"No projects specified for the test suites: [${filters.sorted}]")
//      case projects => Right(new TestSuiteDebugAdapter(projects, filters, state))
//    }
//  }
//
//  def forAttachRemote(state: State): DebuggeeRunner =
//    new AttachRemoteDebugAdapter(state)
//
//  def classFilesMappedTo(
//      origin: Path,
//      lines: Array[Int],
//      columns: Array[Int],
//      allAnalysis: Seq[Analysis]
//  ): List[Path] = {
//    def isInfoEmpty(info: SourceInfo) = info == sbt.internal.inc.SourceInfos.emptyInfo
//
//    val originFile = origin.toFile
//    val foundClassFiles = allAnalysis.collectFirst { analysis =>
//      analysis match {
//        case analysis if !isInfoEmpty(analysis.infos.get(originFile)) =>
//          analysis.relations.products(originFile).iterator.map(_.toPath).toList
//      }
//    }
//
//    foundClassFiles.toList.flatten
//  }
//}
