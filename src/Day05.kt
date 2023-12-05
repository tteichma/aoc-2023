fun main() {
    data class StageMapElement(val startOut: Long, val startIn: Long, val length: Long)
    data class Stage(val nameIn: String, val nameOut: String, val mapElements: List<StageMapElement>)
    class Router(val stages: List<Stage>) {
        fun route(origValue: Long): Long {
            var currentValue = origValue
            for (stage in stages) {
                val offset = stage.mapElements
                    .filter { currentValue in it.startIn until it.startIn + it.length }
                    .map { it.startOut - it.startIn }
                    .firstOrNull() ?: 0
                currentValue += offset
            }
            return currentValue
        }
    }

    fun parseBlock(input: List<String>): Stage {
        val namesMatch = Regex("""([a-z]*)-to-([a-z]*) map:""").matchEntire(input.first())!!.groupValues
        val nameIn = namesMatch[1]
        val nameOut = namesMatch[2]

        val stageElements = input.drop(1).map { line ->
            val numbersInLine = numberRegex.findAll(line).map { it.groupValues.first() }.map { it.toLong() }.toList()
            StageMapElement(numbersInLine[0], numbersInLine[1], numbersInLine[2])
        }
        return Stage(nameIn, nameOut, stageElements)
    }

    fun parseInput(input: List<String>): Pair<List<Long>, Router> {
        assert(input.first().startsWith("Seeds: "))
        val seedValues = numberRegex.findAll(input.first().split(":").last())
            .map { it.value.toLong() }
            .toList()
        val indsEmpty = input.withIndex().filter { it.value == "" }.map { it.index }
        val stages = indsEmpty.windowed(2, partialWindows = true) {
            input.subList(it[0] + 1, if (it.count() == 1) input.lastIndex else it[1])
        }.map { parseBlock(it) }

        val sortedStages = stages.filter { it.nameIn == "seed" }.toMutableList()
        var nextInName = sortedStages.first().nameOut
        while (nextInName != "location") {
            val selectedStage = stages.first { it.nameIn == nextInName }
            sortedStages.add(selectedStage)
            nextInName = selectedStage.nameOut
        }
        return Pair(seedValues, Router(sortedStages))
    }


    fun part1(input: List<String>): Long {
        val (seedValues, router) = parseInput(input)
        return seedValues.minOfOrNull { router.route(it) }!!
    }

    fun part2(input: List<String>): Long {
        val (seedRanges, router) = parseInput(input)
        val seedValues = seedRanges
            .asSequence()
            .windowed(2)
            .withIndex()
            .flatMap { it.value[0]..it.value[0] + it.value[1] }
        return seedValues.minOfOrNull { router.route(it) }!!
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day05_test")
    check(part1(testInput) == 35L) { "Wrong solution: ${part1(testInput)}" }
    check(part2(testInput) == 46L) { "Wrong solution: ${part2(testInput)}" }

    val input = readInput("Day05")
    part1(input).println()
    part2(input).println()
}
