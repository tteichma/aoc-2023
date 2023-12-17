sealed class Day16Direction(val nextCoordinate: (IntCoordinate) -> IntCoordinate) {
    data object LR : Day16Direction({ Pair(it.first, it.second + 1) })
    data object RL : Day16Direction({ Pair(it.first, it.second - 1) })
    data object UD : Day16Direction({ Pair(it.first + 1, it.second) })
    data object DU : Day16Direction({ Pair(it.first - 1, it.second) })
}


sealed class Day16Cell {
    abstract fun getNextDirections(inDir: Day16Direction): List<Day16Direction>
    fun handleBeam(beamElement: Day16BeamElement) = getNextDirections(beamElement.direction).map {
        Day16BeamElement(it.nextCoordinate(beamElement.coordinate), it)
    }

    data object SplitterVertical : Day16Cell() {
        override fun getNextDirections(inDir: Day16Direction) = when (inDir) {
            Day16Direction.LR, Day16Direction.RL -> listOf(Day16Direction.UD, Day16Direction.DU)
            else -> listOf(inDir)
        }
    }

    data object SplitterHorizontal : Day16Cell() {
        override fun getNextDirections(inDir: Day16Direction) = when (inDir) {
            Day16Direction.UD, Day16Direction.DU -> listOf(Day16Direction.LR, Day16Direction.RL)
            else -> listOf(inDir)
        }
    }

    data object MirrorBackward : Day16Cell() {
        override fun getNextDirections(inDir: Day16Direction) = when (inDir) {
            Day16Direction.UD -> listOf(Day16Direction.LR)
            Day16Direction.DU -> listOf(Day16Direction.RL)
            Day16Direction.LR -> listOf(Day16Direction.UD)
            Day16Direction.RL -> listOf(Day16Direction.DU)
        }
    }

    data object MirrorForward : Day16Cell() {
        override fun getNextDirections(inDir: Day16Direction) = when (inDir) {
            Day16Direction.UD -> listOf(Day16Direction.RL)
            Day16Direction.DU -> listOf(Day16Direction.LR)
            Day16Direction.LR -> listOf(Day16Direction.DU)
            Day16Direction.RL -> listOf(Day16Direction.UD)
        }
    }

    data object Empty : Day16Cell() {
        override fun getNextDirections(inDir: Day16Direction) = listOf(inDir)
    }

    companion object {
        fun fromChar(c: Char) = when (c) {
            '.' -> Empty
            '|' -> SplitterVertical
            '-' -> SplitterHorizontal
            '\\' -> MirrorBackward
            '/' -> MirrorForward
            else -> throw RuntimeException("Invalid character.")
        }

    }
}

data class Day16BeamElement(val coordinate: IntCoordinate, val direction: Day16Direction)

class Day16MirrorMap(val data: List<List<Day16Cell>>) {
    private fun Day16BeamElement.isWithinBoundaries() =
        (this.coordinate.first in 0..data.lastIndex && this.coordinate.second in 0..data.first().lastIndex)

    private fun Day16BeamElement.getNextBeamElements() =
        data[this.coordinate].handleBeam(this).filter { it.isWithinBoundaries() }


    private fun getVisitedBeamElements(initialBeamElement: Day16BeamElement): MutableSet<Day16BeamElement> {
        val visitedBeamElements = mutableSetOf(initialBeamElement)
        val beamElementsToProcess = mutableSetOf(initialBeamElement)

        while (beamElementsToProcess.isNotEmpty()) {
            val beamElement = beamElementsToProcess.pop()!!
            for (nextBeamElement in beamElement.getNextBeamElements()) {
                if (nextBeamElement !in visitedBeamElements) {
                    visitedBeamElements.add(nextBeamElement)
                    beamElementsToProcess.add(nextBeamElement)
                }
            }
        }
        return visitedBeamElements
    }

    fun getNumberOfEnergizedCells(initialBeamElement: Day16BeamElement) = getVisitedBeamElements(initialBeamElement)
        .map { it.coordinate }
        .toSet()
        .size
}

fun main() {
    fun parseInput(input: List<String>): Day16MirrorMap {
        return Day16MirrorMap(input.map { row -> row.map { Day16Cell.fromChar(it) } })
    }

    fun part1(input: List<String>): Int {
        val mirrorMap = parseInput(input)
        return mirrorMap.getNumberOfEnergizedCells(Day16BeamElement(Pair(0, 0), Day16Direction.LR))
    }

    fun part2(input: List<String>): Int {
        val mirrorMap = parseInput(input)
        val lastRowIndex = mirrorMap.data.lastIndex
        val lastColumnIndex = mirrorMap.data.first().lastIndex
        val entryElements = (
                (0..lastRowIndex).map { Day16BeamElement(Pair(it, 0), Day16Direction.LR) }
                        + (0..lastRowIndex).map { Day16BeamElement(Pair(it, lastColumnIndex), Day16Direction.RL) }
                        + (0..lastColumnIndex).map { Day16BeamElement(Pair(0, it), Day16Direction.UD) }
                        + (0..lastColumnIndex).map { Day16BeamElement(Pair(lastColumnIndex, it), Day16Direction.DU) }
                )
        return entryElements.maxOf { mirrorMap.getNumberOfEnergizedCells(it) }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day16_test")
    check(part1(testInput) == 46) { "Wrong solution: ${part1(testInput)}" }
    check(part2(testInput) == 51) { "Wrong solution: ${part2(testInput)}" }

    val input = readInput("Day16")
    part1(input).println()
    part2(input).println()
}
