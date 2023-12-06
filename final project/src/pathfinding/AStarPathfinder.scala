package pathfinding

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration._

class AStarPathfinder {
  private object NodeOrdering extends Ordering[Node] {
    override def compare(x: Node, y: Node): Int = x.fCost.compare(y.fCost)
  }

  val heuristic = new Heuristic()

  /**
   * The main bulk of the A Star algorithm - set to protected so that the parallel version that extends
   * AStarPathfinder can use this as well
   * @param start:Node - Starting point coordinate
   * @param end:Node - End goal coordinate
   * @param nodeGrid: NodeGrid - contains a 2d arr of nodes and dimensions
   * @return List of Nodes
   */
  protected def aStarSearch(start: Node, end: Node, nodeGrid: NodeGrid): List[Node] = {
    start.gCost = 0f
    start.parent = None

    val openSet = mutable.PriorityQueue(start)(Ordering.by(-_.fCost))
    val closedSet = mutable.Set[Node]()

    while (openSet.nonEmpty) {
      val current = openSet.dequeue()
      //      println(s"Dequeued: ${current.pos}")


      if (current.pos == end.pos) {
        //        println("Goal reached")
        return reconstructPath(current)
      }

      closedSet.add(current)
      //      println(s"Added to closed set: ${current.pos}")

      for (neighbor <- current.neighbors) {
        if (!closedSet.contains(neighbor)) {
          val tentativeGScore = heuristic.calculateGCost(neighbor, current)

          //          println(s"Checking neighbor: ${neighbor.pos}, tentativeGScore: $tentativeGScore, gScore: ${current.gCost}")


          if (tentativeGScore < neighbor.gCost) {
            neighbor.parent = Some(current)
            neighbor.gCost = tentativeGScore
            neighbor.hCost = heuristic.calculateHCost(neighbor, end)
            if (!openSet.exists(_.pos == neighbor.pos)) {
              openSet.enqueue(neighbor) // This ensures that the node is added only if it's not already present in the open set
            }
          }
        }
      }
    }
    List.empty[Node]
  }

  protected def reconstructPath(endNode: Node): List[Node] = {
    var path = List[Node]()
    var current: Option[Node] = Some(endNode)
    while (current.isDefined) {
      //      println(s"Path node: ${current.get.pos}")
      path = current.get :: path
      current = current.get.parent
    }
    path
  }

  /**
   * This function is what you will call to create your most optimal path.
   * @param startLocation: (Int, Int) - Where the entrance is
   * @param endLocation: (Int, Int) - Where the exit is
   * @param towerLocations: Set[(Int, Int)] - Where the towers are located
   * @param gridDimension - How large our grid is
   * @return List of Nodes - Node.pos will give you the coordinates of which cell to go to
   */

  def findPath(startLocation: (Int, Int), endLocation: (Int, Int), towerLocations: Set[(Int, Int)], gridDimension: Int): List[Node] = {
    val nodeGrid = new NodeGrid(startLocation, endLocation, towerLocations, gridDimension)
    Await.result(nodeGrid.initGrid(), 10.seconds)

    val startNodeOption = nodeGrid.getNode(startLocation._1, startLocation._2)
    val endNodeOption = nodeGrid.getNode(endLocation._1, endLocation._2)

    startNodeOption.flatMap { startNode =>
      // since it is of type Option[Node] it will be Some(Node) if the start node exists in the grid or None if it doesn't
      endNodeOption.map { endNode =>
        startNode.parent = None // Set the parent of startNode to None - was getting error previously with stackoverflow :(
        aStarSearch(startNode, endNode, nodeGrid)
      }
    }.getOrElse(List.empty)
  }
}
