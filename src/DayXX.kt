fun main() {
    fun part1(input: List<String>): Int {
        return input.size
    }

    fun part2(input: List<String>): Int {
        return part1(input)
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("DayXX_test")
    check(part1(testInput) == 1) { "Wrong solution: ${part1(testInput)}" }
    check(part2(testInput) == 1) { "Wrong solution: ${part2(testInput)}" }

    val input = readInput("DayXX")
    part1(input).println()
    part2(input).println()
}
