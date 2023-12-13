fun main() {
    data class MirroredMap(val data: List<String>) {
        fun getHorizontalMirrorIndex() = (1..data.lastIndex)
            .firstOrNull {
                data
                    .subList(0, it)
                    .reversed()
                    .zip(data.subList(it, data.size))
                    .all { line -> line.first == line.second }
            }

        fun getVerticalMirrorIndex() = (1..data.first().lastIndex)
            .firstOrNull {
                data
                    .all { row ->
                        row.substring(0, it)
                            .reversed()
                            .zip(row.substring(it, row.length))
                            .all { line -> line.first == line.second }
                    }
            }
    }


    fun parseInput(input: List<String>): List<MirroredMap> {
        val indsEmpty = listOf(-1) + input.mapIndexedNotNull { index, s ->
            if (s.isEmpty()) index else null
        } + listOf(input.size)

        return indsEmpty.windowed(2).map { MirroredMap(input.subList(it[0] + 1, it[1])) }
    }

    fun part1(input: List<String>): Long {
        val mirroredMaps = parseInput(input)
        return mirroredMaps.sumOf { (100L * (it.getHorizontalMirrorIndex() ?: 0) + (it.getVerticalMirrorIndex() ?: 0)) }

    }

    fun part2(input: List<String>): Long {
        return part1(input)
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day13_test")
    check(part1(testInput) == 405L) { "Wrong solution: ${part1(testInput)}" }
    check(part2(testInput) == 405L) { "Wrong solution: ${part2(testInput)}" }

    val input = readInput("Day13")
    part1(input).println()
    part2(input).println()
}
