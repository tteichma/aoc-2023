fun main() {
    val lineRegex = Regex("""Game (?<id>\d+): (?<draws>.*)""")
    val drawRegex = Regex("""(?:(?<r>\d+) red|(?<g>\d+) green|(?<b>\d+) blue|,| )*""")

    data class CubeSet(val r: Int, val g: Int, val b: Int)
    data class Game(val id: Int, val draws: List<CubeSet>) {
        fun isGamePossible(maxRed: Int, maxGreen: Int, maxBlue: Int) =
            draws.all { it.r <= maxRed && it.g <= maxGreen && it.b <= maxBlue }

        fun getSmallestPossibleSet() = CubeSet(draws.maxOf { it.r }, draws.maxOf { it.g }, draws.maxOf { it.b })
    }

    fun parseDraw(input: String): CubeSet {
        val match = drawRegex.matchEntire(input)
        return CubeSet(
            r = match?.groups?.get("r")?.value?.toInt() ?: 0,
            g = match?.groups?.get("g")?.value?.toInt() ?: 0,
            b = match?.groups?.get("b")?.value?.toInt() ?: 0
        )
    }

    fun parseGame(line: String): Game {
        val groups = lineRegex.matchEntire(line)!!.groups
        val gameId = groups["id"]!!.value.toInt()
        val draws = groups["draws"]!!.value.split(";").map { parseDraw(it) }
        return Game(gameId, draws)
    }

    fun part1(input: List<String>): Int {
        val games = input.map { parseGame(it) }
        return games.filter { it.isGamePossible(12, 13, 14) }.sumOf { it.id }
    }

    fun part2(input: List<String>): Int {
        val games = input.map { parseGame(it) }
        val smallestSet = games.map { it.getSmallestPossibleSet() }
        return smallestSet.sumOf { it.r * it.g * it.b }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day02_test")
    check(part1(testInput) == 8) { "Wrong solution: ${part1(testInput)}" }
    check(part2(testInput) == 2286) { "Wrong solution: ${part2(testInput)}" }

    val input = readInput("Day02")
    part1(input).println()
    part2(input).println()
}
