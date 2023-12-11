private class Day10ReachedEmptyFieldException : Exception()

private enum class Day10Direction {
    U, D, L, R;

    fun getNextCoordinate(currentCoordinate: IntCoordinate) = when (this) {
        U -> Pair(currentCoordinate.first - 1, currentCoordinate.second)
        D -> Pair(currentCoordinate.first + 1, currentCoordinate.second)
        L -> Pair(currentCoordinate.first, currentCoordinate.second - 1)
        R -> Pair(currentCoordinate.first, currentCoordinate.second + 1)
    }

    fun getOpposite() = when (this) {
        U -> D
        D -> U
        L -> R
        R -> L
    }
}

private sealed class Day10Corner(val coordinate: IntCoordinate, val name: String) {
    abstract fun getNeighbours(): List<Day10Corner>

    class TL(coordinate: IntCoordinate) : Day10Corner(coordinate, "TL") {
        override fun getNeighbours() = listOf(
            TR(Pair(coordinate.first, coordinate.second - 1)),
            BL(Pair(coordinate.first - 1, coordinate.second)),
            BR(Pair(coordinate.first - 1, coordinate.second - 1)),
        )
    }

    class TR(coordinate: IntCoordinate) : Day10Corner(coordinate, "TR") {
        override fun getNeighbours() = listOf(
            TL(Pair(coordinate.first, coordinate.second + 1)),
            BR(Pair(coordinate.first - 1, coordinate.second)),
            BL(Pair(coordinate.first - 1, coordinate.second + 1)),
        )
    }

    class BL(coordinate: IntCoordinate) : Day10Corner(coordinate, "BL") {
        override fun getNeighbours() = listOf(
            BR(Pair(coordinate.first, coordinate.second - 1)),
            TL(Pair(coordinate.first + 1, coordinate.second)),
            TR(Pair(coordinate.first + 1, coordinate.second - 1)),
        )
    }

    class BR(coordinate: IntCoordinate) : Day10Corner(coordinate, "BR") {
        override fun getNeighbours() = listOf(
            BL(Pair(coordinate.first, coordinate.second + 1)),
            TR(Pair(coordinate.first + 1, coordinate.second)),
            TL(Pair(coordinate.first + 1, coordinate.second + 1)),
        )
    }

    companion object {
        fun createByName(name: String, coordinate: IntCoordinate): Day10Corner {
            return when (name) {
                "TL" -> TL(coordinate)
                "TR" -> TR(coordinate)
                "BL" -> BL(coordinate)
                "BR" -> BR(coordinate)
                else -> throw RuntimeException()
            }
        }
    }
}

private sealed class Day10Pipe(
    private val symbol: Char,
    private val directions: List<Day10Direction>,
    private val cornerGroups: List<List<String>>
) {
    object UD : Day10Pipe('|', listOf(Day10Direction.U, Day10Direction.D), listOf(listOf("TL", "BL"), listOf("TR", "BR")))
    object LR : Day10Pipe('-', listOf(Day10Direction.L, Day10Direction.R), listOf(listOf("TL", "TR"), listOf("BL", "BR")))
    object UR : Day10Pipe('L', listOf(Day10Direction.U, Day10Direction.R), listOf(listOf("TL", "BL", "BR"), listOf("TR")))
    object UL : Day10Pipe('J', listOf(Day10Direction.U, Day10Direction.L), listOf(listOf("TR", "BL", "BR"), listOf("TL")))
    object DL : Day10Pipe('7', listOf(Day10Direction.D, Day10Direction.L), listOf(listOf("TL", "TR", "BR"), listOf("BL")))
    object DR : Day10Pipe('F', listOf(Day10Direction.D, Day10Direction.R), listOf(listOf("TL", "TR", "BL"), listOf("BR")))
    object EMPTY : Day10Pipe('.', listOf(), listOf(listOf("TL", "TR", "BL", "BR")))
    object START : Day10Pipe('S', listOf(), listOf(listOf("TL"), listOf("TR"), listOf("BL"), listOf("BR")))

    override fun toString(): String {
        return "Pipe ${this.symbol}"
    }

    fun getOtherConnectedCorners(corner: Day10Corner) = cornerGroups
        .first { corner.name in it }
        .filterNot { corner.name == it }
        .map { Day10Corner.createByName(it, corner.coordinate) }

    fun getNextDirection(previousDirection: Day10Direction): Day10Direction? {
        val incomingDirection = previousDirection.getOpposite()
        if (incomingDirection !in this.directions) {
            if (this is START) {
                return null
            } else {
                throw Day10ReachedEmptyFieldException()
            }
        }
        return this.directions.first { it != incomingDirection }
    }


    companion object {
        fun createBySymbol(symbol: Char) = when (symbol) {
            '|' -> UD
            '-' -> LR
            'L' -> UR
            'J' -> UL
            '7' -> DL
            'F' -> DR
            'S' -> START
            '.' -> EMPTY
            else -> throw RuntimeException()
        }
    }
}

