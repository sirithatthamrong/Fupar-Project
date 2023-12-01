package Game

import UserInput.*

object Main extends App {
  private val input = new getUserInput()

  val GridNum = input.gridNum
  val EnemyNum = input.enemyNum
  val StartPt = input.startPt
  val EndPt = input.endPt
  val TowerPos = input.towerLocation

  private val startTime = System.currentTimeMillis()
  private val game = new gameLogic(GridNum, EnemyNum, StartPt, EndPt, TowerPos)
  game.gameLoop()
  private val endTime = System.currentTimeMillis()
  private val elapsedTimeSeconds = (endTime - startTime) / 1000.0
  println(s"Elapsed time: $elapsedTimeSeconds seconds")
}
