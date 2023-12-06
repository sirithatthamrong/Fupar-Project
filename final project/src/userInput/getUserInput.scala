package userInput

import scala.io.StdIn
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import userPrompt.*

object getUserInput {

//  private def createInitialMap(gridSize: Int, startPt: (Int, Int), endPt: (Int, Int), towerLocations: List[(Int, Int)]): (List[String], List[((Int, Int), Char)]) = {
//    val rowFutures = (1 to gridSize).toList.map { row => Future {
//        val rowCells = (1 to gridSize).toList.map { col =>
//          val cellChar = if (row == startPt._1 && col == startPt._2) 's'
//          else if (row == endPt._1 && col == endPt._2) 'g'
//          else if (towerLocations.contains((row, col))) 'T'
//          else ' '
//          cellChar
//        }.mkString
//        rowCells
//      }
//    }
//    val rows = Await.result(Future.sequence(rowFutures), Duration.Inf)
//    val cells = (1 to gridSize).flatMap { row =>
//      rows(row - 1).zipWithIndex.map { case (cellChar, col) => ((row, col + 1), cellChar) }
//    }.toList
//    (rows, cells)
//  }

  private def parseCoordinates(input: String): Vector[(Int, Int)] = {
    val pattern = "\\((\\d+),\\s*(\\d+)\\)".r
    pattern.findAllIn(input).matchData.map { m => (m.group(1).toInt, m.group(2).toInt) }.toVector
  }

  private def readIntWithPrompt(prompt: String, errorPrompt: String, validation: Int => Boolean): Int = {
    var input: Option[Int] = None
    while (input.isEmpty || !validation(input.get)) {
      if (input.nonEmpty && !validation(input.get)) {
        println(errorPrompt)
      }
      print(prompt)
      val userInput = StdIn.readLine()
      try { input = Some(userInput.toInt) }
      catch { case _: NumberFormatException => println(errorPrompt) }
    }
    input.get
  }

  private def readCoordinatesWithPrompt(prompt: String, errorPrompt: String, validation: (Int, Int) => Boolean): List[(Int, Int)] = {
    var coordinates: List[(Int, Int)] = List.empty
    while (true) {
      print(prompt)
      val userInput = StdIn.readLine()
      val parsedCoordinates: Vector[(Int, Int)] = parseCoordinates(userInput)
      if (parsedCoordinates.nonEmpty) {
        val validCoordinates = parsedCoordinates.filter { case (x, y) => validation(x, y) }
        val invalidCoordinates = parsedCoordinates.filterNot { case (x, y) => validation(x, y) }
//        println(validCoordinates)
//        println(invalidCoordinates)
        if (invalidCoordinates.nonEmpty) {
          println(userPrompt.red("Invalid: " + invalidCoordinates.toString()))
        }
        if (validCoordinates.nonEmpty) {
          coordinates ++= validCoordinates
        }
        else {
          println(errorPrompt)
        }
      }
      else {
        println(errorPrompt)
      }
      print("Do you want to rewrite/add location? (y/n): ")
      if (StdIn.readLine().toLowerCase != "y") {
        return coordinates
      }
    }
    coordinates
  }

  userPrompt.printTitle("Welcome to Tower Defense Enemy Pathfinding, Hope you guys enjoy :)", userPrompt.boldYellow)
  val gridNum: Int = readIntWithPrompt(gridSizePrompt, gridSizeErrorPrompt, gridSizeValidation)
  val enemyNum: Int = readIntWithPrompt(enemyNumPrompt, enemyNumErrorPrompt, enemyNumValidation)
  val startPt: (Int, Int) = readCoordinatesWithPrompt(startPointPrompt, startPointErrorPrompt, startPointValidation).head
  val endPt: (Int, Int) = readCoordinatesWithPrompt(endPointPrompt, endPointErrorPrompt, endPointValidation).head
  val towerLocation: List[(Int, Int)] = readCoordinatesWithPrompt(towerLocationPrompt, towerLocationErrorPrompt, towerLocationValidation)
//  val (gameMap, cells) = createInitialMap(gridNum, startPt, endPt, towerLocation)
}
