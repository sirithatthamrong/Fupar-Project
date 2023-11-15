object Prompt {
  def blue(text: String): String = s"\u001B[34m$text\u001B[0m"
  def red(text: String): String = s"\u001B[31m$text\u001B[0m"
  
  val gridSizePrompt: String = blue("Enter the N size of grid (NxN): ")
  val gridSizeErrorPrompt: String = red("     **Invalid grid size. Please enter a valid integer. \n" +
    "      - The size must be greater than or equal to 5")
  val gridSizeValidation: Int => Boolean = (size: Int) => size >= 5

  val enemyNumPrompt: String = blue("Enter the number of enemies: ")
  val enemyNumErrorPrompt: String = red("     **Invalid number of enemies. Please enter a valid integer. \n" +
    "      - The number of eneies must be greater than or equal to 1")
  val enemyNumValidation: Int => Boolean = (num: Int) => num >= 1

  val startPointPrompt: String = blue("Enter the start point (x, y): ")
  val startPointErrorPrompt: String = red("     **Invalid start point. Please enter one valid coordinate pair. \n" +
    "      - The coordinate y == 1, the rightmost \n" +
    "      - The coordinate x <= grid size and x >= 1")
  val startPointValidation: (Int, Int) => Boolean = (x: Int, y: Int) => x >= 1 && x <= UserInput.gridNum && y == 1

  val endPointPrompt: String = blue("Enter the end point (x, y): ")
  val endPointErrorPrompt: String = red("     **Invalid end point. Please enter one valid coordinate pair. \n" +
    "      - The coordinate x <= grid size and x >= 1 \n" +
    "      - The coordinate y == grid size, the leftmost")
  val endPointValidation: (Int, Int) => Boolean = (x: Int, y: Int) => x <= UserInput.gridNum && x >= 1 && y == UserInput.gridNum

  val towerLocationPrompt: String = blue("Please locate the tower (x, y): ")
  val towerLocationErrorPrompt: String = red("      **Invalid tower location. Please enter one valid coordinate pair. \n" +
    "      - The coordinate x <= grid size and x >= 1 \n" +
    "      - The coordinate y <= grid size and y >= 1 \n" +
    "      - (x, y) must not be the same as start point and end point")
  val towerLocationValidation: (Int, Int) => Boolean = (x: Int, y: Int) => x >= 1 && x <= UserInput.gridNum && y >= 1 && y <= UserInput.gridNum
    && (x, y) != UserInput.startPt && (x, y) != UserInput.endPt
}
