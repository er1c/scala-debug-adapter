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

private final class AttachRemoteDebugAdapter(state: State) extends DebuggeeRunner {
  private lazy val allAnalysis = state.results.allAnalysis
  override def logger: Logger = state.logger

  override def run(logger: DebugSessionLogger): Task[ExitStatus] = Task(ExitStatus.Ok)

  override def classFilesMappedTo(
      origin: Path,
      lines: Array[Int],
      columns: Array[Int]
  ): List[Path] = {
    DebuggeeRunner.classFilesMappedTo(origin, lines, columns, allAnalysis)
  }
}