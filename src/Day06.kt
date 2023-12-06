fun main() {
    data class Record(val time: Long, val distance: Long) {
        fun getWinPossibilities() = (0..time).filter { (time - it) * it > distance }
    }

    fun parseInput(input: List<String>): Sequence<Record> {
        val times = numberRegex.findAll(input[0].split(':')[1]).map { it.groupValues[0].toLong() }
        val distances = numberRegex.findAll(input[1].split(':')[1]).map { it.groupValues[0].toLong() }
        return times.zip(distances).map { Record(it.first, it.second) }
    }

    fun part1(input: List<String>): Int {
        val records = parseInput(input)
        return records.map { it.getWinPossibilities().size }.reduce { acc, i -> acc * i }
    }

    fun part2(input: List<String>): Int {
        return part1(input.map { it.replace(" ", "") })
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day06_test")
    check(part1(testInput) == 288) { "Wrong solution: ${part1(testInput)}" }
    check(part2(testInput) == 71503) { "Wrong solution: ${part2(testInput)}" }

    val input = readInput("Day06")
    part1(input).println()
    part2(input).println()
}
