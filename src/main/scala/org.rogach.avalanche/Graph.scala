package org.rogach.avalanche

object Graph {
  def apply[T]() = new Graph[T](Map().withDefault(_ => Set()))
  def apply[T](map: Map[T, Set[T]]) = new Graph[T](map.withDefault(_ => Set()))
}
class Graph[T](val map: Map[T, Set[T]]) {
  def addEdge(t1: T, t2: T) = new Graph(map = map + (t1 -> (map(t1) + t2)))

  /** Get neighbours close to this node */
  def apply(node: T) = map(node)

  def roots: Set[T] = map.keySet -- map.values.toSet.flatten

  /** returns a list of nodes, selected in depth-first order.
   *  @param select Function, that specifies if search should include this node and descend into its children.
   */
  def depthFirstSearch(roots: Set[T], select: T => Boolean = _ => true): List[T] = {
    val visited = new collection.mutable.HashSet[T]
    val traversal = new collection.mutable.ListBuffer[T]
    def visit(n: T) {
      visited += n
      map(n).filterNot(visited).filter(select).foreach(visit)
      traversal += n
    }
    roots filter select foreach visit
    traversal.toList
  }

  def nodes: Set[T] = map.keySet ++ map.values.toSet.flatten

}
