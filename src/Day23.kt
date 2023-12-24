fun main() {
    val symWall = '#'

    data class Edge(val dest: IntCoordinate, val firstDirection: Direction, val length: Int)

    data class RouteExplorationItem(
        val coordinate: IntCoordinate,
        val cost: Long,
        val visited: Set<IntCoordinate>
    )

    class Labyrinth(data: List<List<Char>>) : DataMap<Char>(data) {
        val edges = createEdges()

        fun getStart() = Pair(0, data.first().indexOfFirst { it != symWall })
        fun getDest() = Pair(lastRowIndex, data.last().indexOfFirst { it != symWall })

        private fun getAvailableDirectionsAndReversibility(
            coordinate: IntCoordinate,
            originalDirection: Direction? = null
        ) =
            Direction.entries
                .mapNotNull {
                    val nextCoordinate = it.nextCoordinate(coordinate)
                    if (it == originalDirection?.opposite || !nextCoordinate.isWithinBoundaries() || data[nextCoordinate] == symWall) return@mapNotNull null
                    when (data[coordinate]) {
                        '>' -> if (it == Direction.LR) Pair(it, false) else null
                        '<' -> if (it == Direction.RL) Pair(it, false) else null
                        'v' -> if (it == Direction.UD) Pair(it, false) else null
                        '^' -> if (it == Direction.DU) Pair(it, false) else null
                        '.' -> Pair(it, true)
                        else -> throw RuntimeException("Unhandled symbol $it")
                    }
                }

        private fun createEdges(): Map<IntCoordinate, List<Edge>> {
            val mutableEdges = mutableMapOf<IntCoordinate, MutableList<Edge>>()

            val directionsToExplore =
                mutableSetOf<Pair<IntCoordinate, Direction>>(Pair(getStart(), Direction.UD))

            while (directionsToExplore.isNotEmpty()) {
                val (startCoordinate, startDirection) = directionsToExplore.pop()!!
                if (mutableEdges[startCoordinate]?.any { it.firstDirection == startDirection } == true) {
                    continue
                }

                var counter = 0
                var coordinate = startCoordinate
                var direction = startDirection
                var isSegmentReversible = true
                var nextDirections: List<Pair<Direction, Boolean>> = listOf(Pair(startDirection, true))
                while (nextDirections.size == 1) {
                    ++counter
                    val (newDirection, isStepReversible) = nextDirections.first()
                    direction = newDirection
                    isSegmentReversible = isSegmentReversible && isStepReversible
                    coordinate = direction.nextCoordinate(coordinate)
                    nextDirections = getAvailableDirectionsAndReversibility(coordinate, direction)
                }
                mutableEdges.getOrPut(startCoordinate) { mutableListOf() }
                    .add(Edge(coordinate, startDirection, counter))
                if (isSegmentReversible) {
                    mutableEdges.getOrPut(coordinate) { mutableListOf() }
                        .add(Edge(startCoordinate, direction.opposite, counter))
                }
                nextDirections.forEach { directionsToExplore.add(Pair(coordinate, it.first)) }
            }

            return mutableEdges
        }

        fun getLongestPathLength(): Long {
            val routesToExplore = mutableSetOf(RouteExplorationItem(getStart(), 0L, setOf()))
            val dest = getDest()
            var highestCost = 0L

            while (routesToExplore.isNotEmpty()) {
                val (coordinate, cost, alreadyVisited) = routesToExplore.pop()!!

                if (coordinate == dest && cost > highestCost) {
                    highestCost = cost
                }

                for (edge in edges[coordinate] ?: listOf()) {
                    if (edge.dest in alreadyVisited) {
                        continue
                    } else {
                        routesToExplore.add(
                            RouteExplorationItem(
                                edge.dest,
                                cost + edge.length,
                                alreadyVisited + setOf(coordinate)
                            )
                        )
                    }
                }
            }
            return highestCost
        }
    }


    fun parseInput(input: List<String>): Labyrinth {
        return Labyrinth(input.map { it.toList() })
    }

    fun part1(input: List<String>): Long {
        val labyrinth = parseInput(input)
        return labyrinth.getLongestPathLength()
    }

    fun part2(input: List<String>): Long {
        val labyrinth =
            parseInput(input.map { line -> line.map { if (it == symWall) symWall else '.' }.joinToString("") })
        return labyrinth.getLongestPathLength()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day23_test")
    check(part1(testInput) == 94L) { "Wrong solution: ${part1(testInput)}" }
    check(part2(testInput) == 154L) { "Wrong solution: ${part2(testInput)}" }

    val input = readInput("Day23")
    part1(input).println()
    part2(input).println()
}
