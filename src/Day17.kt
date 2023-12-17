import java.util.PriorityQueue

fun main() {
    data class NodeToVisit(
        val coordinate: IntCoordinate,
        val direction: Direction,
        val movesInDirection: Int,
        val currentCost: Long
    )

    class HeatMap(data: List<List<Int>>) : DataMap<Int>(data) {
        private val nodeComparator = Comparator<NodeToVisit> { a, b ->
            val costComparison = a.currentCost.compareTo(b.currentCost)
            if (costComparison == 0) return@Comparator a.movesInDirection.compareTo(b.movesInDirection)
            return@Comparator costComparison
        }

        fun getMinimalHeatLoss(startNode: NodeToVisit, dest: IntCoordinate, allowedSameDirMoves: Int): Long {
            val visitedNodes = mutableSetOf<Triple<IntCoordinate, Direction, Int>>()
            val upcomingNodes = PriorityQueue(data.size + data.first().size, nodeComparator)

            upcomingNodes.add(startNode)
            while (upcomingNodes.isNotEmpty()) {
                val currentNode = upcomingNodes.poll()

                if (currentNode.coordinate == dest) {
                    return currentNode.currentCost
                }
                val currentTriple = Triple(currentNode.coordinate, currentNode.direction, currentNode.movesInDirection)
                if (currentTriple in visitedNodes) {
                    continue
                } else {
                    visitedNodes.add(currentTriple)
                }

                val nextDirections = ((
                        if (currentNode.movesInDirection < allowedSameDirMoves) listOf(
                            Pair(currentNode.direction, currentNode.movesInDirection + 1)
                        ) else listOf())
                        + currentNode.direction.getRightAngleDirs().map { Pair(it, 1) })

                nextDirections
                    .mapNotNull {
                        val nextCoordinate = it.first.nextCoordinate(currentNode.coordinate).also { coordinate ->
                            if (!coordinate.isWithinBoundaries()) return@mapNotNull null
                        }
                        NodeToVisit(nextCoordinate, it.first, it.second, currentNode.currentCost + data[nextCoordinate])
                    }
                    .forEach { upcomingNodes.add(it) }
            }

            return 0L
        }
    }


    fun parseInput(input: List<String>): HeatMap {
        return HeatMap(input.map { row -> row.map { it.digitToInt() } })
    }

    fun part1(input: List<String>): Long {
        val heatMap = parseInput(input)
        return heatMap.getMinimalHeatLoss(
            NodeToVisit(Pair(0, 0), Direction.LR, 1, 0),
            Pair(heatMap.lastRowIndex, heatMap.lastColIndex), 3
        )
    }

    fun part2(input: List<String>): Long {
        return part1(input)
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day17_test")
    check(part1(testInput) == 102L) { "Wrong solution: ${part1(testInput)}" }
    check(part2(testInput) == 102L) { "Wrong solution: ${part2(testInput)}" }

    val input = readInput("Day17")
    part1(input).println()
    part2(input).println()
}
