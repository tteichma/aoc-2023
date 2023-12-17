sealed class Day16Cell {
    abstract fun getNextDirections(inDir: Direction): List<Direction>
    fun handleBeam(beamElement: Day16BeamElement) = getNextDirections(beamElement.direction).map {
        Day16BeamElement(it.nextCoordinate(beamElement.coordinate), it)
    }

    data object SplitterVertical : Day16Cell() {
        override fun getNextDirections(inDir: Direction) = when (inDir) {
            Direction.LR, Direction.RL -> listOf(Direction.UD, Direction.DU)
            else -> listOf(inDir)
        }
    }

    data object SplitterHorizontal : Day16Cell() {
        override fun getNextDirections(inDir: Direction) = when (inDir) {
            Direction.UD, Direction.DU -> listOf(Direction.LR, Direction.RL)
            else -> listOf(inDir)
        }
    }

    data object MirrorBackward : Day16Cell() {
        override fun getNextDirections(inDir: Direction) = when (inDir) {
            Direction.UD -> listOf(Direction.LR)
            Direction.DU -> listOf(Direction.RL)
            Direction.LR -> listOf(Direction.UD)
            Direction.RL -> listOf(Direction.DU)
        }
    }

    data object MirrorForward : Day16Cell() {
        override fun getNextDirections(inDir: Direction) = when (inDir) {
            Direction.UD -> listOf(Direction.RL)
            Direction.DU -> listOf(Direction.LR)
            Direction.LR -> listOf(Direction.DU)
            Direction.RL -> listOf(Direction.UD)
        }
    }

    data object Empty : Day16Cell() {
        override fun getNextDirections(inDir: Direction) = listOf(inDir)
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

data class Day16BeamElement(val coordinate: IntCoordinate, val direction: Direction)

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
        return mirrorMap.getNumberOfEnergizedCells(Day16BeamElement(Pair(0, 0), Direction.LR))
    }

    fun part2(input: List<String>): Int {
        val mirrorMap = parseInput(input)
        val lastRowIndex = mirrorMap.data.lastIndex
        val lastColumnIndex = mirrorMap.data.first().lastIndex
        val entryElements = (
                (0..lastRowIndex).map { Day16BeamElement(Pair(it, 0), Direction.LR) }
                        + (0..lastRowIndex).map { Day16BeamElement(Pair(it, lastColumnIndex), Direction.RL) }
                        + (0..lastColumnIndex).map { Day16BeamElement(Pair(0, it), Direction.UD) }
                        + (0..lastColumnIndex).map { Day16BeamElement(Pair(lastColumnIndex, it), Direction.DU) }
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
