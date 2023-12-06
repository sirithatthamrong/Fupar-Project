package game

import userInput.*

object Main extends App {

  val GridNum = getUserInput.gridNum
  val EnemyNum = getUserInput.enemyNum
  val StartPt = getUserInput.startPt
  val EndPt = getUserInput.endPt
  val TowerPos = getUserInput.towerLocation

  println("----------------------------------------------------------------------------")

  private val startTimeP = System.currentTimeMillis()
  private val gameP = new SequentialGameLogic(GridNum, EnemyNum, (StartPt._1 - 1, StartPt._2 - 1), (EndPt._1 - 1, EndPt._2 - 1), TowerPos)
  gameP.gameLoop()
  private val endTimeP = System.currentTimeMillis()
  private val elapsedTimeSecondsP = (endTimeP - startTimeP) / 1000.0
  println(userPrompt.boldYellow(s"Without Parallel Version: $elapsedTimeSecondsP seconds"))

  println("----------------------------------------------------------------------------")

  private val startTime = System.currentTimeMillis()
  private val game = new ParallelGameLogic(GridNum, EnemyNum, (StartPt._1 - 1, StartPt._2 - 1), (EndPt._1 - 1, EndPt._2 - 1), TowerPos)
  game.gameLoop()
  private val endTime = System.currentTimeMillis()
  private val elapsedTimeSeconds = (endTime - startTime) / 1000.0
  println(userPrompt.boldYellow(s"Parallel Version: $elapsedTimeSeconds seconds"))
}
