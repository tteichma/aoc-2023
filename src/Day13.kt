fun main() {
    data class MirroredMap(val data: List<String>) {
        override fun toString(): String {
            return data.joinToString("\n")
        }

        fun getHorizontalMirrorIndices() = (1..data.lastIndex).filter {
            data.subList(0, it).reversed().zip(data.subList(it, data.size))
                .all { line -> line.first == line.second }
        }

        fun getVerticalMirrorIndices() = (1..data.first().lastIndex).filter {
            data.all { row ->
                row.substring(0, it).reversed().zip(row.substring(it, row.length))
                    .all { line -> line.first == line.second }
            }
        }

        fun yieldFuzzed() = iterator {
            for (iRow in 0..data.lastIndex) {
                for (iCol in 0..data[iRow].lastIndex) yield(
                    MirroredMap(
                        data.mapIndexed { indRow, row ->
                            if (indRow != iRow) row else {
                                val c = if (row[iCol] == '#') '.' else '#'
                                row.replaceRange(iCol, iCol + 1, c.toString())
                            }
                        }
                    )
                )
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
        return mirroredMaps.sumOf {
            val horizontal = it.getHorizontalMirrorIndices().firstOrNull() ?: 0
            val vertical = it.getVerticalMirrorIndices().firstOrNull() ?: 0
            100 * horizontal + vertical
        }.toLong()
    }

    fun part2(input: List<String>): Long {
        val mirroredMaps = parseInput(input)
        return mirroredMaps.sumOf { mm ->
            val originalHorizontal = mm.getHorizontalMirrorIndices()
            val originalVertical = mm.getVerticalMirrorIndices()

            var score: Long = 0
            for (mmf in mm.yieldFuzzed()) {
                val horizontal =
                    mmf.getHorizontalMirrorIndices().filterNot { it in originalHorizontal }.firstOrNull() ?: 0
                val vertical = mmf.getVerticalMirrorIndices().filterNot { it in originalVertical }.firstOrNull() ?: 0

                val newScore = 100 * horizontal + vertical
                if (newScore > 0) {
                    score = newScore.toLong()
                    break
                }
            }
            score
        }
    }

// test if implementation meets criteria from the description, like:
    val testInput = readInput("Day13_test")
    check(part1(testInput) == 405L) { "Wrong solution: ${part1(testInput)}" }
    check(part2(testInput) == 400L) { "Wrong solution: ${part2(testInput)}" }

    val input = readInput("Day13")
    part1(input).println()
    part2(input).println()
}
