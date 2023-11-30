fun main() {
    val replacements = mapOf(
//        "zero" to "0",
        "one" to "1",
        "two" to "2",
        "three" to "3",
        "four" to "4",
        "five" to "5",
        "six" to "6",
        "seven" to "7",
        "eight" to "8",
        "nine" to "9"
    )
    val startsWithNumberRegex = Regex("""^(${replacements.keys.joinToString("|")})""")

    fun replaceNumbersInStr(line: String): String {
        if (line.isEmpty()) {
            return ""
        }

        val numberPrefix = startsWithNumberRegex.matchAt(line, 0)?.groups?.firstOrNull()?.value
        return if (numberPrefix == null) {
            line[0] + replaceNumbersInStr(line.drop(1))
        } else {
            replacements[numberPrefix] + replaceNumbersInStr(line.drop(1))  // overlaps
//            replacements[numberPrefix] + replaceNumbersInStr(line.drop(numberPrefix.length))  // no overlaps
        }
    }

    fun part1(input: List<String>): Int {
        return input.sumOf { line ->
            line.filter { it.isDigit() }.let { 10 * it.first().digitToInt() + it.last().digitToInt() }
        }
    }

    fun part2(input: List<String>): Int {
        return part1(input.map { replaceNumbersInStr(it) })
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day01_test")
    check(part1(testInput) == 209) { "Wrong solution: ${part1(testInput)}" }
    check(part2(testInput) == 281) { "Wrong solution: ${part2(testInput)}" }

    val input = readInput("Day01")
    part1(input).println()
    part2(input).println()
}
