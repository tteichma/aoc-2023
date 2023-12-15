fun main() {
    data class Lense(val label: String, val lens: Int?)

    fun String.day15Hash() = this.map { it.code }.fold(0) { acc, it -> ((acc + it) * 17) % 256 }

    class LenseBoxes {
        private val boxes = List(256) { mutableListOf<Lense>() }

        override fun toString() =
            boxes.withIndex()
                .filter { it.value.isNotEmpty() }
                .joinToString("\n") { "Box % 3d: %s".format(it.index, it.value) }

        fun removeLense(lense: Lense) {
            val box = boxes[lense.label.day15Hash()]
            val ind = box.indexOfFirst { it.label == lense.label }
            if (ind >= 0) {
                box.removeAt(ind)
            }
        }

        fun addLense(lense: Lense) {
            val box = boxes[lense.label.day15Hash()]
            val ind = box.indexOfFirst { it.label == lense.label }
            if (ind >= 0) {
                box.removeAt(ind)
                box.add(ind, lense)
            } else {
                box.add(lense)
            }
        }

        fun handleInstruction(instruction: String) {
            if (instruction.last() == '-') {
                removeLense(Lense(instruction.substring(0..<instruction.lastIndex), null))
            } else {
                addLense(
                    Lense(instruction.substring(0..<instruction.lastIndex - 1), instruction.last().digitToIntOrNull())
                )
            }
        }

        fun getPower() = boxes.withIndex().sumOf { box ->
            (box.index + 1L) * box.value.withIndex().sumOf { (it.index + 1) * it.value.lens!! }
        }
    }

    fun parseInput(input: List<String>): List<String> {
        return input.first().split(",")
    }


    fun part1(input: List<String>): Long {
        return parseInput(input).sumOf { it.day15Hash().toLong() }
    }

    fun part2(input: List<String>): Long {
        val lenseBoxes = LenseBoxes()
        for (word in parseInput(input)) {
            lenseBoxes.handleInstruction(word)
        }
        return lenseBoxes.getPower()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day15_test")
    check(part1(testInput) == 1320L) { "Wrong solution: ${part1(testInput)}" }
    check(part2(testInput) == 145L) { "Wrong solution: ${part2(testInput)}" }

    val input = readInput("Day15")
    part1(input).println()
    part2(input).println()
}
