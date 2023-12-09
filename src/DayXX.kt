fun main() {
    data class Helper(val line: String)


    fun parseInput(input: List<String>): List<Helper> {
        return input.map { Helper(it) }
    }

    fun part1(input: List<String>): Long {
        return parseInput(input).size.toLong()
    }

    fun part2(input: List<String>): Long {
        return part1(input)
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("DayXX_test")
    check(part1(testInput) == 1L) { "Wrong solution: ${part1(testInput)}" }
    check(part2(testInput) == 1L) { "Wrong solution: ${part2(testInput)}" }

    val input = readInput("DayXX")
    part1(input).println()
    part2(input).println()
}
