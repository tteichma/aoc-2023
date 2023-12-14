@Suppress("unused")
enum class Day14TiltDirection(val rotations: Int) {
    E(0), S(1), W(2), N(3)
}

fun main() {
    val symEmpty = '.'
    val symFixed = '#'
    val symLoose = 'O'


    data class BoulderMap(private var data: List<List<Char>>) {
        fun getRightTiltData() = data.map { row ->
            row.toString().split(symFixed).joinToString(symFixed.toString()) { chunk ->
                symEmpty.toString().repeat(chunk.count { it == symEmpty }) +
                        symLoose.toString().repeat(chunk.count { it == symLoose })
            }.toList()
        }

        fun tilt(direction: Day14TiltDirection) {
            repeat(direction.rotations) {
                data = data.rotateLeft()
            }
            data = getRightTiltData()
            repeat((4 - direction.rotations) % 4) {
                data = data.rotateLeft()
            }
        }

        fun tiltFullCycles(count: Int) {
            val previousSituations = mutableListOf<String>()
            var indexOfNextCorrectResult: Int? = null
            repeat(count) {
                if (it == indexOfNextCorrectResult) return

                val currentString = this.toString()
                val indFirstOccurrence = previousSituations.indexOf(currentString)
                if (indFirstOccurrence >= 0) {
                    val repetitionCycleLength = it - indFirstOccurrence
                    indexOfNextCorrectResult =
                        (count - indFirstOccurrence) % repetitionCycleLength + indFirstOccurrence + repetitionCycleLength
                } else {
                    previousSituations.add(currentString)
                }
                repeat(4) {
                    data = data.rotateRight()
                    data = getRightTiltData()
                }
            }
        }

        fun getScore(): Long {
            val rotatedData = data.rotateRight()
            return rotatedData.sumOf { row ->
                row.withIndex().sumOf { if (it.value == symLoose) it.index + 1L else 0L }
            }
        }

        override fun toString(): String {
            return data.joinToString("\n") { it.joinToString("") }
        }
    }


    fun parseInput(input: List<String>): BoulderMap {
        return BoulderMap(input.map { it.toList() })
    }

    fun part1(input: List<String>): Long {
        val boulderMap = parseInput(input)
        boulderMap.tilt(Day14TiltDirection.N)
        return boulderMap.getScore()
    }

    fun part2(input: List<String>): Long {
        val boulderMap = parseInput(input)
        boulderMap.tiltFullCycles(1_000_000_000)
        return boulderMap.getScore()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day14_test")
    check(part1(testInput) == 136L) { "Wrong solution: ${part1(testInput)}" }
    check(part2(testInput) == 64L) { "Wrong solution: ${part2(testInput)}" }

    val input = readInput("Day14")
    part1(input).println()
    part2(input).println()
}
