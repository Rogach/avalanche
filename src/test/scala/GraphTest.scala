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

  test("depth first traversal, with descending into all nodes") {
    testGraph.depthFirstSearch() ==== List(
      ('f',Set()), ('g',Set()), ('h',Set()),
      ('d',Set('f', 'g', 'h')), ('e',Set('g')),
      ('a',Set('d', 'e')), ('b',Set('d')),
      ('c',Set('e', 'h'))
    )
  }
  test("depth first traversal, excluding tree part") {
    testGraph.depthFirstSearch('d'!=) ==== List(
      ('g',Set()), ('e',Set('g')), ('a',Set('d', 'e')),
      ('b',Set('d')), ('h',Set()), ('c',Set('e', 'h'))
    )
  }
  test("graph with single element") {
    Graph(Map("a" -> Set[String]())).depthFirstSearch("a"!=) ==== List()
  }

}