fun main() {
    class PipeMap(val pipes: List<List<Day10Pipe>>) {
        // null -> Not a loop
        fun getPipe(coordinate: IntCoordinate) = pipes[coordinate.first][coordinate.second]

        fun isCoordinateValid(coordinate: IntCoordinate) =
            (coordinate.first in 0..pipes.lastIndex && coordinate.second in 0..pipes.first().lastIndex)

        fun getDistancesFromLocation(
            startCoordinate: IntCoordinate,
            startDirection: Day10Direction
        ): Map<IntCoordinate, Int>? {
            var currentCoordinate = startCoordinate
            var currentDirection = startDirection
            var counter = 0
            val result = mutableMapOf(startCoordinate to 0)
            try {
                do {
                    result[currentCoordinate] = counter
                    val nextCoordinate = currentDirection.getNextCoordinate(currentCoordinate)
                    val nextPipe = pipes[nextCoordinate.first][nextCoordinate.second]
                    currentDirection = nextPipe.getNextDirection(currentDirection) ?: break
                    currentCoordinate = nextCoordinate
                    ++counter
                } while (currentCoordinate !in result)
            } catch (e: Day10ReachedEmptyFieldException) {
                return null
            }

            return result.toMap()
        }

        fun getNumEnclosedCells(cornerName: String): Int? {
            val start = findStart()
            val coordinatesInLoop = Day10Direction.entries.firstNotNullOf { getDistancesFromLocation(start, it) }.keys

            val visitedCoordinates = mutableSetOf(start)

            val openSet = Day10Corner.createByName(cornerName, start).getNeighbours().toSet().toMutableSet()
            while (openSet.isNotEmpty()) {
                val currentCorner = openSet.pop()!!
                if (currentCorner.coordinate in visitedCoordinates) continue
                val expansionPipe =
                    if (currentCorner.coordinate in coordinatesInLoop) getPipe(currentCorner.coordinate) else Day10Pipe.EMPTY
                for (newCorner in expansionPipe.getOtherConnectedCorners(currentCorner)
                    .flatMap { it.getNeighbours() }) {
                    when {
                        !isCoordinateValid(newCorner.coordinate) -> return null
                        newCorner.coordinate in visitedCoordinates -> continue
                    }
                    openSet.add(newCorner)
                }
                visitedCoordinates.add(currentCorner.coordinate)
            }
            return visitedCoordinates.count { it !in coordinatesInLoop }
        }


        fun findStart() = pipes
            .withIndex()
            .filter { row -> row.value.any { it is Day10Pipe.START } }
            .map { row -> Pair(row.index, row.value.indexOfFirst { it is Day10Pipe.START }) }
            .first()
    }


    fun parseInput(input: List<String>): PipeMap {
        val origPipes = input.map { line ->
            line.map { Day10Pipe.createBySymbol(it) }
        }
        val pipesWithBorder = listOf(List(origPipes.first().size + 2) { Day10Pipe.EMPTY }) + origPipes.map { row ->
            listOf(Day10Pipe.EMPTY) + row + listOf(Day10Pipe.EMPTY)
        } + listOf(List(origPipes.first().size + 2) { Day10Pipe.EMPTY })
        return PipeMap(pipesWithBorder)
    }

    fun part1(input: List<String>): Int {
        val pipeMap = parseInput(input)
        val start = pipeMap.findStart()

        val distancesToLocations = Day10Direction.entries.mapNotNull { pipeMap.getDistancesFromLocation(start, it) }

        check(distancesToLocations.size == 2)
        return distancesToLocations
            .flatMap { it.asSequence() }
            .groupBy { it.key }
            .mapValues { itOneDirection -> itOneDirection.value.minOf { it.value } }
            .maxOf { it.value }
    }

    fun part2(input: List<String>): Int {
        val pipeMap = parseInput(input)
        return sequenceOf("TL", "TR", "BL", "BR").map { pipeMap.getNumEnclosedCells(it) }.filterNotNull().first()
    }

    // test if implementation meets criteria from the description, like:
    val testInputA = readInput("Day10_testA")
    val testInputB = readInput("Day10_testB")
    val input = readInput("Day10")
    check(part1(testInputA) == 4) { "Wrong solution: ${part1(testInputA)}" }
    check(part1(testInputB) == 8) { "Wrong solution: ${part1(testInputB)}" }
    part1(input).println()

    val testInputC = readInput("Day10_testC")
    val testInputD = readInput("Day10_testD")
    val testInputE = readInput("Day10_testE")
    check(part2(testInputA) == 1) { "Wrong solution: ${part2(testInputA)}" }
    check(part2(testInputB) == 1) { "Wrong solution: ${part2(testInputB)}" }
    check(part2(testInputC) == 4) { "Wrong solution: ${part2(testInputC)}" }
    check(part2(testInputD) == 8) { "Wrong solution: ${part2(testInputD)}" }
    check(part2(testInputE) == 10) { "Wrong solution: ${part2(testInputE)}" }
    part2(input).println()
}
