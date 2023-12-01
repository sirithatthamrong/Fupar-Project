package Game

case class Enemy(
                  var Pos: (Int, Int), // Position
                  var HP: Int, // Alive?
                ) {
  def updatePos(newRow: Int, newCol: Int): Unit = { this.Pos = (newRow, newCol) }

  def updateHP(): Unit = { this.HP -= 1 }

  def isAlive: Boolean = { this.HP >= 1 }
}