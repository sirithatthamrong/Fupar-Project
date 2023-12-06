package game

import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import userInput.*
import pathfinding.*

class ParallelGameLogic(GridNum: Int,
                        EnemyNum: Int,
                        StartPt: (Int, Int),
                        EndPt: (Int, Int),
                        TowerPos: List[(Int, Int)],
                ) {

  private var gameTowers: List[Tower] = List.empty
  private var gameEnemies: List[Enemy] = List.empty
  private val enemiesInTown = AtomicInteger(0)

  private val LosingThreshold = 0.30 // Adjust as needed


  private def checkWinOrLose(): Unit = {
    if (enemiesInTown.get() >= LosingThreshold * EnemyNum)
      println(userPrompt.red("You Lose! Too many enemies reached the town ╥_╥"))
    else println(userPrompt.blue("You Win! The town is safe ´•‿•`"))
  }

  /**
   * The tower can shoot only one enemy at a time
   *
   * The first enemy the come in range is the first the will be shoot
   * @param towers  gameTowers
   * @param enemies gameEnemies
   */
  private def shooting(towers: List[Tower], enemies: List[Enemy]): Unit = {
    val filterTower = towers.filter(_.queue.nonEmpty)
    for (tower <- filterTower) {
      if (tower.queue.nonEmpty) {
        val targetEnemy = tower.queue.head
        targetEnemy.updateHP()

        val updatedEnemies = gameEnemies.zipWithIndex.map {
          case (enemy, index) =>
            if (enemy == targetEnemy) targetEnemy
            else enemy
        }

        // Update the gameEnemies list with the new list
        gameEnemies = updatedEnemies

        // If an enemy is dead
        if (!targetEnemy.isAlive) {
          gameEnemies = gameEnemies.filterNot(_ == targetEnemy)
          towers.foreach(_.deleteQueue(targetEnemy))
        }
        // otherwise
        else {
          tower.deleteQueue(targetEnemy)
        }
      }
    }
  }

  /**
   * Update tower queue in parallel
   * @param towers  gameTowers
   * @param enemies gameEnemies
   */
  private def updateTowerQueue(towers: List[Tower], enemies: List[Enemy]): Unit = {
    val futures = towers.map { tower => Future {
        val enemiesInRange: List[Enemy] = enemies.filter(enemy => isInRange(tower, enemy))
//        println(enemiesInRange)
        tower.addQueue(enemiesInRange)
        tower // Return the modified tower
      }
    }

    val updatedTowers: List[Tower] = Await.result(Future.sequence(futures), Duration.Inf)
//    println(updatedTowers)

    // Update the original towers list
    gameTowers = updatedTowers
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
      enemiesInTown.getAndAdd(1)
      gameEnemies = gameEnemies.filterNot(_ == enemy)
    }
  }

  /**
   * An enemy can move only one at at time, 1 grid for 1 enemy
   * @param num The number of enemy
   * @param bestPath from Astar
   */
  private def moveEach(num: Int, bestPath: List[Node]): Unit = {
    // Adding n enemies to the map
    for (step <- 0 until num) {
      if (gameEnemies.nonEmpty) {
        // Update positions of existing enemies based on their individual paths
        gameEnemies.zipWithIndex.foreach { case (enemy, index) =>
          val pathIndex = math.min(bestPath.length - 1, gameEnemies.length - 1 - index)
          val newPosition = bestPath(pathIndex).pos
          enemy.updatePos(newPosition._1, newPosition._2)
          checkEnemyAtEndPoint(enemy)
        }
      }
      // Add a new enemy at the start point
      val newEnemy = Enemy(StartPt, 30)
      gameEnemies = gameEnemies :+ newEnemy
      updateTowerQueue(gameTowers, gameEnemies)
      shooting(gameTowers, gameEnemies)
//      println(gameEnemies)
//      println("-------------")
    }

    // Continue the move of the remaining enemies
    var steps = num - 1
    while (gameEnemies.nonEmpty) {
      gameEnemies.zipWithIndex.foreach { case (enemy, index) =>
        val pathIndex = math.min(bestPath.length - 1, steps - index)
        val newPosition = bestPath(pathIndex).pos
        enemy.updatePos(newPosition._1, newPosition._2)
        checkEnemyAtEndPoint(enemy)
      }
      updateTowerQueue(gameTowers, gameEnemies)
      shooting(gameTowers, gameEnemies)
//      println(gameEnemies)
//      println("-------------")
      steps += 1
    }
  }


  /**
   * Game Loop run here
   */
  def gameLoop(): Unit = {
    val newTowerPos = TowerPos.map( (row, col) =>  (row-1, col-1))
    gameTowers = newTowerPos.collect { case (row, col) => Tower((row, col), List.empty) }

//    println(gameTowers)
//    println(s"towers: ${newTowerPos.toSet}")
//    println(StartPt)
//    println(EndPt)

    val aStar = new Parallel5pAStarPathfinder()
    val bestPath: List[Node] = aStar.findPath(StartPt, EndPt, newTowerPos.toSet, GridNum)
//    println(bestPath)

    moveEach(EnemyNum, bestPath.tail)

    checkWinOrLose()
    println(s"    - ${EnemyNum - enemiesInTown.get} enemies are dead")
    println(s"    - The best path uses ${bestPath.length} moves")
  }
}
