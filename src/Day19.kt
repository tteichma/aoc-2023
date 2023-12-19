fun main() {
    val partXRegex = Regex("""x=(\d+)""")
    val partMRegex = Regex("""m=(\d+)""")
    val partARegex = Regex("""a=(\d+)""")
    val partSRegex = Regex("""s=(\d+)""")
    val partialRuleRegex = Regex("""(?<char>[xmas])(?<comparator>[<>])(?<number>\d+):(?<nextRule>[A-Za-z]+)""")
    val ruleRegex = Regex("""(?<name>[A-Za-z]+)\{(?<rules>.*)}""")

    data class Part(val x: Int, val m: Int, val a: Int, val s: Int) {
        val score: Long
            get() = (x + m + a + s).toLong()

        fun get(c: Char): Int {
            return when (c) {
                'x' -> x
                'm' -> m
                'a' -> a
                's' -> s
                else -> throw RuntimeException()
            }
        }
    }

    data class Rule(val partialRules: List<(Part) -> String?>)

    data class RuleSet(val rules: Map<String, Rule>) {
        fun isAccepted(part: Part): Boolean {
            var currentRule = rules["in"]!!.partialRules.firstNotNullOf { it(part) }
            while (true) {
                if (currentRule == "A") {
                    return true
                }
                if (currentRule == "R") {
                    return false
                }
                currentRule = rules[currentRule]!!.partialRules.firstNotNullOf { it(part) }
            }
        }
    }

    fun parsePartialRule(input: String): (Part) -> String? {
        val groups = partialRuleRegex.matchEntire(input)!!.groups
        val c = groups["char"]!!.value[0]
        val comp = groups["comparator"]!!.value[0]
        val num = groups["number"]!!.value.toInt()
        val nextRule = groups["nextRule"]!!.value
        return when (comp) {
            '>' -> { it: Part -> if (it.get(c) > num) nextRule else null }
            '<' -> { it: Part -> if (it.get(c) < num) nextRule else null }
            else -> throw RuntimeException()
        }
    }

    fun parseInput(input: List<String>): Pair<RuleSet, List<Part>> {
        val ruleSet = RuleSet(input
            .takeWhile { it != "" }
            .mapNotNull { ruleRegex.matchEntire(it) }.associate { it ->
                val name = it.groups["name"]!!.value
                val partialRuleStrings = it.groups["rules"]!!.value
                    .split(",")
                val rule = Rule(
                    partialRuleStrings.dropLast(1)
                        .map { parsePartialRule(it) } + listOf { partialRuleStrings.last() }
                )
                name to rule
            }
        )

        val parts = input
            .dropWhile { it != "" }
            .drop(1)
            .map { line ->
                Part(
                    x = partXRegex.findAll(line).map { it.groupValues[1].toInt() }.first(),
                    m = partMRegex.findAll(line).map { it.groupValues[1].toInt() }.first(),
                    a = partARegex.findAll(line).map { it.groupValues[1].toInt() }.first(),
                    s = partSRegex.findAll(line).map { it.groupValues[1].toInt() }.first()
                )
            }
        return Pair(ruleSet, parts)
    }

    fun part1(input: List<String>): Long {
        val (ruleSet, parts) = parseInput(input)
        return parts
            .filter { ruleSet.isAccepted(it) }
            .sumOf { it.score }
    }

    fun part2(input: List<String>): Long {
        return part1(input)
    }

// test if implementation meets criteria from the description, like:
    val testInput = readInput("Day19_test")
    check(part1(testInput) == 19114L) { "Wrong solution: ${part1(testInput)}" }
    check(part2(testInput) == 167409079868000L) { "Wrong solution: ${part2(testInput)}" }

    val input = readInput("Day19")
    part1(input).println()
    part2(input).println()
}
