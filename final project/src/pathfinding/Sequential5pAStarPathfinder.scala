package pathfinding

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration._

class Sequential5pAStarPathfinder extends AStarPathfinder {
  private val fCostThreshold = 0.05

  def findPathSequential(startLocation: (Int, Int), endLocation: (Int, Int), towerLocations: Set[(Int, Int)], gridDimension: Int): List[Node] = {
    val nodeGrid = new NodeGrid(startLocation, endLocation, towerLocations, gridDimension)
    Await.result(nodeGrid.initGrid(), 10.seconds)

    val startNode = nodeGrid.getNode(startLocation._1, startLocation._2).getOrElse(throw new IllegalStateException("Start node not found"))
    val endNode = nodeGrid.getNode(endLocation._1, endLocation._2).getOrElse(throw new IllegalStateException("End node not found"))

    explorePathsSequential(startNode, endNode, nodeGrid)
  }

  private def explorePathsSequential(startNode: Node, endNode: Node, nodeGrid: NodeGrid): List[Node] = {
    val openSet = mutable.PriorityQueue(startNode)(Ordering.by(-_.fCost))
    val closedSet = mutable.Set[Node]()

    //    println(s"Starting with openSet size: ${openSet.size}")

    startNode.gCost = 0
    startNode.hCost = heuristic.calculateHCost(startNode, endNode)

    while (openSet.nonEmpty) {
      //      println(s"Open Set Size Before Batch: ${openSet.size}")
      val currentBatch = getNextBatch(openSet, nodeGrid)
      //      println(s"Processing Batch of Size: ${currentBatch.size}")

      currentBatch.foreach(node => processNode(node, endNode, nodeGrid, closedSet, openSet))
    }

    reconstructPath(endNode)
  }

  private def getNextBatch(openSet: mutable.PriorityQueue[Node], nodeGrid: NodeGrid): List[Node] = {
    // Convert the priority queue to a sorted list based on f-cost
    val sortedOpenSet = openSet.dequeueAll.sortBy(_.fCost)

    // Determine the f-cost threshold (95th percentile of f-costs)
    val thresholdIndex = (sortedOpenSet.size * 0.95).toInt
    val fCostThreshold = sortedOpenSet.lift(thresholdIndex).map(_.fCost).getOrElse(Double.MaxValue)

    // Select nodes for the batch whose f-cost is less than or equal to the threshold
    val batch = sortedOpenSet.filter(_.fCost <= fCostThreshold)
    batch.toList
  }


  private def processNode(current: Node, endNode: Node, nodeGrid: NodeGrid, closedSet: mutable.Set[Node], openSet: mutable.PriorityQueue[Node]): Unit = {
    if (!closedSet.contains(current)) {
      closedSet.add(current)
      current.neighbors.filter(n => !closedSet.contains(n) && n.isWalkable).foreach { neighbor =>
        val tentativeGScore = current.gCost + heuristic.calculateGCost(neighbor, current)
        if (tentativeGScore < neighbor.gCost) {
          neighbor.gCost = tentativeGScore
          neighbor.hCost = heuristic.calculateHCost(neighbor, endNode)
          neighbor.parent = Some(current)
          if (!openSet.exists(_.pos == neighbor.pos)) openSet.enqueue(neighbor)
        }
      }
    }
  }
}