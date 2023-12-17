import java.util.PriorityQueue

fun main() {
    data class State(
        val coordinate: IntCoordinate,
        val direction: Direction,
        val movesInDirection: Int,
    )

    abstract class HeatMap(data: List<List<Int>>) : DataMap<Int>(data) {
        abstract fun isStraightPossible(sameDirCount: Int): Boolean
        abstract fun isTurnPossible(sameDirCount: Int): Boolean
        abstract fun isEndPossible(sameDirCount: Int): Boolean

        private val stateComparator = Comparator<Pair<State, Long>> { a, b ->
            return@Comparator a.second.compareTo(b.second)
        }

        fun getMinimalHeatLoss() = listOf(Direction.LR, Direction.UD).minOf {
            getMinimalHeatLossForStartDirection(State(Pair(0, 0), it, 0), Pair(lastRowIndex, lastColIndex))
        }

        fun getMinimalHeatLossForStartDirection(startState: State, dest: IntCoordinate): Long {
            val visitedStates = mutableSetOf<State>()
            val upcomingStates = PriorityQueue(data.size + data.first().size, stateComparator)
            var count = 0

            upcomingStates.add(Pair(startState, 0))
            while (upcomingStates.isNotEmpty()) {
                ++count
                val (currentState, currentCost) = upcomingStates.poll()
                if (currentState.coordinate == dest && isEndPossible(currentState.movesInDirection)) {
                    return currentCost
                }
                if (currentState in visitedStates) {
                    continue
                } else {
                    visitedStates.add(currentState)
                }

                val nextDirections = ((
                        if (isStraightPossible(currentState.movesInDirection)) listOf(
                            Pair(currentState.direction, currentState.movesInDirection + 1)
                        ) else listOf())
                        + (if (isTurnPossible(currentState.movesInDirection))
                    currentState.direction.perpendicular.map { Pair(it, 1) }
                else listOf()))

                nextDirections
                    .mapNotNull {
                        val nextCoordinate = it.first.nextCoordinate(currentState.coordinate).also { coordinate ->
                            if (!coordinate.isWithinBoundaries()) return@mapNotNull null
                        }
                        State(nextCoordinate, it.first, it.second)
                    }
                    .filterNot { it in visitedStates }
                    .forEach {
                        upcomingStates.add(Pair(it, currentCost + data[it.coordinate]))
                    }
            }

            throw RuntimeException("Could not find any valid path.")
        }
    }

    class Part1HeatMap(data: List<List<Int>>) : HeatMap(data) {
        override fun isStraightPossible(sameDirCount: Int) = sameDirCount < 3
        override fun isTurnPossible(sameDirCount: Int) = true
        override fun isEndPossible(sameDirCount: Int) = true
    }

    class Part2HeatMap(data: List<List<Int>>) : HeatMap(data) {
        override fun isStraightPossible(sameDirCount: Int) = sameDirCount < 10
        override fun isTurnPossible(sameDirCount: Int) = sameDirCount >= 4
        override fun isEndPossible(sameDirCount: Int) = sameDirCount >= 4
    }

    fun part1(input: List<String>): Long {
        val heatMap = Part1HeatMap(input.map { row -> row.map { it.digitToInt() } })
        return heatMap.getMinimalHeatLoss()
    }

    fun part2(input: List<String>): Long {
        val heatMap = Part2HeatMap(input.map { row -> row.map { it.digitToInt() } })
        return heatMap.getMinimalHeatLoss()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day17_test")
    check(part1(testInput) == 102L) { "Wrong solution: ${part1(testInput)}" }
    check(part2(testInput) == 94L) { "Wrong solution: ${part2(testInput)}" }

    val input = readInput("Day17")
    part1(input).println()
    part2(input).println()
}
