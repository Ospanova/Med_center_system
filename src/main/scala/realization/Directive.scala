package realization


import scala.collection.mutable.HashMap

class Directive {
  val pathMap: HashMap  [String, String => String] = HashMap()
  val lastPath = ""

  def apply (currentPath: String) = {
      pathMap.put(lastPath, parameter => currentPath)
      this
  }

  def apply (currentFunction: String => String) = {
    pathMap.put(lastPath, currentFunction)
    this
  }

}
