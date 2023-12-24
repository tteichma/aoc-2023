import java.util.PriorityQueue

fun main() {
    val symFree = '.'
    val symRock = '#'
    val symStart = 'S'

    data class InfiniteCoordinate(val coordinate: IntCoordinate, val rowOffset: Int, val colOffset: Int)

    class InfiniteGardenMap(data: List<List<Boolean>>) : DataMap<Boolean>(data) {
        // true -> Can be moved to.
        // Repeats in all directions
        fun normalizeCoordinate(orig: InfiniteCoordinate): InfiniteCoordinate {
            val newRowOffset = when (orig.coordinate.first) {
                in Int.MIN_VALUE..<0 -> -1
                in rowSize..Int.MAX_VALUE -> 1
                else -> 0
            }
            val newColOffset = when (orig.coordinate.second) {
                in Int.MIN_VALUE..<0 -> -1
                in colSize..Int.MAX_VALUE -> 1
                else -> 0
            }
            return InfiniteCoordinate(
                Pair((orig.coordinate.first + rowSize) % rowSize, (orig.coordinate.second + colSize) % colSize),
                orig.rowOffset + newRowOffset, orig.colOffset + newColOffset
            )
        }

        fun countReachableCoordinates(start: IntCoordinate, maxSteps: Long): Long =
            getMinimumDistancesToCoordinates(start, maxSteps.toInt())
                .mapNotNull { if (it.value % 2 == maxSteps % 2) 1L else null }
                .sum()

        fun getMinimumDistancesToCoordinates(
            start: IntCoordinate,
            maxSteps: Int
        ): MutableMap<InfiniteCoordinate, Long> {
            // Coordinate -> Distance until first seen.
            val coordinatesToMinimumDistance = mutableMapOf<InfiniteCoordinate, Long>()
            val upcomingCoordinates = PriorityQueue(/* initialCapacity = */ maxSteps, /* comparator = */
                Comparator<Pair<InfiniteCoordinate, Long>> { a, b ->
                    return@Comparator a.second.compareTo(b.second)
                })


            var lastCost = 0L

            upcomingCoordinates.add(Pair(InfiniteCoordinate(start, 0, 0), 0))
            while (upcomingCoordinates.isNotEmpty()) {
                val (currentCoordinate, currentCost) = upcomingCoordinates.poll()

                if (currentCost > lastCost && currentCost % 10 == 0L) {
                    lastCost = currentCost
                }

                if (currentCost > maxSteps) break

                if (currentCoordinate in coordinatesToMinimumDistance) {
                    continue
                } else {
                    coordinatesToMinimumDistance[currentCoordinate] = currentCost
                }

                Direction.entries
                    .mapNotNull {
                        (normalizeCoordinate(
                            InfiniteCoordinate(
                                it.nextCoordinate(currentCoordinate.coordinate),
                                currentCoordinate.rowOffset,
                                currentCoordinate.colOffset
                            )
                        ))
                            .also { infCoordinate ->
                                if (!data[infCoordinate.coordinate]) return@mapNotNull null
                            }
                    }
                    .filterNot { it in coordinatesToMinimumDistance }
                    .forEach {
                        upcomingCoordinates.add(Pair(it, currentCost + 1))
                    }
            }
            return coordinatesToMinimumDistance
        }
    }

    fun parseInput(input: List<String>): Pair<InfiniteGardenMap, IntCoordinate> {
        var startCoordinate: IntCoordinate? = null
        val data = input
            .mapIndexed { iRow, row ->
                row.mapIndexed { iCol, c ->
                    when (c) {
                        symFree -> true
                        symRock -> false
                        symStart -> {
                            startCoordinate = Pair(iRow, iCol)
                            true
                        }

                        else -> throw RuntimeException()
                    }
                }
            }
            .map { if (it.size % 2 != 0) it + it else it }  // Double columns if not even number
            .let { if (it.size % 2 != 0) it + it else it }  // Double rows if not even number
        return Pair(InfiniteGardenMap(data), startCoordinate!!)
    }

    fun solve(input: List<String>, numSteps: Long): Long {
        val (gardenMap, startCoordinate) = parseInput(input)
        return gardenMap.countReachableCoordinates(startCoordinate, numSteps)
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day21_test")
    check(solve(testInput, 6) == 16L) { "Wrong solution: ${solve(testInput, 6)}" }
    println("6")
    check(solve(testInput, 10) == 50L) { "Wrong solution: ${solve(testInput, 10)}" }
    println("10")
    check(solve(testInput, 50) == 1594L) { "Wrong solution: ${solve(testInput, 50)}" }
    println("50")
    check(solve(testInput, 100) == 6536L) { "Wrong solution: ${solve(testInput, 100)}" }
    println("100")
    check(solve(testInput, 500) == 167004L) { "Wrong solution: ${solve(testInput, 500)}" }
    println("500")
    check(solve(testInput, 1000) == 668697L) { "Wrong solution: ${solve(testInput, 1000)}" }
    println("1000")
    check(solve(testInput, 5000) == 16733044L) { "Wrong solution: ${solve(testInput, 5000)}" }
    println("5000")

    val input = readInput("Day21")
    solve(input, 64).println()
    solve(input, 26501365).println()
}
