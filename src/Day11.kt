//private typealias Coordinate = Pair<Int, Int>


fun main() {
    val empty = '.'
    val veryEmpty = 'o'
    val galaxy = '#'


    class GalaxyMap(val symbols: List<List<Char>>) {
        fun getTotalDistance(stretchFactor: Long = 2): Long {
            fun getSymbolValue(s: Char) = if (s == veryEmpty) stretchFactor else 1

            val galaxyCoordinates = symbols
                .withIndex()
                .flatMap { (indRow, row) ->
                    row
                        .withIndex()
                        .filter { it.value == galaxy }
                        .map { Pair(indRow, it.index) }
                }

            var sum = 0L
            for (ind1 in 0..<galaxyCoordinates.lastIndex) {
                val coordinate1 = galaxyCoordinates[ind1]
                for (ind2 in ind1 + 1..galaxyCoordinates.lastIndex) {
                    val coordinate2 = galaxyCoordinates[ind2]
                    val xRange =
                        if (coordinate2.first >= coordinate1.first) coordinate1.first..coordinate2.first else coordinate2.first..coordinate1.first
                    val yRange =
                        if (coordinate2.second >= coordinate1.second) coordinate1.second..coordinate2.second else coordinate2.second..coordinate1.second
                    val dist = (xRange.sumOf { indX -> getSymbolValue(symbols[indX][coordinate1.second]) } - 1
                            + yRange.sumOf { indY -> getSymbolValue(symbols[coordinate1.first][indY]) } - 1)
                    sum += dist
                }
            }
            return sum
        }
    }

    fun parseInput(input: List<String>): GalaxyMap {
        val indsEmptyCols = (0..input.first().lastIndex)
            .filter { indCol -> (0..input.lastIndex).all { indRow -> input[indRow][indCol] == empty } }
        val extendedInput = input
            .map { row ->
                row.mapIndexed { indCol, c ->
                    if (indCol in indsEmptyCols) veryEmpty else c
                }
            }
            .map { row -> if (row.all { it == empty || it == veryEmpty }) row.map { veryEmpty } else row }
        return GalaxyMap(extendedInput)
    }


    fun part1(input: List<String>): Long {
        val galaxyMap = parseInput(input)
        return galaxyMap.getTotalDistance()
    }

    fun part2(input: List<String>, stretchFactor: Long): Long {
        val galaxyMap = parseInput(input)
        return galaxyMap.getTotalDistance(stretchFactor)
    }

// test if implementation meets criteria from the description, like:
    val input = readInput("Day11")
    val testInput = readInput("Day11_test")
    check(part1(testInput) == 374L) { "Wrong solution: ${part1(testInput)}" }
    part1(input).println()

    check(part2(testInput, 10) == 1030L) { "Wrong solution: ${part2(testInput, 10)}" }
    check(part2(testInput, 100) == 8410L) { "Wrong solution: ${part2(testInput, 100)}" }
    part2(input, 1_000_000).println()
}
