@OptIn(ExperimentalStdlibApi::class)
fun main() {

    val symOrig = '.'
    val symTrench = '#'

    data class Instruction(val direction: Direction, val length: Int)

    class DiggingMap(data: List<List<Char>>) : DataMap<Char>(data) {
        fun countDigged() = data.sumOf { row ->
            row.count { it == symTrench }.toLong()
        }


        fun getFilledTrenchesMap(): DiggingMap {
            val coordinatesOutside = mutableSetOf<IntCoordinate>()
            val coordinatesToCheck = (
                    rowIndices.flatMap { listOf(Pair(it, 0), Pair(it, lastColIndex)) }
                            + colIndices.flatMap { listOf(Pair(0, it), Pair(lastRowIndex, it)) })
                .toMutableSet()

            while (coordinatesToCheck.isNotEmpty()) {
                val coordinate = coordinatesToCheck.pop()!!
                if (data[coordinate] == symOrig) {
                    coordinatesOutside.add(coordinate)
                    Direction.entries
                        .map { it.nextCoordinate(coordinate) }
                        .filter { it !in coordinatesOutside }
                        .filter { it.isWithinBoundaries() }
                        .forEach { coordinatesToCheck.add(it) }
                }
            }

            val filledData = data.mapIndexed { indRow, row ->
                List(row.size) { indCol -> if (Pair(indRow, indCol) in coordinatesOutside) symOrig else symTrench }
            }
            return DiggingMap(filledData)
        }

    }

    fun parseInput1(input: List<String>): List<Instruction> = input.map { line ->
        val splitLine = line.split(" ")
        val direction = when (splitLine[0]) {
            "U" -> Direction.DU
            "D" -> Direction.UD
            "L" -> Direction.RL
            "R" -> Direction.LR
            else -> throw RuntimeException()
        }
        Instruction(direction, splitLine[1].toInt())
    }

    fun parseInput2(input: List<String>): List<Instruction> = input.map { line ->
        val hexString = line.split(" ").last().drop(2).dropLast(1)
        val direction = when (hexString.last()) {
            '0' -> Direction.LR
            '1' -> Direction.UD
            '2' -> Direction.RL
            '3' -> Direction.DU
            else -> throw RuntimeException()
        }
        val distance = hexString.dropLast(1).hexToInt()
        Instruction(direction, distance)
    }

    fun parseInstructions(instructions: List<Instruction>): DiggingMap {
        val foldedRows = instructions
            .map {
                it.length * when (it.direction) {
                    Direction.UD -> +1
                    Direction.DU -> -1
                    else -> 0
                }
            }
            .runningFold(0) { r, it -> r + it }
        val foldedCols = instructions
            .map {
                it.length * when (it.direction) {
                    Direction.LR -> +1
                    Direction.RL -> -1
                    else -> 0
                }
            }
            .runningFold(0) { r, it -> r + it }

        val cells =
            MutableList(foldedRows.max() - foldedRows.min() + 1) { MutableList(foldedCols.max() - foldedCols.min() + 1) { symOrig } }

        var coordinate = Pair(-foldedRows.min(), -foldedCols.min())

        for (instruction in instructions) {
            repeat(instruction.length) {
                cells[coordinate.first][coordinate.second] = symTrench
                coordinate = instruction.direction.nextCoordinate(coordinate)
            }
        }

        return DiggingMap(cells)
    }

    fun part1(input: List<String>): Long {
        val diggingMap = parseInstructions(parseInput1(input))
        val filledTrenchesMap = diggingMap.getFilledTrenchesMap()
        return filledTrenchesMap.countDigged()
    }

    fun part2(input: List<String>): Long {
        val diggingMap = parseInstructions(parseInput2(input))
        val filledTrenchesMap = diggingMap.getFilledTrenchesMap()
        return filledTrenchesMap.countDigged()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day18_test")
    check(part1(testInput) == 62L) { "Wrong solution: ${part1(testInput)}" }
    check(part2(testInput) == 952408144115L) { "Wrong solution: ${part2(testInput)}" }

    val input = readInput("Day18")
    part1(input).println()
    part2(input).println()
}
