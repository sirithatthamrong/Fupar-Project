package pathfinding

class Heuristic {
  private def manhattanDistance(node1: Node, node2: Node): Double = {
    math.abs(node1.pos._1 - node2.pos._1) + math.abs(node1.pos._2 - node2.pos._2)
  }

  def calculateHCost(currentNode: Node, goalNode: Node): Double = {
    manhattanDistance(currentNode, goalNode)
  }

  // Additional cost for being in range of a turret
  def turretRangeCost(currentNode: Node): Double = {
    if (currentNode.isInTowerRange) 5.0f else 0.0f
  }

  // Combined cost from start to current node
  def calculateGCost(currentNode: Node, parentNode: Node): Double = {
    val travelCost = 1.0f // cost for moving from parent to current node
    parentNode.gCost + travelCost + turretRangeCost(currentNode) + calculateHCost(currentNode, parentNode)
  }

}
