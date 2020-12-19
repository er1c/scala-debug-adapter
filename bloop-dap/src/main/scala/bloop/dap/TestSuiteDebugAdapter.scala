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

private final class TestSuiteDebugAdapter(
    projects: Seq[Project],
    filters: List[String],
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
    val debugState = state.copy(logger = debugLogger)

    val filter = TestInternals.parseFilters(filters)
    val handler = new LoggingEventHandler(debugState.logger)

    val task = Tasks.test(
      debugState,
      projects.toList,
      Nil,
      filter,
      handler,
      runInParallel = false,
      mode = RunMode.Debug
    )

    task.map(_.status)
  }
}