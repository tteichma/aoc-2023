import java.util.PriorityQueue

fun main() {
    val symFree = '.'
    val symRock = '#'
    val symStart = 'S'

    class GardenTileMap(data: List<List<Boolean>>) : DataMap<Boolean>(data) {
        // true -> Can be moved to.
        // Repeats in all directions

        init {
            check(data.first().all { it }) { "Upper boundary of map must be accessible" }
            check(data.last().all { it }) { "Bottom boundary of map must be accessible" }
            check(data.all { it.first() }) { "Left boundary of map must be accessible" }
            check(data.all { it.last() }) { "Right boundary of map must be accessible" }
        }

        fun getCoordinatesToDistances(
            start: IntCoordinate
        ): MutableMap<IntCoordinate, Long> {
            // Coordinate -> Distance until first seen.
            val coordinatesToMinimumDistance = mutableMapOf<IntCoordinate, Long>()

            val upcomingCoordinates = PriorityQueue(16,
                Comparator<Pair<IntCoordinate, Long>> { a, b ->
                    return@Comparator a.second.compareTo(b.second)
                })

            var lastCost = 0L

            upcomingCoordinates.add(Pair(start, 0))
            while (upcomingCoordinates.isNotEmpty()) {
                val (currentCoordinate, currentCost) = upcomingCoordinates.poll()

                if (currentCost > lastCost && currentCost % 10 == 0L) {
                    lastCost = currentCost
                }

                if (currentCoordinate in coordinatesToMinimumDistance) {
                    continue
                } else {
                    coordinatesToMinimumDistance[currentCoordinate] = currentCost
                }

                Direction.entries
                    .mapNotNull {
                        it.nextCoordinate(currentCoordinate)
                            .also { intCoordinate ->
                                if (!intCoordinate.isWithinBoundaries() || !data[intCoordinate]) return@mapNotNull null
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

    fun getBestCoordinateDistancesAtEdge(sortedCoordDists: List<Pair<IntCoordinate, Long>>) = listOfNotNull(
        if (sortedCoordDists[0].second < sortedCoordDists[1].second) sortedCoordDists[0] else null
    ) + sortedCoordDists.windowed(3).filter { it[0].second > it[1].second && it[1].second < it[2].second }
        .map { it[1] } + listOfNotNull(
        if (sortedCoordDists[sortedCoordDists.lastIndex - 1].second > sortedCoordDists[sortedCoordDists.lastIndex].second) sortedCoordDists.last() else null
    )

    data class CachedResult(val coordinatesToDistances: Map<IntCoordinate, Long>, private val tileMap: GardenTileMap) {
        val distanceCounts = coordinatesToDistances.toList()
            .groupBy { it.second }
            .mapValues { it.value.size }

        val edgeDistancesToTry: Map<Direction, List<Pair<IntCoordinate, Long>>>

        init {
            val sortedDistances =
                coordinatesToDistances.toList().sortedWith(compareBy({ it.first.first }, { it.first.second }))
            edgeDistancesToTry = mapOf(
                Direction.DU to sortedDistances.filter { it.first.first == 0 },
                Direction.UD to sortedDistances.filter { it.first.first == tileMap.lastRowIndex },
                Direction.RL to sortedDistances.filter { it.first.second == 0 },
                Direction.LR to sortedDistances.filter { it.first.second == tileMap.lastColIndex }
            ).mapValues { getBestCoordinateDistancesAtEdge(it.value) }
        }

        val maxDistance = distanceCounts.keys.max()
        private val totalEven = distanceCounts.filter { it.key.isEven() }.values.sum().toLong()
        private val totalOdd = distanceCounts.filter { it.key.isOdd() }.values.sum().toLong()

        fun getReachableWithinDistance(distance: Long): Long {
            if (distance >= maxDistance) {
                return if (distance.isEven()) totalEven else totalOdd
            }

            fun Map<Long, Int>.sumWithinDistance() = this.filter { it.key <= distance }.values.sum().toLong()

            val filteredDistances =
                if (distance.isEven()) {
                    distanceCounts.filter { it.key.isEven() }
                } else {
                    distanceCounts.filter { it.key.isOdd() }
                }

            return filteredDistances.sumWithinDistance()
        }
    }

    class TiledGardenMap(data: List<List<Boolean>>) {
        val tileMap = GardenTileMap(data)

        private val resultCache = mutableMapOf<Set<Pair<Int, IntCoordinate>>, CachedResult>()

        fun getCachedResult(offsetsAndCoordinates: Set<Pair<Int, IntCoordinate>>): CachedResult {
            return resultCache.getOrPut(offsetsAndCoordinates) {
                if (offsetsAndCoordinates.size == 1) {
                    val (offset, coordinate) = offsetsAndCoordinates.first()
                    assert(offset == 0)
                    val coordinatesToDistances = tileMap.getCoordinatesToDistances(coordinate)
                    CachedResult(coordinatesToDistances, tileMap)
                } else {
                    val combinedCoordinatesToDistances = offsetsAndCoordinates.map { (offset, coordinate) ->
                        getCachedResult(
                            setOf(
                                Pair(
                                    0,
                                    coordinate
                                )
                            )
                        ).coordinatesToDistances.mapValues { it.value + offset }
                    }.reduce { acc, map ->
                        acc.keys.associateWith {
                            if (acc.getValue(it) <= map.getValue(it)) acc.getValue(it) else map.getValue(it)
                        }
                    }

                    CachedResult(combinedCoordinatesToDistances, tileMap)
                }
            }
        }

        fun countReachableCoordinates(start: IntCoordinate, maxSteps: Long, allowShortCut: Boolean = false): Long {
            val visitedTiles = mutableSetOf<IntCoordinate>()  // Coordinates OF tile, not IN tile!

            val tileStepsRemainingResults = ArrayDeque<Triple<IntCoordinate, Long, CachedResult>>(16)
            val startTileResult = getCachedResult(setOf(Pair(0, start)))

            var reachable = 0L

            if (allowShortCut) {
                /*
                 *  The shortcut is for huge datasets that have the following characteristics of the actual input:
                 *      1.  The map tiles are quadratic.
                 *      2.  The start is exactly in the center.
                 *      3.  The straight horizontal / vertical path from the start is always free.
                 *     (4.) The tile borders are always free (but this is a requirement also without the shortcut).
                 */
                val startToEdge = start.first
                val tileSize = tileMap.rowSize

                check(tileMap.colSize == tileSize)
                check(start.second == startToEdge)
                check(2 * startToEdge + 1 == tileSize)
                check(tileMap.data[start.first].all { it })
                check(tileMap.data.all { it[start.second] })

                val maxDistInTile = listOf(
                    Pair(0, 0), Pair(0, startToEdge), Pair(0, tileSize - 1),
                    Pair(startToEdge, 0), Pair(startToEdge, tileSize - 1),
                    Pair(tileSize - 1, 0), Pair(tileSize - 1, startToEdge), Pair(tileSize - 1, tileSize - 1),
                ).maxOf { getCachedResult(setOf(Pair(0, it))).maxDistance }
                val rangeFilledTiles = ((maxSteps - maxDistInTile) / tileSize - 1).toInt()

                if (rangeFilledTiles <= 0) {
                    tileStepsRemainingResults.add(Triple(Pair(0, 0), maxSteps, startTileResult))
                } else {
                    val reachableInFullEvenTile = startTileResult.getReachableWithinDistance(maxSteps)
                    val reachableInFullOddTile = startTileResult.getReachableWithinDistance(maxSteps + 1)

                    reachable += startTileResult.getReachableWithinDistance(maxSteps)
                    visitedTiles.add(Pair(0, 0))

                    visitedTiles.addAll((0..rangeFilledTiles).flatMap {
                        listOf(
                            Pair(it, rangeFilledTiles - it), Pair(it, it - rangeFilledTiles),
                            Pair(-it, rangeFilledTiles - it), Pair(-it, it - rangeFilledTiles),
                        )
                    })

                    val distToPerpendicularTile = (startToEdge + 1 + rangeFilledTiles * tileSize).toLong()

                    if (startToEdge % 2 == 1) {
                        // It takes an odd number of steps to get to a perpendicular cell's start point.
                        reachable += (4 * (rangeFilledTiles - rangeFilledTiles / 2) * reachableInFullOddTile)
                        reachable += (4 * (rangeFilledTiles / 2) * reachableInFullEvenTile)
                    } else {
                        reachable += (4 * (rangeFilledTiles - rangeFilledTiles / 2) * reachableInFullEvenTile)
                        reachable += (4 * (rangeFilledTiles / 2) * reachableInFullOddTile)
                    }

                    tileStepsRemainingResults.add(
                        Triple(
                            Pair(-(rangeFilledTiles + 1), 0),
                            maxSteps - distToPerpendicularTile,
                            getCachedResult(setOf(Pair(0, Pair(tileSize - 1, startToEdge))))
                        )
                    )
                    tileStepsRemainingResults.add(
                        Triple(
                            Pair(+(rangeFilledTiles + 1), 0),
                            maxSteps - distToPerpendicularTile,
                            getCachedResult(setOf(Pair(0, Pair(0, startToEdge))))
                        )
                    )
                    tileStepsRemainingResults.add(
                        Triple(
                            Pair(0, -(rangeFilledTiles + 1)),
                            maxSteps - distToPerpendicularTile,
                            getCachedResult(setOf(Pair(0, Pair(startToEdge, tileSize - 1))))
                        )
                    )
                    tileStepsRemainingResults.add(
                        Triple(
                            Pair(0, +(rangeFilledTiles + 1)),
                            maxSteps - distToPerpendicularTile,
                            getCachedResult(setOf(Pair(0, Pair(startToEdge, 0))))
                        )
                    )

                    if (rangeFilledTiles > 1) {
                        val distToDiagonalTile = (2 * startToEdge + 2 + (rangeFilledTiles - 1) * tileSize).toLong()
                        // It always takes an odd number of steps to get to the first diagonal cell's start point.
                        // There is one less diagonal tile than distance than perpendicular per sector.
                        val rftM1 = (rangeFilledTiles - 1).toLong()
                        //     Sum of all odd numbers until n is (n/2+1)^2
                        reachable += (4 * ((rftM1-1) / 2 + 1) * ((rftM1-1) / 2 + 1) * reachableInFullEvenTile)
                        //     Sum of all 2*i for i in 1 to n is n*(n+1)
                        reachable += (4 * (rftM1 / 2) * (rftM1 / 2 + 1) * reachableInFullOddTile)

                        for (it in 0..<rangeFilledTiles) {
                            tileStepsRemainingResults.add(
                                Triple(
                                    Pair(-(it + 1), -(rangeFilledTiles - it)),
                                    maxSteps - distToDiagonalTile,
                                    getCachedResult(setOf(Pair(0, Pair(tileSize - 1, tileSize - 1))))
                                )
                            )
                            tileStepsRemainingResults.add(
                                Triple(
                                    Pair(-(it + 1), +(rangeFilledTiles - it)),
                                    maxSteps - distToDiagonalTile,
                                    getCachedResult(setOf(Pair(0, Pair(tileSize - 1, 0))))
                                )
                            )
                            tileStepsRemainingResults.add(
                                Triple(
                                    Pair(+(it + 1), -(rangeFilledTiles - it)),
                                    maxSteps - distToDiagonalTile,
                                    getCachedResult(setOf(Pair(0, Pair(0, tileSize - 1))))
                                )
                            )
                            tileStepsRemainingResults.add(
                                Triple(
                                    Pair(+(it + 1), +(rangeFilledTiles - it)),
                                    maxSteps - distToDiagonalTile,
                                    getCachedResult(setOf(Pair(0, Pair(0, 0))))
                                )
                            )
                        }
                    }
                }
            } else {
                tileStepsRemainingResults.add(Triple(Pair(0, 0), maxSteps, startTileResult))
            }

            while (tileStepsRemainingResults.isNotEmpty()) {
                val (tile, stepsRemaining, result) = tileStepsRemainingResults.removeFirst()

                if (tile in visitedTiles) continue

                reachable += result.getReachableWithinDistance(stepsRemaining)
                visitedTiles.add(tile)

                for ((direction, edgeDistances) in result.edgeDistancesToTry) {
                    val nextTile = tile + direction
                    if (nextTile in visitedTiles) continue

                    val minEdgeDistanceP1 = edgeDistances.minOf { it.second } + 1
                    if (minEdgeDistanceP1 > stepsRemaining) continue

                    val normalizedEdgeDistanceSet = edgeDistances.map { (coordinate, distance) ->
                        Pair(
                            (distance + 1 - minEdgeDistanceP1).toInt(),
                            when (direction) {
                                Direction.UD -> IntCoordinate(0, coordinate.second)
                                Direction.DU -> IntCoordinate(tileMap.lastRowIndex, coordinate.second)
                                Direction.LR -> IntCoordinate(coordinate.first, 0)
                                Direction.RL -> IntCoordinate(coordinate.first, tileMap.lastColIndex)
                            },
                        )
                    }.toSet()
                    tileStepsRemainingResults.add(
                        Triple(
                            nextTile,
                            stepsRemaining - minEdgeDistanceP1,
                            getCachedResult(normalizedEdgeDistanceSet)
                        )
                    )
                }
            }

            return reachable
        }
    }


    fun parseInput(input: List<String>): Pair<TiledGardenMap, IntCoordinate> {
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
        return Pair(TiledGardenMap(data), startCoordinate!!)
    }

    fun solve(input: List<String>, numSteps: Long, allowShortCut: Boolean = false): Long {
        val (gardenMap, startCoordinate) = parseInput(input)
        return gardenMap.countReachableCoordinates(startCoordinate, numSteps, allowShortCut = allowShortCut)
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day21_test")
    profiledCheck(expected = 16u, "6 steps") { solve(testInput, 6) }
    profiledCheck(expected = 50u, "10 steps") { solve(testInput, 10) }
    profiledCheck(expected = 1594u, "50 steps") { solve(testInput, 50) }
    profiledCheck(expected = 6536u, "100 steps") { solve(testInput, 100) }
    profiledCheck(expected = 167004u, "500 steps") { solve(testInput, 500) }
    profiledCheck(expected = 668697u, "1000 steps") { solve(testInput, 1000) }
    profiledCheck(expected = 16733044u, "5000 steps") { solve(testInput, 5000) }

    val input = readInput("Day21")
    profiledExecute("part 1") { solve(input, 64) }.println()
    profiledExecute("part 2") { solve(input, 26501365, allowShortCut = true) }.println()
}
