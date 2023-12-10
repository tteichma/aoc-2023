class Day10ReachedEmptyFieldException : Exception()

enum class Day10Direction {
    U, D, L, R;

    fun getNextCoordinate(currentCoordinate: Pair<Int, Int>) = when (this) {
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

sealed class Day10Pipe(private val symbol: Char, private val directions: List<Day10Direction>) {
    class UD : Day10Pipe('|', listOf(Day10Direction.U, Day10Direction.D))
    class LR : Day10Pipe('-', listOf(Day10Direction.L, Day10Direction.R))
    class UR : Day10Pipe('L', listOf(Day10Direction.U, Day10Direction.R))
    class UL : Day10Pipe('J', listOf(Day10Direction.U, Day10Direction.L))
    class DL : Day10Pipe('7', listOf(Day10Direction.D, Day10Direction.L))
    class DR : Day10Pipe('F', listOf(Day10Direction.D, Day10Direction.R))
    class EMPTY : Day10Pipe('.', listOf())
    class START : Day10Pipe('S', listOf())

    override fun toString(): String {
        return "Pipe ${this.symbol}"
    }

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
            '|' -> UD()
            '-' -> LR()
            'L' -> UR()
            'J' -> UL()
            '7' -> DL()
            'F' -> DR()
            'S' -> START()
            '.' -> EMPTY()
            else -> throw RuntimeException()
        }
    }
}

fun main() {
    class PipeMap(val pipes: List<List<Day10Pipe>>) {
        // null -> Not a loop
        fun getDistancesFromLocation(
            startCoordinate: Pair<Int, Int>,
            startDirection: Day10Direction
        ): Map<Pair<Int, Int>, Int>? {
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
        val pipesWithBorder = listOf(List(origPipes.first().size + 2) { Day10Pipe.EMPTY() }) + origPipes.map { row ->
            listOf(Day10Pipe.EMPTY()) + row + listOf(Day10Pipe.EMPTY())
        } + listOf(List(origPipes.first().size + 2) { Day10Pipe.EMPTY() })
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
        return part1(input)
    }

    // test if implementation meets criteria from the description, like:
    val testInputA = readInput("Day10_testA")
    val testInputB = readInput("Day10_testB")
    check(part1(testInputA) == 4) { "Wrong solution: ${part1(testInputA)}" }
    check(part1(testInputB) == 8) { "Wrong solution: ${part1(testInputB)}" }
    check(part2(testInputA) == 4) { "Wrong solution: ${part2(testInputA)}" }
    check(part2(testInputB) == 8) { "Wrong solution: ${part2(testInputB)}" }

    val input = readInput("Day10")
    part1(input).println()
    part2(input).println()
}
