package dap

trait ExitStatus {
  def code: Int
  def name: String

  def isOk: Boolean = code == 0
  override def toString: String = s"$name=$code"
}