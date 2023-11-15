import Prompt.{endPointErrorPrompt, endPointPrompt, endPointValidation, enemyNumErrorPrompt, enemyNumPrompt,
  enemyNumValidation, gridSizeErrorPrompt, gridSizePrompt, gridSizeValidation, startPointErrorPrompt, startPointPrompt,
  startPointValidation, towerLocationErrorPrompt, towerLocationPrompt, towerLocationValidation}
import scala.io.StdIn
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

// Case class to store game configuration
case class GameConfig(
                       gridNum: Int,
                       enemyNum: Int,
                       towerNum: Int,
                       towerLocation: Vector[(Int, Int)],
                       startPt: (Int, Int),
                       endPt: (Int, Int),
                       cells: Vector[(Int, Int, Char)],
                       gameMap: Vector[String]
                     )

object UserInput extends App {

  // Print the map to the console
  def printMap(map: Vector[String], stylingFunction: String => String = identity): Unit = {
    map.foreach(line => println(stylingFunction(line)))
  }
  // Create the initial map
  def createInitialMap(gridSize: Int, startPt: (Int, Int), endPt: (Int, Int), towerLocations: Vector[(Int, Int)]): (Vector[String],  Vector[(Int, Int, Char)]) = {
    val rowFutures = (1 to gridSize).toVector.map { row => Future {
        val rowCells = (1 to gridSize).toVector.map { col =>
          val cellChar = if (row == startPt._1 && col == startPt._2) 's'
          else if (row == endPt._1 && col == endPt._2) 'e'
          else if (towerLocations.contains((row, col))) 'T'
          else '0'
          cellChar
        }.mkString
        rowCells
      }
    }
    val rows = Await.result(Future.sequence(rowFutures), Duration.Inf)
    val cells = (1 to gridSize).flatMap { row =>
      rows(row - 1).zipWithIndex.map { case (cellChar, col) => (row, col + 1, cellChar) }
    }.toVector
    (rows, cells)
  }

  // Print the title with yellow bold text
  def printTitle(message: String, stylingFunction: String => String): Unit = {
    val width = message.length + 4
    val horizontalChar = "═"; val verticalChar = "║"
    val topLeftChar = "╔"; val topRightChar = "╗"
    val bottomLeftChar = "╚"; val bottomRightChar = "╝"
    // Print the top line
    print(stylingFunction(topLeftChar))
    print(stylingFunction(horizontalChar * (width - 2)))
    println(stylingFunction(topRightChar))
    // Print the message
    print(stylingFunction(verticalChar + " " + message + " " + verticalChar))
    println()
    // Print the bottom line
    print(stylingFunction(bottomLeftChar))
    print(stylingFunction(horizontalChar * (width - 2)))
    println(stylingFunction(bottomRightChar))
  }

  def blue(text: String): String = s"\u001B[34m$text\u001B[0m"
  def red(text: String): String = s"\u001B[31m$text\u001B[0m"
  def boldYellow(text: String): String = s"\u001B[1;33m$text\u001B[0m"
  def underline(text: String): String = s"\u001B[4m$text\u001B[24m"

  def parseCoordinates(input: String): Vector[(Int, Int)] = {
    val pattern = "\\((\\d+),\\s*(\\d+)\\)".r
    pattern.findAllIn(input).matchData.map { m => (m.group(1).toInt, m.group(2).toInt) }.toVector
  }

  def readIntWithPrompt(prompt: String, errorPrompt: String, validation: Int => Boolean): Int = {
    var input: Option[Int] = None
    while (input.isEmpty || !validation(input.get)) {
      if (input.nonEmpty && !validation(input.get)) { println(errorPrompt) }
      print(prompt)
      val userInput = StdIn.readLine()
      try { input = Some(userInput.toInt) }
      catch { case _: NumberFormatException => println(errorPrompt) }
    }
    input.get
  }

  def readCoordinatesWithPrompt(prompt: String, errorPrompt: String, validation: (Int, Int) => Boolean): Vector[(Int, Int)] = {
    var coordinates: Vector[(Int, Int)] = Vector.empty
    while (true) {
      print(prompt)
      val userInput = StdIn.readLine()
      val parsedCoordinates: Vector[(Int, Int)] = parseCoordinates(userInput)
      if (parsedCoordinates.nonEmpty) {
        val validCoordinates = parsedCoordinates.filter { case (x, y) => validation(x, y) }
        val invalidCoordinates = parsedCoordinates.filterNot { case (x, y) => validation(x, y) }
        if (invalidCoordinates.nonEmpty) { println(red("Invalid: " + invalidCoordinates.toString())) }
        if (validCoordinates.nonEmpty) { coordinates ++= validCoordinates }
        else { println(errorPrompt) }
      }
      else { println(errorPrompt) }
      print("Do you want to rewrite/add location? (y/n): ")
      if (StdIn.readLine().toLowerCase != "y") { return coordinates }
    }
    coordinates
  }

  printTitle("Welcome to Tower Defense Enemy Pathing Finding, Hope you guys enjoy :)", boldYellow)

  val gridNum = readIntWithPrompt(gridSizePrompt, gridSizeErrorPrompt, gridSizeValidation)
  val enemyNum = readIntWithPrompt(enemyNumPrompt, enemyNumErrorPrompt, enemyNumValidation)
  val startPt = readCoordinatesWithPrompt(startPointPrompt, startPointErrorPrompt, startPointValidation).head
  val endPt = readCoordinatesWithPrompt(endPointPrompt, endPointErrorPrompt, endPointValidation).head
  val towerLocation = readCoordinatesWithPrompt(towerLocationPrompt, towerLocationErrorPrompt, towerLocationValidation)
  val towerNum = towerLocation.length
  val (gameMap, cells) = createInitialMap(gridNum, startPt, endPt, towerLocation)

  GameConfig(gridNum, enemyNum, towerNum, towerLocation, startPt, endPt, cells, gameMap)

  // Print the game map
  println(underline(boldYellow("Initial Map")))
  printMap(this.gameMap)
}
