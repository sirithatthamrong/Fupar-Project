# Tower Defense Enemy Pathfinding with A*
This project was completed as part of the term assignment for ICCS311: Functional and Parallel Programming by Kanladaporn Sirithatthamrong and Chada Kengradomying.

## Overview
This project is an implementation of the A* pathfinding algorithm, which aims to find the best path for the attacking enemies to reach the goal while preserving as much enemy counts as possible. The project focuses on efficiently navigating through a grid-based map to find optimal paths avoiding towers targeting them only when it is reasonable to do so. The project also aims to utilize the power of parallel programming to enhance and speed up the program while also preserving its original running time. 

## Contents
1. [Package: pathfinding](#pathfinding)
    - [AStarPathfinder](#AStarPathfinder) 
    - [BenchmarkTest](#BenchmarkTest)
    - [Heuristic](#Heuristic)
    - [Node](#Node)
    - [NodeGrid](#NodeGrid)
    - [Parallel5pAStarPathfinder](#Parallel5pAStarPathfinder)
    - [Sequential5pAStarPathfinder](#Sequential5pAStarPathfinder)
    - [Integration and Overall System Usage](#Integration-and-Overall-System-Usage)
    - [Final Thoughts on Parallelizing A*](#Final-Thoughts-on-Parallelizing-A*)
2. [Package: game](#game)
   - [Enemy](#Enemy)
   - [Main](#Main)
   - [ParallelGameLogic](#ParallelGameLogic)
   - [SequentialGameLogic](#SequentiallGameLogic)
   - [Tower](#Tower)
2. [Package: userInput](#userInput)
   - [getUserInput](#getUserInput)
   - [userPrompt](#userPrompt)

## Package: pathfinding
The `pathfinding` package is dedicated to finding the most efficient path for enemies in the Tower Defense game, by considering the distance to the exit and the tower locations. With the heuristics given, the enemies are able to weigh between taking a detour and walking into turret range and are also able to force its way through turrets if there are no better paths. 

### AStarPathfinder

`AStarPathfinder` is the base class for implementing the other A* pathfinding algorithms. It provides the essential functionalities required to calculate the optimal path in this game.

#### Methods
- **aStarSearch(start: Node, end: Node, nodeGrid: NodeGrid)**:
    - **Description**: Executes the A* algorithm. It expands nodes starting from the `start` node, progressing towards the `end` node, while considering the structure of the grid defined in `nodeGrid`.
    - **Input**: `start` (Node) - starting node, `end` (Node) - goal node, `nodeGrid` (NodeGrid) - grid structure containing nodes.
    - **Output**: List of Nodes representing the optimal path from start to end.

- **reconstructPath(endNode: Node)**:
    - **Description**: Reconstructs the path from the `endNode` to the start node by traversing backwards through each node’s parent.
    - **Input**: `endNode` (Node) - the goal node where the path ends.
    - **Output**: List of Nodes representing the path from start to end in reverse order.

- **findPath(startLocation: (Int, Int), endLocation: (Int, Int), towerLocations: Set[(Int, Int)], gridDimension: Int)**:
    - **Description**: Facilitates the pathfinding process by initializing the grid and executing the A* search.
    - **Input**: `startLocation` (tuple of Ints) - grid coordinates of the start location, `endLocation` (tuple of Ints) - grid coordinates of the end location, `towerLocations` (Set of tuples of Ints) - grid coordinates of tower locations, `gridDimension` (Int) - size of the grid.
    - **Output**: List of Nodes representing the path from the start location to the end location.

#### Usage
`AStarPathfinder` can be used directly for standard A* pathfinding requirements or as a base class for more complex pathfinding algorithms, such as those involving parallel computation or enhanced heuristics. To use it, instantiate the class, set up the grid and nodes, and call the `findPath` method with the necessary parameters. The method will compute and return the most efficient path based on the A* algorithm.

### BenchmarkTest
`BenchmarkTest` Benchmarks different A* algorithm implementations in the Tower Defense Enemy Pathfinding project. It evaluates the Parallel, Sequential, and Original A* versions to compare their execution times.
- **Parallel Pathfinding**: Utilizes `Parallel5pAStarPathfinder` to execute the A* algorithm in parallel. The performance is measured and outputted in seconds.
- **Sequential Pathfinding**: Uses `Sequential5pAStarPathfinder` for a sequential implementation of A*. The execution time is noted for comparison.
- **Original A\* Pathfinding**: Employs `AStarPathfinder` to run the standard A* algorithm. Its performance is also measured in terms of execution time.

Each pathfinder receives the same grid dimension, start and end points, and tower locations for a fair comparison.

### Heuristic
The `Heuristic` class is used in the A* algorithm for calculating the cost metrics, which include both the gCost and hCost. These calculations take into account the tower influences on the grid, which guides the pathfinding process.

#### Methods
- `calculateHCost(currentNode: Node, goalNode: Node)`: Calculates the heuristic cost (hCost) based on the Manhattan distance between the current node and the goal node. This distance helps in estimating how far the current node is from the goal.
- `calculateGCost(currentNode: Node, parentNode: Node)`: Computes the gCost, which represents the cost from the starting point to the current node. It factors in the basic travel cost, additional costs for being in the range of towers, and the heuristic cost to the parent node.

#### Usage
- The `Heuristic` class is utilized internally by the pathfinding algorithms (`Parallel5pAStarPathfinder` and `AStarPathfinder`) to evaluate and update the cost metrics of each node during the pathfinding process.
- This class enables the algorithm to make informed decisions about which nodes to explore next, based on their proximity to the goal and the risk of tower influence.
- The heuristic cost (hCost) provides a guess of the remaining distance to the goal, while the gCost represents the known cost of reaching the current node. Together, they influence the algorithm's choice of path in balancing efficiency and risk avoidance.

### Node
Represents each cell in the grid, containing crucial data for pathfinding such as position, costs, neighbors, and tower influence.
#### Properties
- Position (coordinates), gCost (cost from the start node), hCost (estimated cost to the goal), neighbors (adjacent nodes), isInTowerRange (boolean indicating tower influence), parent (previous node in the path), isTower (boolean indicating if the node is a tower).
#### Usage
- Nodes are fundamental elements in pathfinding, used to calculate and store path data.

### NodeGrid
Manages the game's grid, creating a 2D array of Node objects representing the game grid.
- **Input**: Start and end locations, tower locations, grid size.
- **Output**: A structured grid for pathfinding.
#### Usage
- Primarily used internally within pathfinder classes. Initializes the grid and connects neighboring nodes.

### Parallel5pAStarPathfinder
A concurrent implementation of the A* pathfinding algorithm which utilizes Scala's parallel computing capabilities. This class extends `AStarPathfinder` and processes nodes within the top 5% f-cost scores in parallel.
- **Input**: Start and end locations, tower locations, grid dimensions.
- **Output**: A Future containing a List of Nodes representing the path.
#### Methods
- **findPathParallel(startLocation: (Int, Int), endLocation: (Int, Int), towerLocations: Set[(Int, Int)], gridDimension: Int)**:
    - **Description**: Initiates the pathfinding process in a parallel manner, considering multiple nodes simultaneously for increased efficiency.
    - **Input**: Start and end locations (tuples of Ints), tower locations (Set of tuples of Ints), grid dimensions (Int).
    - **Output**: A Future containing a List of Nodes, representing the path calculated in a parallel fashion.

#### Usage
- Create an instance with the required parameters and call the `findPathParallel` method. The method returns a `Future`, which asynchronously computes the optimal path.

### Sequential5pAStarPathfinder
Implements the A* algorithm in a sequential manner. This class extends `AStarPathfinder` and processes nodes within the top 5% f-cost scores sequentially.

- **Input**: Start and end locations, tower locations, grid dimension.
- **Output**: List of Nodes representing the path.
#### Methods
- **findPathSequential(startLocation: (Int, Int), endLocation: (Int, Int), towerLocations: Set[(Int, Int)], gridDimension: Int)**:
    - **Description**: Executes the A* algorithm sequentially, processing one node at a time in the order determined by their f-costs.
    - **Input**: Start and end locations (tuples of Ints), tower locations (Set of tuples of Ints), grid dimensions (Int).
    - **Output**: List of Nodes representing the path from the start to the end location.

#### Usage
- Instantiate with the required parameters and call `findPath`.

### Integration and Overall System Usage
- **System Integration**: To use the pathfinding system, start by creating an instance of the pathfinder class (`Parallel5pAStarPathfinder` or `AStarPathfinder`), depending on the requirement for parallel processing.
- **Pathfinding Execution**: Call the `findPath` or `findPathParallel` method with the necessary parameters (start and end points, tower locations, grid size). The method will return the computed path as a list of nodes.
- **Visualizing and Utilizing the Path**: The returned path can be visualized or directly used in the game logic to navigate entities (like enemies) through the grid.

### Final Thoughts on Parallelizing A*
After benchmarking the parallelized A* algorithm, we found that the parallelized version ran slower than its sequential counterpart. This outcome is primarily attributed to the fact that our algorithm is not compute-intensive, and thus, the overhead of parallelizing the process outweighs its benefits. If the algorithm involved more complex heuristic functions, the parallelized version might outperform the sequential `Sequential5pAStarPathfinder`. The computation of all top 5% `fCost` nodes did not significantly alter the path outcome, likely due to the simplified logic in our enemy movement. This simplicity in movement logic means that the optimal path often correlates with the top `fCost` node, making the calculation of additional nodes within the 5% threshold excessive. Specifically, given our game's mechanics where enemies move one tile at a time in orthogonal directions, the `fCost` of potential nodes is either very similar or the nodes are not walkable. This similarity in `fCost` values diminishes the effectiveness of considering a broader range of nodes. A more complex game scenario with varied enemy movement patterns and more intricate heuristics might make the parallel A* algorithm more advantageous compared to the traditional and sequential A* implementations.

## Package: game
The `game` package is dedicated to combine the game logic with the best path that retrieved from `pathfinding`.

### Enemy
The `Enemy` case class is using for collecting data of each enemy in the game, providing the position and HP.

#### Method
- **updatePos(newRow: Int, newCol: Int): Unit** - Updating its position in each move
- **updateHP(): Unit** - If an enemy is being shot, then its HP decreases by 1 
- **isAlive():Boolean** - Checking whether is it alive.

### Tower
The `Tower` case class is using for collecting data of each tower in the game, providing the position and queue for shooting.

#### Method
- **addQueue(enemies: List[Enemy]): Unit** - Adding enemies to the queue
- **deleteQueue(targetEnemy: Enemy): Unit** - Deleting an enemy from the queue

### Main
`Main` is where we compare the sequential version with parallel version. All the variables from `getUserInput` will be used.

### ParallelGameLogic

The `ParallelGameLogic` class is where the game loop will be run in parallel. It will use the `Parallel5pAStarPathfinder` to find the best path.
   
- **Input**: grid dimension, number of enemy, start point, end point, tower position
- **Output**: print how many enemies died, check win or lose and how many moves have been used

#### Variables

- `gameEnemies: List[Enemy]` - Collect all the enemies that still alive
- `gameTowers: List[Tower]` - Collect all the towers
- `enemiesInTown` - The number of enemies that reach the endpoint

#### Method

- **checkWinOrLose(): Unit**
  - **Description**: If more than 30% of enemies enter the town, then it's a loss; otherwise, it's a win.
- **shooting(towers: List[Tower], enemies: List[Enemy]): Unit**
  - **Description**: Each tower has its own enemy queue. The first enemy that enters the tower's shooting range will be targeted first. Towers can have the same target enemy, unless that target is already engaged. In such cases, the target enemy will be shifted to the next enemy in the queue, if one exists.
- **updateTowerQueue(towers: List[Tower], enemies: List[Enemy]): Unit**
  - **Description**: In every move, the tower queue needs to be updated, and parallel processing will be used here
- **isInRange(tower: Tower, enemy: Enemy): Boolean**
  - **Description**: This is a helper function for `shooting` to determine whether an enemy enters the tower's shooting range
- **checkEnemyAtEndPoint(enemy: Enemy): Unit**
  - **Description**: If an enemy is at the end point, then `enemiesInTown` will increment by 1
- **moveEach(num: Int, bestPath: List[Node]): Unit**
  - **Description**: Enemies are added to the game one by one, occupying one grid per enemy. The moves follow `bestPath`
- **gameLoop(): Unit**
  - **Description**: Main game loop run here

### SequentialGameLogic

The `SequentialGameLogic` class is responsible for running the game loop sequentially. It will use the `Sequential5pAStarPathfinder` to find the best path, utilizing the same set of variables and methods, but without parallel processing.

## Package: userInput

### userPrompt

Using for validation and error prompt if an input is not in an expected format.

### getUserInput

Using for reading the stdin from the terminal.

#### Variables

- `gridNum: Int`
- `enemyNum: Int`
- `startPt: (Int, Int)`
- `endPt: (Int, Int)`
- `towerLocation: List[(Int, Int)]`

### Benchmark Test

We run benchmark in `Main`
```scala
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
```
We test with **100 x 100**

#### 100x100

![100 x 100](https://github.com/sirithatthamrong/Tower-Defense-Enemy-Pathfinding/assets/122991005/f67f7d53-ee54-4bd9-951e-7f92c29053a1)

- `StartPt`: (1,1)
- `EndPt`: (100,100)
- `EnemyNum`: 80
- `TowerPos`: (1,2), (2,2), (3,2), (4,2), (5,2), (6,2), (2,3), (2,4), (2,5), (2,6), (2,7), (8,1), (8,2), (8,3), (10,4), (10,5), (10,6), (10,7), (10,8), (10,9), (10,10), (10,11), (10,12), (10,13), (10,14), (10,15), (1,16), (2,16), (3,16), (4,16), (5,16), (6,16), (7,16), (8,16), (9,16), (10,16), (12,3), (12,18), (13,18), (15,2), (15,3), (15,4), (15,5), (15,6), (15,7), (15,8), (15,9), (15,10), (15,11), (15,12), (15,13), (15,14), (15,15), (15,16), (15,17), (15,18), (15,19), (15,20), (15,21), (17,1), (16,21), (17,21), (18,21), (19,21), (20,21), (21,21), (22,21), (23,21), (24,21), (25,21), (26,21), (27,21), (28,21), (29,21), (45,1), (50,1), (50,2), (50,3), (50,4), (50,5), (47,2), (47,3), (47,4), (47,5), (47,6), (47,7), (47,8), (47,9), (47,10), (47,11), (47,12), (47,13), (47,14), (47,15), (47,16), (47,17), (47,18), (47,19), (47,20), (47,21), (47,22), (47,23), (47,24), (47,25), (47,26), (47,27), (47,28), (47,29), (47,30), (47,31), (47,32), (47,33), (47,34), (47,35), (47,36), (47,37), (47,38), (47,39), (47,40), (47,41), (47,42), (47,43), (47,44), (47,45), (47,46), (47,47), (47,48), (47,49), (47,50), (47,51), (47,52), (47,53), (47,54), (47,55), (47,56), (47,57), (47,58), (47,59), (47,60), (46,61), (45,99), (47,62), (47,63), (47,64), (47,65), (47,66), (47,67), (47,68), (47,69), (47,70), (47,71), (47,72), (47,73), (47,74), (47,75), (47,76), (47,77), (47,78), (47,79), (47,80), (47,81), (47,82), (47,83), (47,84), (47,85), (47,86), (47,87), (47,88), (47,89), (47,90), (47,91), (47,92), (47,93), (47,94), (47,95), (47,96), (47,97), (47,98), (47,99), (45,99), (50,59), (51,59), (52,59), (53,59), (54,59), (55,59), (56,59), (57,59), (58,59), (59,59), (50,62), (50,63), (50,64), (50,65), (50,66), (50,96), (50,97), (50,98), (50,99), (50,100), (62,3), (62,4), (62,5), (62,6), (62,7), (62,8), (62,9), (62,10), (62,11), (62,12), (62,13), (62,14), (62,15), (62,16), (62,17), (62,18), (62,19), (62,20), (62,21), (62,22), (62,23), (62,24), (62,25), (62,26), (62,27), (62,28), (62,29), (62,30), (62,31), (62,32), (62,33), (62,34), (62,35), (62,36), (62,37), (62,38), (62,39), (62,40), (62,41), (62,42), (62,43), (62,44), (62,45), (62,46), (62,47), (62,48), (62,49), (62,50), (62,51), (62,52), (62,53), (62,54), (62,55), (62,56), (62,57), (62,58), (62,59), (62,60), (62,61), (62,62), (62,63), (62,64), (62,65), (62,66), (62,67), (62,68), (62,69), (62,70), (62,71), (62,72), (62,73), (62,74), (62,75), (62,76), (62,77), (62,78), (62,79), (62,80), (62,81), (62,82), (62,83), (62,84), (62,85), (62,86), (62,87), (62,88), (62,89), (62,90), (62,91), (62,92), (62,93), (62,94), (62,95), (62,96), (62,97), (62,98), (62,99), (62,100), (64,2), (65,3), (66,4), (65,8), (65,9), (65,10), (68,6), (69,7), (70,8), (71,9), (71,2), (71,3), (71,4), (74,2), (74,3), (74,4), (74,5), (74,6), (74,7), (74,8), (74,9), (74,10), (74,11), (74,12), (74,13), (77,2), (77,3), (77,4), (65,14), (66,14), (67,14), (68,14), (69,14), (70,14), (71,14), (72,14), (73,14), (68,24), (69,24), (70,24), (71,24), (72,24), (73,24), (74,24), (75,24), (76,24), (77,24), (78,24), (79,24), (80,24), (81,24), (82,24), (83,24), (84,24), (85,24), (86,24), (87,24), (88,24), (89,24), (90,24), (91,24), (92,24), (93,24), (94,24), (95,24), (96,24), (97,24), (98,24), (99,24), (100,24), (66,31), (67,31), (68,31), (69,31), (70,31), (71,31), (72,31), (73,31), (74,31), (75,31), (76,31), (77,31), (78,31), (79,31), (80,31), (81,31), (82,31), (83,31), (84,31), (85,31), (86,31), (87,31), (88,31), (89,31), (90,31), (91,31), (92,31), (93,31), (94,31), (95,31), (96,31), (97,31), (98,31), (99,31), (100,31), (65,28), (66,28), (67,28), (68,28), (69,28), (70,28), (71,28), (72,28), (64,33), (77,82), (78,82), (79,82), (80,82), (81,82), (82,82), (83,82), (84,82), (85,82), (86,82), (87,82), (88,82), (89,82), (90,82), (91,82), (92,82), (93,82), (94,82), (95,82), (96,82), (97,82), (98,82), (99,82), (100,82), (94,94), (95,94), (96,94), (97,94), (98,94), (99,94), (100,94), (65,18), (65,19), (65,20), (66,22), (66,23), (68,17), (68,18), (68,19), (95,84), (95,85), (95,86), (95,87), (95,88), (95,89), (95,90), (95,91), (95,92), (95,93)

```
----------------------------------------------------------------------------
You Win! The town is safe ´•‿•`
    - 70 enemies are dead
    - The best path uses 227 moves
Without Parallel Version: 0.482 seconds
----------------------------------------------------------------------------
You Win! The town is safe ´•‿•`
    - 70 enemies are dead
    - The best path uses 227 moves
Parallel Version: 0.314 seconds
```

Now Let's try with a larger grid like **2000 x 2000**
#### 2000x2000
- `StartPt`: (1,1)
- `EndPt`: (2000,2000)
- `EnemyNum`: 500
- `TowerPos`: Same TowerPos as **100 x 100**

```
----------------------------------------------------------------------------
You Lose! Too many enemies reached the town ╥_╥
    - 2 enemies are dead
    - The best path uses 4005 moves
Without Parallel Version: 85.158 seconds
----------------------------------------------------------------------------
You Lose! Too many enemies reached the town ╥_╥
    - 2 enemies are dead
    - The best path uses 4005 moves
Parallel Version: 83.093 seconds
```

### Requirements
- Scala
- Java Virtual Machine
