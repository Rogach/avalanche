package org.rogach.avalanche

object Graph {
  def apply[T](nodes:List[T]) = new Graph(nodes,List())
}
case class Graph[T](nodes:List[T], edges:List[(T,T)]) {
  def +(node:T) = if (!nodes.contains(node)) new Graph(node :: nodes, edges) else new Graph(nodes, edges)
  def +(edge:(T,T)) = {
    if (nodes.contains(edge._1) && nodes.contains(edge._2)) {
      new Graph(nodes, edge :: edges)
    } else throw new Exception("One of the edge nodes is not present in the graph")
  }
  def remove(edge:(T,T)) = new Graph(nodes, edges.filter(edge !=))

  /** returns a list of nodes and their children, selected in depth-first order.
   *  @param select Function, that specifies if search should include this node and descend into its children.
   *                Note, that if the node is not selected, it can still be returned as child of other node!
   */
  def depthFirstSearch(select: T => Boolean = _ => true): List[(T, List[T])] = {
    val visited = new collection.mutable.HashSet[T]
    val traversal = new collection.mutable.ListBuffer[(T, List[T])]()
    val roots = nodes.filter(n => edges.filter(_._2 == n).isEmpty).filter(select)
    def visit(n: T) {
      visited += n
      val children = edges.filter(_._1 == n).map(_._2)
      children.filterNot(visited).filter(select).foreach(visit)
      traversal += (n -> children)
    }
    roots foreach visit
    traversal.toList
  }

  /* Topological sort, as per Wikipedia's article */
  def topologicalSort:List[T] = {
    var tgraph = this
    // L ← Empty list that will contain the sorted elements
    var l = List[T]()
    // S ← Set of all nodes with no incoming edges
    var s = nodes.filter(n => edges.filter(_._2 == n).isEmpty)
    // while S is non-empty do
    while (s.size > 0) {
      // remove a node n from S
      val n = s.head
      s = s.tail
      // insert n into L
      l = l :+ n
      // for each node m with an edge e from n to m do
      tgraph.nodes.filter(m => tgraph.edges.filter(e => e._1 == n && e._2 == m).size > 0).foreach { m =>
        // remove edge e from the graph
        tgraph.edges.filter(e => e._1 == n && e._2 == m).foreach{e => tgraph = tgraph.remove(e)}
        // if m has no other incoming edges then
        if (tgraph.edges.filter(e => e._2 == m).isEmpty) {
          // insert m into S
          s = m :: s
        }
      }
    }
    // if graph has edges then
    if (tgraph.edges.size > 0) {
      // return error (graph has at least one cycle)
      throw new Exception("Graph has at least one cycle")
    // else
    } else {
      // return L (a topologically sorted order)
      return l
    }
  }
}
