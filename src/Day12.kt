import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlin.time.measureTime

fun main() {
    val symWorking = '.'
    val symBroken = '#'
    val symUnknown = '?'

    data class InputLine(val springs: String, val counts: List<Int>)

    // Function assumes sting only contains WORKING or BROKEN
    fun getActualGroupsFromString(filled: String, isIncomplete: Boolean): List<Int> {
        if (filled.isEmpty()) return listOf()

        val groups = filled.split(symWorking).map { it.length }.filterNot { it == 0 }
        return if (isIncomplete && filled.last() == symBroken) groups.dropLast(1) else groups
    }

    fun getFilledQuestionMarks(
        filled: String,
        remaining: String,
        expectedGroups: List<Int>,
        missingBroken: Int,
        missingWorking: Int
    ): Sequence<String> =
        sequence {
            if (remaining.isEmpty()) {
                if (expectedGroups == getActualGroupsFromString(filled, isIncomplete = false)) {
                    yield(filled)
                }
                return@sequence
            }

            val actualGroups = getActualGroupsFromString(filled, isIncomplete = true)
            if (actualGroups.zip(expectedGroups).any { it.first != it.second }) return@sequence

            val splitRemaining = remaining.drop(1).split(symUnknown)
            val nextFilledChunk = splitRemaining.first()  // Between upcoming two unknowns
            val nextRemaining = if (splitRemaining.size == 1) "" else symUnknown + splitRemaining.drop(1)
                .joinToString(symUnknown.toString())
            if (missingWorking < 0) {
                yieldAll(
                    getFilledQuestionMarks(
                        filled + symWorking + nextFilledChunk,
                        nextRemaining,
                        expectedGroups,
                        missingBroken,
                        missingWorking + 1
                    )
                )
            }
            if (missingBroken < 0) {
                yieldAll(
                    getFilledQuestionMarks(
                        filled + symBroken + nextFilledChunk,
                        nextRemaining,
                        expectedGroups,
                        missingBroken + 1,
                        missingWorking
                    )
                )
            }
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
            lines.pmap { line ->
                val lineString: String
                val groups: List<Int>
                val count: Long

                val duration = measureTime {
                    lineString = (1..repeat).joinToString(symUnknown.toString()) { line.springs }
                    groups = (1..repeat).flatMap { line.counts }

                    count = getFilledQuestionMarks(
                        lineString.takeWhile { it != symUnknown },
                        lineString.dropWhile { it != symUnknown },
                        groups,
                        lineString.count { it == symBroken } - groups.sum(),
                        lineString.count { it == symWorking } - (lineString.length - groups.sum())
                    ).count().toLong()
                }
//                println("${count}\t${duration.inWholeSeconds}s\t$lineString $groups")
                count
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
