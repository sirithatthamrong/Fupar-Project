package pathfinding

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class NodeGrid(start: (Int, Int), end: (Int, Int), towers: Set[(Int, Int)], size: Int) {
  private val grid: Array[Array[String]] = createGrid(start, end, towers, size)
  private val rowSize = grid.length
  private val colSize = grid(0).length
  private val nodeGrid: Array[Array[Node]] = Array.ofDim[Node](rowSize, colSize)


  /**
   * 2d array of where things are. Will be used for node grid creation
   *
   * @param start  - starting point
   * @param end    - end point
   * @param towers - tower locations
   * @param size   - size of the grid
   * @return returns a 2d array of string indicating where things are.
   */
  private def createGrid(start: (Int, Int), end: (Int, Int), towers: Set[(Int, Int)], size: Int): Array[Array[String]] = {
    Array.tabulate(size, size) { (x, y) =>
      if (start == (x, y)) "s"
      else if (end == (x, y)) "g"
      else if (towers.contains((x, y))) "T"
      else ""
    }
  }

  def initGrid(): Future[Unit] = Future {
    for (x <- 0 until rowSize; y <- 0 until colSize) {
      val cellType = grid(x)(y)
      val isTower = cellType == "T"
      nodeGrid(x)(y) = Node((x, y), isTower = isTower)
    }
    // Mark tower range
    for (x <- 0 until rowSize; y <- 0 until colSize if grid(x)(y) == "T") {
      markTowerRange(x, y)
    }
    connectNeighbors()

  }

  private def markTowerRange(x: Int, y: Int): Unit = {
    (-1 to 1).flatMap(i => (-1 to 1).map(j => (i, j)))
      .filter { case (i, j) => isValidPosition(x + i, y + j) && !(i == 0 && j == 0) }
      .foreach { case (i, j) => nodeGrid(x + i)(y + j).isInTowerRange = true }
  }

  private def connectNeighbors(): Unit = {
    for {
      x <- 0 until rowSize
      y <- 0 until colSize
    } {
      val neighbors = Seq((-1, 0), (1, 0), (0, -1), (0, 1))
        .map { case (i, j) => (x + i, y + j) }
        .filter { case (nx, ny) => isValidPosition(nx, ny) }
        .flatMap { case (nx, ny) =>
          Option(nodeGrid(nx)(ny)).filter(_.isWalkable)
        }

      nodeGrid(x)(y).neighbors = neighbors.toVector
    }
  }


  private def isValidPosition(x: Int, y: Int): Boolean =
    x >= 0 && x < rowSize && y >= 0 && y < colSize

  def getNode(x: Int, y: Int): Option[Node] = {
    if (x >= 0 && x < rowSize && y >= 0 && y < colSize) {
      Some(nodeGrid(x)(y))
    } else {
      None
    }
  }

  def findNode(condition: Node => Boolean): Option[Node] = {
    nodeGrid.flatten.find(condition)
  }
}