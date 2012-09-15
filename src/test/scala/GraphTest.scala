package org.rogach.avalanche

class GraphTest extends UsefulTest {
  val testGraph = Graph(
    nodes = ('a' to 'h').toList, 
    edges = List('a'->'d', 'a'->'e', 'b'->'d', 'c'->'e', 'c'->'h', 'd'->'f', 'd'->'g', 'd'->'h', 'e'->'g')
  )

  test("topological sort") {
    testGraph.topologicalSort ==== List('a','b','d','f','c','h','e','g')
  }
  test("depth first traversal, with descending into all nodes") {
    testGraph.depthFirstSearch() ==== List(('f',Nil), ('g',Nil), ('h',Nil), ('d',List('f', 'g', 'h')), ('e',List('g')), ('a',List('d', 'e')), ('b',List('d')), ('c',List('e', 'h')))
  }
  test("depth first traversal, excluding tree part") {
    testGraph.depthFirstSearch('d'!=) ==== List(('g',Nil), ('e',List('g')), ('a',List('d', 'e')), ('b',List('d')), ('h',Nil), ('c',List('e', 'h')))
  }
  test("graph with single element") {
    Graph(List("a"), Nil).depthFirstSearch("a"!=) ==== Nil
  }
  
}
