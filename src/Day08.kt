fun main() {
    val lineRegex = Regex("""(...) = \((...), (...)\)""")

    data class Routing(val instructions: List<Boolean>, val edges: Map<String, Pair<String, String>>)

    class Rider(firstDestDistance: Long, val distsBetweenDestNodes: List<Long>) {
        val nextDistanceOffsetIterator = distsBetweenDestNodes.asSequence().repeatInfinitely().iterator()

        private var _currentDistance = firstDestDistance
        val currentDistance
            get() = _currentDistance

        fun advanceToNextDest() {
            _currentDistance += nextDistanceOffsetIterator.next()
        }
    }

    fun createRider(routing: Routing, start: String, destRegex: Regex): Rider {
        var currentLocation = start
        var count = 0L

        var distToFirstDest = 0L
        val _distsBetweenDestNodes = mutableListOf<Long>()
        val visitedDestNodes = mutableSetOf<String>()

        val isNextRightSequence = routing.instructions.asSequence().repeatInfinitely().iterator()
        fun isNextRight() = isNextRightSequence.next()

        if (destRegex.matches(start)) {
            distToFirstDest = 0
            visitedDestNodes.add(start)
        }

        while (true) {
            val edge = routing.edges[currentLocation]!!
            currentLocation = if (isNextRight()) edge.second else edge.first
            ++count
            if (destRegex.matches(currentLocation)) {
                if (visitedDestNodes.isEmpty()) {
                    distToFirstDest = count
                } else {
                    _distsBetweenDestNodes.add(count)
                    if (currentLocation in visitedDestNodes) {
                        break
                    }
                }
                visitedDestNodes.add(currentLocation)
                count = 0
            }

        }

        return Rider(distToFirstDest, _distsBetweenDestNodes)
    }

    fun mergeRiders(riders: List<Rider>): List<Rider> {
        val ridersWithSingleDest = riders.filter { it.distsBetweenDestNodes.size == 1 }
        // Don't know how to combine
        val ridersWithMultipleDests = riders.filter { it.distsBetweenDestNodes.size > 1 }

        if (ridersWithSingleDest.size == 1) return ridersWithSingleDest + ridersWithMultipleDests

        val mergedRiders = ridersWithSingleDest
            .windowed(2, step = 2, partialWindows = false)
            .map { twoRiders ->
                while (twoRiders.map { it.currentDistance }.toSet().size > 1) {
                    riders.minByOrNull { it.currentDistance }!!.advanceToNextDest()
                }
                val distBetweenDestNode =
                    getLcm(twoRiders[0].distsBetweenDestNodes[0], twoRiders[1].distsBetweenDestNodes[0])
                Rider(twoRiders.first().currentDistance, listOf(distBetweenDestNode))
            }

        return if (ridersWithSingleDest.size % 2 == 1) {
            mergeRiders(mergedRiders + ridersWithSingleDest.last() + ridersWithMultipleDests)
        } else {
            mergeRiders(mergedRiders + ridersWithMultipleDests)
        }
    }

    fun parseInput(input: List<String>): Routing {
        val instructions = input.first().map {
            when (it) {
                'L' -> false
                'R' -> true
                else -> throw RuntimeException("Invalid direction input $it")
            }
        }

        val directions = input
            .drop(2).associate {
                val match = lineRegex.matchEntire(it)!!.groupValues
                match[1] to Pair(match[2], match[3])
            }

        return Routing(instructions, directions)
    }


    fun solve(input: List<String>, startRegex: Regex, destRegex: Regex): Long {
        val routing = parseInput(input)
        val originalRiders = routing.edges.keys
            .filter { startRegex.matches(it) }
            .map { createRider(routing, it, destRegex) }

        val riders = mergeRiders(originalRiders)

        if (riders.size == 1) return riders.first().currentDistance

        while (riders.map { it.currentDistance }.toSet().size > 1) {
            riders.minByOrNull { it.currentDistance }!!.advanceToNextDest()
        }

        return riders.first().currentDistance
    }

    fun part1(input: List<String>): Long {
        return solve(input, Regex("AAA"), Regex("ZZZ"))
    }

    fun part2(input: List<String>): Long {
        return solve(input, Regex(".*A"), Regex(".*Z"))
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day08_test")
    check(part1(testInput) == 6L)
    { "Wrong solution: ${part1(testInput)}" }
    check(part2(testInput) == 6L)
    { "Wrong solution: ${part2(testInput)}" }
    val testInput2 = readInput("Day08_testB")
    check(part2(testInput) == 6L)
    { "Wrong solution: ${part2(testInput2)}" }

    val input = readInput("Day08")
    part1(input).println()
    part2(input).println()
}
