package game

case class Tower(
                  Pos: (Int, Int), // Position
                  var queue: List[Enemy] // Queue for shooting
                ) {
  def addQueue(enemies: List[Enemy]): Unit = { 
    this.queue = this.queue ++ enemies 
  }

  def deleteQueue(targetEnemy: Enemy): Unit = {
    this.queue = this.queue.filterNot(_ == targetEnemy)
  }
}

