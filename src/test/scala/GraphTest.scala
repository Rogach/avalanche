package org.rogach.avalanche

class GraphTest extends UsefulTest {
  val testGraph = Graph(
    Map(
      ('a', Set('d','e')),
      ('b', Set('d')),
      ('c', Set('e','h')),
      ('d', Set('f','g','h')),
      ('e', Set('g'))
    )
  )

  test("roots retrieval") {
    testGraph.roots ==== Set('a','b','c')
  }
  test("depth first traversal, with descending into all nodes") {
    testGraph.depthFirstSearch(testGraph.roots) ==== List('f','g','h','d','e','a','b','c')
  }
  test("depth first traversal, excluding tree part") {
    testGraph.depthFirstSearch(testGraph.roots, 'd'!=) ==== List('g','e','a','b','h','c')
  }
  test("graph with single element") {
    Graph(Map("a" -> Set[String]())).depthFirstSearch(Set("a"), "a"!=) ==== List()
  }

}
