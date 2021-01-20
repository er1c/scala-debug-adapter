package sbtdap

import dap.{Logger => DapLogger, _}
import java.nio.file.Path
import sbt.{Def, ForkOptions, ForkRun, Keys, Project, State, Task}
import sbt.internal.bsp
import scala.util.{Failure, Success}
import sjsonnew.JsonFormat
import sjsonnew.support.scalajson.unsafe.Converter
import sjsonnew.shaded.scalajson.ast.unsafe.JValue
import java.net.URI
import sbt.internal.bsp.{RunResult, ScalaMainClass, StatusCode}
import sbt.internal.server.ServerCallback
import sbt.internal.util.Attributed
import scala.concurrent.Future


object SbtDebuggeeRunner {

  // From: https://github.com/build-server-protocol/build-server-protocol/blob/8ed2c5126bede46b0908ba33076dedd761ebbeb0/bsp4j/src/main/java/ch/epfl/scala/bsp4j/DebugSessionParamsDataKind.java
  object DebugSessionParamsDataKind {
    val ScalaMainClass: String = "scala-main-class"
    val ScalaTestSuites: String = "scala-test-suites"
    val ScalaAttachRemote: String = "scala-attach-remote"
  }

  private[sbtdap] def inferDebuggeeRunner(
     params: bsp.DebugSessionParams,
     projects: Seq[Project],
     state: State
  ): BspResponse[SbtDebuggeeRunner] = {
    (params.dataKind, params.data) match {
      case (None, _) => Left(JsonRpcResponse.invalidRequest("Missing required 'dataKind' argument"))
      case (_, None) => Left(JsonRpcResponse.invalidRequest("Missing required 'data' argument"))
      case (Some(dataKind), Some(data)) =>
        import sbt.internal.bsp.codec.DebugAdapterJsonProtocol._
        dataKind match {
          case DebugSessionParamsDataKind.ScalaMainClass =>
            convert[bsp.ScalaMainClass](data, main => forMainClass(projects, state, main))
          case DebugSessionParamsDataKind.ScalaTestSuites =>
            convert[List[String]](data, filters => forTestSuite(projects, state, filters))
          case DebugSessionParamsDataKind.ScalaAttachRemote =>
            convertError(forAttachRemote(state))
          case dataKind => Left(JsonRpcResponse.invalidRequest(s"Unsupported data kind: $dataKind"))
        }
    }
  }

  private def convert[A: JsonFormat](
    data: JValue,
    f: A => Either[String, SbtDebuggeeRunner]
  ): Either[ProtocolError, SbtDebuggeeRunner] = {
    Converter.fromJson[A](data) match {
      case Success(ret) => convertError(f(ret))
      case Failure(err) => Left(JsonRpcResponse.invalidParams(err.getMessage))
    }
  }

  private def convertError(runner: Either[String, SbtDebuggeeRunner]): Either[ProtocolError, SbtDebuggeeRunner] =
    runner match {
      case Right(adapter) => Right(adapter)
      case Left(error) => Left(JsonRpcResponse.invalidRequest(error))
    }

  private def forMainClass(
    projects: Seq[Project],
    state: State,
    mainClass: bsp.ScalaMainClass,
  ): Either[String, SbtDebuggeeRunner] = {
    projects match {
      case Seq() => Left(s"No projects specified for main class: [$mainClass]")
      case Seq(_) => Right(MainClassSbtDebuggeeRunner(projects, state, mainClass))
      case projects => Left(s"Multiple projects specified for main class [$mainClass]: $projects")
    }
  }

  private def forTestSuite(
    projects: Seq[Project],
    state: State,
    filters: List[String],
  ): Either[String, SbtDebuggeeRunner] = {
    projects match {
      case Seq() => Left(s"No projects specified for the test suites: [${filters.sorted}]")
      case projects => Right(TestSuiteSbtDebuggeeRunner(projects, state, filters))
    }
  }

  private def forAttachRemote(state: State): Either[String, SbtDebuggeeRunner] =
    Right(AttachRemoteSbtDebuggeeRunner(state))
}

private trait SbtDebuggeeRunner extends DebuggeeRunner {
  //def callback: ServerCallback
  def state: State

  final def getURI: URI = ???
  final override def logger: DapLogger = ???
  final override def classFilesMappedTo(origin: Path, lines: Array[Int], columns: Array[Int]): List[Path] = ???


  protected def getProjectFromBuildTargetIdentifier(target: bsp.BuildTargetIdentifier): Either[String, Option[Project]] = {

    //       val targets = spaceDelimited().parsed.map(uri => BuildTargetIdentifier(URI.create(uri)))
    //      val filter = ScopeFilter.in(targets.map(workspace))

    //ProjectUris.getProjectDagFromUri(projectUri, state)
    ???
  }
}

// originId: Option[String]
private final case class MainClassSbtDebuggeeRunner(
  projects: Seq[Project],
  state: State,
  mainClass: bsp.ScalaMainClass
) extends SbtDebuggeeRunner {


  override def run(callbacks: DebugSessionCallbacks): CancelableFuture[Unit] = Def.task {
    val state = Keys.state.value
    val logger = Keys.streams.value.log
    val classpath = Attributed.data(Keys.fullClasspath.value)
    val forkOpts = ForkOptions(
      javaHome = Keys.javaHome.value,
      outputStrategy = Keys.outputStrategy.value,
      // bootJars is empty by default because only jars on the user's classpath should be on the boot classpath
      bootJars = Vector(),
      workingDirectory = Some(Keys.baseDirectory.value),
      runJVMOptions = mainClass.jvmOptions,
      connectInput = Keys.connectInput.value,
      envVars = Keys.envVars.value
    )
    val runner = new ForkRun(forkOpts)
    val statusCode = runner
      .run(mainClass.`class`, classpath, mainClass.arguments, logger)
      .fold(
        _ => StatusCode.Error,
        _ => StatusCode.Success
      )
    //callbacks.onFinish()
    //state.respondEvent(RunResult(originId, statusCode))

  }
}

private final case class TestSuiteSbtDebuggeeRunner(
  projects: Seq[Project],
  state: State,
  filters: List[String]
) extends SbtDebuggeeRunner {

  override def run(callbacks: DebugSessionCallbacks): CancelableFuture[Unit] = {
/*
          case r: JsonRpcRequestMessage if r.method == "buildTarget/test" =>
            val task = bspBuildTargetTest.key
            val paramStr = CompactPrinter(json(r))
            val _ = callback.appendExec(s"$task $paramStr", Some(r.id))
 */
    ???
  }
}

private final case class AttachRemoteSbtDebuggeeRunner(
  state: State
) extends SbtDebuggeeRunner {
  override def run(callbacks: DebugSessionCallbacks): CancelableFuture[Unit] = ???
}