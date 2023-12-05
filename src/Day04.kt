fun main() {
    data class ScratchCard(val id: Int, val cardNumbers: Set<Int>, val ownNumbers: Set<Int>) {
        fun getNumMatches(): Int = cardNumbers.intersect(ownNumbers).size
        fun getScore(): Int {
            val numMatches = getNumMatches()
            return if (numMatches > 0) {
                var score = 1
                repeat(numMatches - 1)
                {
                    score *= 2
                }
                return score
            } else 0
        }
    }

    val numRegex = Regex("""\d+""")
    val lineRegex = Regex("""Card *(?<num>\d+): (?<card>[^|]+)\|(?<own>[^|]+)""")

    fun parseLine(input: String): ScratchCard {
        val groups = lineRegex.matchEntire(input)!!.groups
        return ScratchCard(
            groups["num"]!!.value.toInt(),
            numRegex.findAll(groups["card"]!!.value).flatMap { m -> m.groupValues.map { it.toInt() } }.toSet(),
            numRegex.findAll(groups["own"]!!.value).flatMap { m -> m.groupValues.map { it.toInt() } }.toSet()
        )
    }

    fun part1(input: List<String>): Int {
        val cards = input.map { parseLine(it) }
        return cards.sumOf { it.getScore() }
    }

    fun part2(input: List<String>): Int {
        val cards = input.map { parseLine(it) }
        val cardCounts = cards.map { it.id to 1 }.toMap().toMutableMap()
        for (card in cards) {
            for (iWin in 1..card.getNumMatches()) {
                val wonId = card.id + iWin
                if (wonId in cardCounts) {
                    cardCounts[wonId] = cardCounts[wonId]!! + cardCounts[card.id]!!
                }
            }
        }
        return cardCounts.values.sum()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day04_test")
    check(part1(testInput) == 13) { "Wrong solution: ${part1(testInput)}" }
    check(part2(testInput) == 30) { "Wrong solution: ${part2(testInput)}" }

    val input = readInput("Day04")
    part1(input).println()
    part2(input).println()
}
