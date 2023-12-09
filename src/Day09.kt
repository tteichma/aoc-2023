fun main() {
    fun List<Long>.extrapolate(): List<Long> {
        return if (this.all { it == 0L }) {
            this + listOf(0L)
        } else {
            val differences = this
                .windowed(2)
                .map { it[1] - it[0] }
            this + listOf(this.last() + differences.extrapolate().last())
        }
    }

    fun parseInput(input: List<String>): List<List<Long>> {
        return input.map { line ->
            signedIntegerRegex.findAll(line)
                .map { it.groupValues[0].toLong() }
                .toList()
        }
    }

    fun part1(input: List<String>): Long {
        val sequences = parseInput(input)
        return sequences.sumOf { it.extrapolate().last() }
    }

    fun part2(input: List<String>): Long {
        val sequences = parseInput(input)
        return sequences.sumOf { it.reversed().extrapolate().last() }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day09_test")
    check(part1(testInput) == 114L) { "Wrong solution: ${part1(testInput)}" }
    check(part2(testInput) == 2L) { "Wrong solution: ${part2(testInput)}" }

    val input = readInput("Day09")
    part1(input).println()
    part2(input).println()
}
