package UserInput

import scala.io.StdIn
import Prompt.*

class getUserInput {
  
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
        if (invalidCoordinates.nonEmpty) {
          println(Prompt.red("Invalid: " + invalidCoordinates.toString()))
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
  
  Prompt.printTitle("Welcome to Tower Defense Enemy Pathing Finding, Hope you guys enjoy :)", Prompt.boldYellow)
  val gridNum: Int = readIntWithPrompt(gridSizePrompt, gridSizeErrorPrompt, gridSizeValidation)
  val enemyNum: Int = readIntWithPrompt(enemyNumPrompt, enemyNumErrorPrompt, enemyNumValidation)
  val startPt: (Int, Int) = readCoordinatesWithPrompt(startPointPrompt, startPointErrorPrompt, startPointValidation).head
  val endPt: (Int, Int) = readCoordinatesWithPrompt(endPointPrompt, endPointErrorPrompt, endPointValidation).head
  val towerLocation: List[(Int, Int)] = readCoordinatesWithPrompt(towerLocationPrompt, towerLocationErrorPrompt, towerLocationValidation)
}
