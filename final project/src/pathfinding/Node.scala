package pathfinding

case class Node(
                 pos: (Int, Int),
                 var gCost: Double = Double.MaxValue,
                 var hCost: Double = 0f,
                 var neighbors: Vector[Node] = Vector.empty,
                 var isInTowerRange: Boolean = false,
                 var parent: Option[Node] = None,
                 isTower: Boolean = false,
               ) {
  def fCost: Double = gCost + hCost
  def isWalkable: Boolean = !isTower

  // it stack overflows when i try to println(List[Node]) before overriding it
  override def toString: String = s"${pos}"

  override def hashCode(): Int = {
    val prime = 31
    var result = 1
    result = prime * result + pos.hashCode()
    result = prime * result + isTower.hashCode()
    result = prime * result + isInTowerRange.hashCode()
    result
  }
}