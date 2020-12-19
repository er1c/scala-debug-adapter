package bloop.dap

import bloop.data.{Platform, Project}
import bloop.engine.State
import bloop.engine.tasks.{RunMode, Tasks}
import bloop.data.JdkConfig
import bloop.testing.{LoggingEventHandler, TestInternals}
import ch.epfl.scala.bsp.ScalaMainClass
import monix.eval.Task
import java.nio.file.Path
import xsbti.compile.analysis.SourceInfo
import sbt.internal.inc.Analysis
import bloop.logging.Logger

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