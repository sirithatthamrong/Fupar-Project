package Game

import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

class gameLogic(GridNum: Int, EnemyNum: Int, StartPt: (Int, Int), EndPt: (Int, Int), TowerPos: List[(Int, Int)]) {

  private var gameTowers: List[Tower] = List.empty
  private var gameEnemies: List[Enemy] = List.empty
  private var enemiesInTown = AtomicInteger(0)

  private val LosingThreshold = 0.25 // Adjust as needed

  private def checkWinOrLose(): Unit = {
    val enemiesInHomeLength = enemiesInTown.get()
    if (enemiesInHomeLength >= LosingThreshold * EnemyNum)
      println("You Lose! Too many enemies reached the town ╥_╥")
    else println("You Win! The town is safe ´•‿•`")
  }

  /**
   * @param towers  gameTowers
   * @param enemies gameEnemies
   */
  private def shooting(towers: List[Tower], enemies: List[Enemy]): Unit = {
    for (tower <- towers) {
      if (tower.queue.nonEmpty) {
        val targetEnemy = tower.queue.head
        targetEnemy.updateHP()
        // If an enemy is dead
        if (!targetEnemy.isAlive) {
          gameEnemies = gameEnemies.filterNot(_ == targetEnemy)
          towers.foreach(_.deleteQueue(targetEnemy))
        }
        else {
          // otherwise
          tower.deleteQueue(targetEnemy)
        }
      }
    }
  }

  /**
   * @param towers  gameTowers
   * @param enemies gameEnemies
   */
  private def updateTowerQueue(towers: List[Tower], enemies: List[Enemy]): Unit = {
    val futures = Future.traverse(towers) { tower =>
      Future {
        val enemiesInRange: List[Enemy] = enemies.filter(enemy => isInRange(tower, enemy))
        tower.addQueue(enemiesInRange)
      }
    }
    Await.ready(futures, Duration.Inf)
  }

  /**
   * @param tower Tower
   * @param enemy Enemy
   * @return Check whether the enemy is nearby the tower within 1 grid (the range can be adjusted)
   */
  private def isInRange(tower: Tower, enemy: Enemy): Boolean = {
    val dR = math.abs(tower.Pos._1 - enemy.Pos._1)
    val dC = math.abs(tower.Pos._2 - enemy.Pos._2)
    dR <= 1 && dC <= 1 && (dR + dC) <= 2
  }


  private def checkEnemyAtEndPoint(enemy: Enemy): Unit = {
    if (enemy.Pos == EndPt) {
      enemiesInTown.getAndIncrement()
      gameEnemies = gameEnemies.filterNot(_ == enemy)
    }
  }

  /**
   * @param num      UserInput.enemyNum
   * @param bestPath best path from Astar
   */
  private def moveEach(num: Int, bestPath: List[Node]): Unit = {
    // Adding n enemies to the map
    for (step <- 0 until num) {
      if (gameEnemies.nonEmpty) {
        // Update positions of existing enemies based on their individual paths
        gameEnemies.zipWithIndex.foreach { case (enemy, index) =>
          val newPosition = bestPath(gameEnemies.length - 1 - index).pos
          enemy.Pos = (newPosition._1, newPosition._2)
          checkEnemyAtEndPoint(enemy)
        }
      }
      // Add a new enemy at the start point
      val newEnemy = Enemy(StartPt, 3)
      gameEnemies = gameEnemies :+ newEnemy
      updateTowerQueue(gameTowers, gameEnemies)
      shooting(gameTowers, gameEnemies)
    }

    var steps = num - 1
    // Continue the movement of the remaining enemies
    while (gameEnemies.nonEmpty) {
      gameEnemies.zipWithIndex.foreach { case (enemy, index) =>
        val newPosition = bestPath(steps - index).pos
        enemy.Pos = (newPosition._1, newPosition._2)
        checkEnemyAtEndPoint(enemy)
      }
      updateTowerQueue(gameTowers, gameEnemies)
      shooting(gameTowers, gameEnemies)
      steps += 1
    }
  }


  /**
   * Game Loop run here
   */
  def gameLoop(): Unit = {
    gameTowers = TowerPos.collect { case (row, col) => Tower((row, col), List.empty) }

    val bestPath: List[Node] = AStarPathfinder.findPath(StartPt, EndPt, TowerPos)

    moveEach(EnemyNum, bestPath)

    checkWinOrLose()
    println(s"${EnemyNum - enemiesInTown.get} are death")
    println(s"The best path is $bestPath")
  }
}
