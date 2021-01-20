package sbtdap

import sbt.KeyRanks.DTask
import sbt.inputKey

trait PluginKeys {
  val bspBuildTargetTestDebug = inputKey[Unit]("Corresponds to buildTarget/testDebug request").withRank(DTask)
  val bspBuildTargetRunDebug = inputKey[Unit]("Corresponds to buildTarget/runDebug request").withRank(DTask)
  //val bspBuildTargetAttachDebug = inputKey[Unit]("Corresponds to buildTarget/runDebug request").withRank(DTask)
}