package pathfinding

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

class Parallel5pAStarPathfinder(implicit ec: ExecutionContext) extends AStarPathfinder {
  private val fCostThreshold = 0.05

  def findPathParallel(startLocation: (Int, Int), endLocation: (Int, Int), towerLocations: Set[(Int, Int)], gridDimension: Int): Future[List[Node]] = {
    val nodeGrid = new NodeGrid(startLocation, endLocation, towerLocations, gridDimension)

    for {
      _ <- nodeGrid.initGrid()
      startNode <- Future.successful(nodeGrid.getNode(startLocation._1, startLocation._2).getOrElse(throw new IllegalStateException("Start node not found")))
      endNode <- Future.successful(nodeGrid.getNode(endLocation._1, endLocation._2).getOrElse(throw new IllegalStateException("End node not found")))
      path <- explorePathsParallel(startNode, endNode, nodeGrid)
    } yield path
  }

  private def explorePathsParallel(startNode: Node, endNode: Node, nodeGrid: NodeGrid): Future[List[Node]] = {
    val openSet = mutable.PriorityQueue(startNode)(Ordering.by(-_.fCost))
    val closedSet = mutable.Set[Node]()
    startNode.gCost = 0
    startNode.hCost = heuristic.calculateHCost(startNode, endNode)

    def exploreNextBatch(): Future[List[Node]] = {
      if (openSet.nonEmpty) {
        val currentBatch = getNextBatch(openSet, nodeGrid)
        val futures = currentBatch.map(node => processNode(node, endNode, nodeGrid, closedSet, openSet))
        Future.sequence(futures).flatMap(_ => exploreNextBatch())
      } else Future.successful(reconstructPath(endNode))
    }

    exploreNextBatch()
  }

  private def getNextBatch(openSet: mutable.PriorityQueue[Node], nodeGrid: NodeGrid): List[Node] = {
    // Convert the priority queue to a sorted list based on f-cost
    val sortedOpenSet = openSet.dequeueAll.sortBy(_.fCost)

    // Determine the f-cost threshold (95th percentile of f-costs)
    val thresholdIndex = (sortedOpenSet.size * 0.95).toInt
    val fCostThreshold = sortedOpenSet.lift(thresholdIndex).map(_.fCost).getOrElse(Double.MaxValue)

    // Select nodes for the batch whose f-cost is less than or equal to the threshold
    sortedOpenSet.filter(_.fCost <= fCostThreshold).toList
  }


  private def processNode(current: Node, endNode: Node, nodeGrid: NodeGrid, closedSet: mutable.Set[Node], openSet: mutable.PriorityQueue[Node]): Future[Unit] = {
    Future {
      if (!closedSet.contains(current)) {
        closedSet.synchronized { closedSet.add(current) }
        current.neighbors.filter(n => !closedSet.contains(n) && n.isWalkable).foreach { neighbor =>
          val tentativeGScore = current.gCost + heuristic.calculateGCost(neighbor, current)
          if (tentativeGScore < neighbor.gCost) {
            neighbor.gCost = tentativeGScore
            neighbor.hCost = heuristic.calculateHCost(neighbor, endNode)
            neighbor.parent = Some(current)
            openSet.synchronized { if (!openSet.exists(_.pos == neighbor.pos)) openSet.enqueue(neighbor) }
          }
        }
      }
    }
  }
}