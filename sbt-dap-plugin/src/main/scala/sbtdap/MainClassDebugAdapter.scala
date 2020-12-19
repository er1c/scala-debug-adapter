package sbtdap

//import bloop.data.{Platform, Project}
//import bloop.engine.State
//import bloop.engine.tasks.{RunMode, Tasks}
//import bloop.data.JdkConfig
//import bloop.testing.{LoggingEventHandler, TestInternals}
import bloop.logging.Logger
import ch.epfl.scala.bsp.ScalaMainClass
import dap.{DebuggeeRunner, DebugSessionLogger, ExitStatus}
import java.nio.file.Path
import monix.eval.Task
import sbt.{Keys, ForkOptions, Project, State}

private final class MainClassDebugAdapter(
    project: Project,
    mainClass: ScalaMainClass,
    env: JdkConfig,
    state: State
) extends DebuggeeRunner {
  private lazy val allAnalysis = state.results.allAnalysis
  def classFilesMappedTo(
      origin: Path,
      lines: Array[Int],
      columns: Array[Int]
  ): List[Path] = {
    DebuggeeRunner.classFilesMappedTo(origin, lines, columns, allAnalysis)
  }

  def logger: Logger = {
    state.logger
  }

  def run(debugLogger: DebugSessionLogger): Task[ExitStatus] = {

    val forkOptions: ForkOptions = (Keys.forkOptions in project).value
    // TODO: Add mainClass.environmentVariables, to forkOptions
    val forkRun = new sbt.ForkRun(forkOptions)

    forkRun.fork(
      mainClass = mainClass.`class`,
      classpath: Seq[File],
      options = (mainClass.arguments ++ mainClass.jvmOptions),
      log: Logger
    )
    val javaOptions: Seq[String] = (Keys.javaOptions in project).value


    sbt.Run.run(

    )
    // def run(mainClass: String, classpath: Seq[File], options: Seq[String], log: Logger)

    val workingDir = state.commonOptions.workingPath
    val runState = Tasks.runJVM(
      state.copy(logger = debugLogger),
      project,
      env,
      workingDir,
      mainClass.`class`,
      (mainClass.arguments ++ mainClass.jvmOptions).toArray,
      skipJargs = false,
      mainClass.environmentVariables,
      RunMode.Debug
    )

    runState.map(_.status)
  }
}