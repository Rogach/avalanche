package org.rogach.avalanche

object Graph {
  def apply[T]() = new Graph[T](Map().withDefault(_ => Set()))
  def apply[T](map: Map[T, Set[T]]) = new Graph[T](map.withDefault(_ => Set()))
}
class Graph[T](val map: Map[T, Set[T]]) {
  def addEdge(t1: T, t2: T) = new Graph(map = map + (t1 -> (map(t1) + t2)))

  /** Get neighbours close to this node */
  def neighbours(node: T) = map(node)

  /** returns a list of nodes and their children, selected in depth-first order.
   *  @param select Function, that specifies if search should include this node and descend into its children.
   *                Note, that if the node is not selected, it can still be returned as child of other node!
   */
  def depthFirstSearch(select: T => Boolean = _ => true): List[(T, Set[T])] = {
    val visited = new collection.mutable.HashSet[T]
    val traversal = new collection.mutable.ListBuffer[(T, Set[T])]()
    def visit(n: T) {
      visited += n
      val children = map(n)
      children.filterNot(visited).filter(select).foreach(visit)
      traversal += (n -> children)
    }
    val roots = map.keySet -- map.values.toSet.flatten filter select
    roots foreach visit
    traversal.toList
  }

  def nodes: Set[T] = map.keySet ++ map.values.toSet.flatten

}
