fun main() {
    data class Card(val face: Char, val value: Int)

    fun cardFromChar(c: Char): Card {
        return when (c) {
            'A' -> Card(c, 14)
            'K' -> Card(c, 13)
            'Q' -> Card(c, 12)
            'J' -> Card(c, 11)
            'T' -> Card(c, 10)
            '9' -> Card(c, 9)
            '8' -> Card(c, 8)
            '7' -> Card(c, 7)
            '6' -> Card(c, 6)
            '5' -> Card(c, 5)
            '4' -> Card(c, 4)
            '3' -> Card(c, 3)
            '2' -> Card(c, 2)
            '*' -> Card(c, 0)
            else -> throw RuntimeException("Unknown card '$c'")
        }
    }

    data class Hand(val cards: List<Card>, val bid: Int) : Comparable<Hand> {
        fun getScore(): Int {
            val groupedByFace = cards.groupBy { it.face }
            val numsSameCards = if ('*' in groupedByFace && groupedByFace['*']!!.size != 5) {
                val cardCounts = groupedByFace.map { it.key to it.value.size }.toMap().toMutableMap()
                val delta = cardCounts.remove('*')!!
                val mostFrequentEntry = cardCounts.maxBy { it.value }
                cardCounts[mostFrequentEntry.key] = cardCounts[mostFrequentEntry.key]!! + delta
                cardCounts.map { it.value }
            } else {
                groupedByFace.map { it.value.size }
            }
            return when {
                5 in numsSameCards -> 7
                4 in numsSameCards -> 6
                3 in numsSameCards -> if (2 in numsSameCards) 5 else 4 // Full house / three of a kind.
                2 in numsSameCards -> if (numsSameCards.count { it == 2 } > 1) 3 else 2 // Two / one pair
                else -> 1
            }
        }

        override fun compareTo(other: Hand): Int {
            val scoreComp = this.getScore().compareTo(other.getScore())
            if (scoreComp != 0) return scoreComp
            for ((thisCard, otherCard) in this.cards.zip(other.cards)) {
                val cardComp = thisCard.value.compareTo(otherCard.value)
                if (cardComp != 0) return cardComp
            }
            return 0
        }
    }

    fun parseInput(input: List<String>) =
        input.map { line ->
            val (cardsString, bidString) = line.split(' ')
            Hand(cardsString.map { cardFromChar(it) }, bidString.toInt())
        }

    fun part1(input: List<String>): Int {
        val hands = parseInput(input)
        return hands.sorted().withIndex().sumOf { (it.index + 1) * it.value.bid }
    }

    fun part2(input: List<String>): Int {
        val hands = parseInput(input.map { it.replace('J', '*') })
        return hands.sorted().withIndex().sumOf { (it.index + 1) * it.value.bid }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day07_test")
    check(part1(testInput) == 6440) { "Wrong solution: ${part1(testInput)}" }
    check(part2(testInput) == 5905) { "Wrong solution: ${part2(testInput)}" }

    val input = readInput("Day07")
    part1(input).println()
    part2(input).println()
}
