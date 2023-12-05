fun main() {
    data class StageMapElement(val startDest: Long, val startSource: Long, val length: Long)

    data class Stage(val nameSource: String, val nameDest: String, val mapElements: List<StageMapElement>) {
        fun getOffsetBySource(source: Long) = mapElements
            .filter { source in it.startSource until it.startSource + it.length }
            .map { it.startDest - it.startSource }
            .firstOrNull() ?: 0

        fun getOffsetByDest(dest: Long) = mapElements
            .filter { dest in it.startDest until it.startDest + it.length }
            .map { it.startDest - it.startSource }
            .firstOrNull() ?: 0
    }

    class Router(val stages: List<Stage>) {
        fun route(origValue: Long): Long {
            var currentValue = origValue
            for (stage in stages) {
                currentValue += stage.getOffsetBySource(currentValue)
            }
            return currentValue
        }

        fun getEdgeCases(): Set<Long> {
            var edgeCases = setOf<Long>()
            for (stage in stages.reversed()) {
                val newEdgeCases = stage.mapElements.flatMap { listOf(it.startSource, it.startSource + it.length - 1) }.toSet()
                edgeCases = newEdgeCases + edgeCases.map { it - stage.getOffsetByDest(it) }
            }
            return edgeCases
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

        val sortedStages = stages.filter { it.nameSource == "seed" }.toMutableList()
        var nextInName = sortedStages.first().nameDest
        while (nextInName != "location") {
            val selectedStage = stages.first { it.nameSource == nextInName }
            sortedStages.add(selectedStage)
            nextInName = selectedStage.nameDest
        }
        return Pair(seedValues, Router(sortedStages))
    }


    fun part1(input: List<String>): Long {
        val (seedValues, router) = parseInput(input)
        return seedValues.minOfOrNull { router.route(it) }!!
    }

    fun part2(input: List<String>): Long {
        val (seedRanges, router) = parseInput(input)
        val seedValueBoundaries = seedRanges
            .windowed(2)
            .withIndex()
            .filter { it.index % 2 == 0 }
            .map { Pair(it.value[0], it.value[0] + it.value[1]) }
        val routerEdgeCases = router.getEdgeCases()
            .filter { source ->
                seedValueBoundaries.any { source in it.first..it.second }
            }
        val seedsToAnalyze = routerEdgeCases + seedValueBoundaries.flatMap { listOf(it.first, it.second) }
        return seedsToAnalyze.minOfOrNull { router.route(it) }!!
    }

// test if implementation meets criteria from the description, like:
    val testInput = readInput("Day05_test")
    check(part1(testInput) == 35L) { "Wrong solution: ${part1(testInput)}" }
    check(part2(testInput) == 46L) { "Wrong solution: ${part2(testInput)}" }

    val input = readInput("Day05")
    part1(input).println()
    part2(input).println()
}
