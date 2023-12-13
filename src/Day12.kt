import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

fun main() {
    val symWorking = '.'
    val symBroken = '#'
    val symUnknown = '?'

    data class InputLine(val springs: String, val counts: List<Int>)

    fun Char.couldBeWorking() = this == symWorking || this == symUnknown
    fun Char.couldBeBroken() = this == symBroken || this == symUnknown

    fun getSolutionCount(pattern: String, expectedGroups: List<Int>): Long {
        val cache = mutableMapOf<Pair<Int, Int>, Long>()

        fun evaluateSubSolution(indCharGroupStart: Int, indGroup: Int): Long {
            // Fulfilled all groups, check if rest could be all working.
            val key = Pair(indCharGroupStart, indGroup)
            cache[key]?.also { return it }

            if (indGroup > expectedGroups.lastIndex) {
                return if (indCharGroupStart > pattern.lastIndex
                    || pattern.substring(indCharGroupStart..pattern.lastIndex)
                        .all { it.couldBeWorking() }
                ) {
                    1L
                } else {
                    0L
                }.also { cache[key] = it }
            }

            val thisGroupSize = expectedGroups[indGroup]
            if (indCharGroupStart + thisGroupSize - 1 > pattern.lastIndex) {
                cache[key] = 0L
                return 0L
            }

            val countWithShiftedStart = if (pattern[indCharGroupStart].couldBeWorking()) {
                evaluateSubSolution(indCharGroupStart + 1, indGroup)
            } else {
                0
            }

            if (!pattern[indCharGroupStart].couldBeBroken()) {
                return countWithShiftedStart.also { cache[key] = it }
            }


            val indAfterGroup = indCharGroupStart + thisGroupSize
            return (countWithShiftedStart + if (pattern.substring(indCharGroupStart..<indAfterGroup)
                    .all { it.couldBeBroken() }
                && pattern.getOrNull(indAfterGroup)?.couldBeWorking() != false
            ) {
                evaluateSubSolution(indAfterGroup + 1, indGroup + 1)
            } else {
                0L
            }).also { cache[key] = it }
        }
        return evaluateSubSolution(0, 0)
    }

    fun parseInput(input: List<String>): List<InputLine> {
        return input.map {
            val splitLine = it.split(" ")
            InputLine(splitLine[0], getUnsignedIntsFromString(splitLine[1]).toList())
        }
    }

    fun solve(input: List<String>, repeat: Int): Long {
        val lines = parseInput(input)
        val solutionCounts = runBlocking(Dispatchers.Default) {
            lines.map { line ->
                val lineString = (1..repeat).joinToString(symUnknown.toString()) { line.springs }
                val groups = (1..repeat).flatMap { line.counts }
                getSolutionCount(lineString, groups)
            }
        }
        return solutionCounts.sum()
    }

    fun part1(input: List<String>): Long {
        return solve(input, 1)
    }

    fun part2(input: List<String>): Long {
        return solve(input, 5)
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day12_test")
    val input = readInput("Day12")
    check(part1(testInput) == 21L) { "Wrong solution: ${part1(testInput)}" }
    part1(input).println()

    check(part2(testInput) == 525152L) { "Wrong solution: ${part2(testInput)}" }
    part2(input).println()
}
