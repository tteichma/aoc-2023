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

    data class PartRange(val x: IntRange, val m: IntRange, val a: IntRange, val s: IntRange) {
        fun get(c: Char): IntRange {
            return when (c) {
                'x' -> x
                'm' -> m
                'a' -> a
                's' -> s
                else -> throw RuntimeException()
            }
        }

        fun countElements() = listOf(x, m, a, s).fold(1L) { acc, it -> acc * (it.last - it.first + 1L) }

        fun copy(c: Char, v: IntRange): PartRange {
            return when (c) {
                'x' -> PartRange(x = v, m = m, a = a, s = s)
                'm' -> PartRange(x = x, m = v, a = a, s = s)
                'a' -> PartRange(x = x, m = m, a = v, s = s)
                's' -> PartRange(x = x, m = m, a = a, s = v)
                else -> throw RuntimeException()
            }
        }

        fun isEmpty() = x.isEmpty() || m.isEmpty() || a.isEmpty() || s.isEmpty()
    }

    @Suppress("EmptyRange")
    val emptyPartRange = PartRange(0..<0, 0..<0, 0..<0, 0..<0)

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

    data class PartialRangeRuleResult(val nextRange: PartRange, val nullRange: PartRange, val nextRule: String)

    data class RangeRule(val partialRules: List<(PartRange) -> PartialRangeRuleResult>)

    data class RangeRuleSet(val rules: Map<String, RangeRule>) {
        fun getNumAccepted(initialPartRange: PartRange): Long {
            val rangeRulePairs: MutableSet<Pair<PartRange, String?>> = mutableSetOf(Pair(initialPartRange, "in"))
            val acceptedPartRanges = mutableSetOf<PartRange>()

            while (rangeRulePairs.isNotEmpty()) {
                val (currentPartRange, ruleName) = rangeRulePairs.pop()!!

                if (ruleName == "A") {
                    acceptedPartRanges.add(currentPartRange)
                    continue
                } else if (ruleName == "R") {
                    continue
                }

                val partialRules = rules[ruleName]!!.partialRules

                var result = PartialRangeRuleResult(emptyPartRange, currentPartRange, "")
                for (partialRule in partialRules) {
                    result = partialRule(result.nullRange)
                    if (!result.nextRange.isEmpty()) rangeRulePairs.add(Pair(result.nextRange, result.nextRule))
                }
            }
            return acceptedPartRanges.sumOf { it.countElements() }
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

    fun parsePartialRangeRule(input: String): (PartRange) -> PartialRangeRuleResult {
        val groups = partialRuleRegex.matchEntire(input)!!.groups
        val c = groups["char"]!!.value[0]
        val comp = groups["comparator"]!!.value[0]
        val num = groups["number"]!!.value.toInt()
        val nextRule = groups["nextRule"]!!.value
        return when (comp) {
            '>' -> { it: PartRange ->
                val origRange = it.get(c)
                PartialRangeRuleResult(
                    it.copy(c, num + 1..origRange.last),
                    it.copy(c, origRange.first..num),
                    nextRule
                )
            }

            '<' -> { it: PartRange ->
                val origRange = it.get(c)
                PartialRangeRuleResult(
                    it.copy(c, origRange.first..<num),
                    it.copy(c, num..origRange.last),
                    nextRule
                )
            }

            else -> throw RuntimeException()
        }
    }

    fun parseRuleSetStrings(input: List<String>) = RuleSet(input
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

    fun parseRangeRuleSetStrings(input: List<String>) = RangeRuleSet(input
        .mapNotNull { ruleRegex.matchEntire(it) }.associate { it ->
            val name = it.groups["name"]!!.value
            val partialRuleStrings = it.groups["rules"]!!.value
                .split(",")
            val rule = RangeRule(
                partialRuleStrings.dropLast(1)
                    .map { parsePartialRangeRule(it) } + listOf {
                    @Suppress("EmptyRange")
                    PartialRangeRuleResult(it, PartRange(0..<0, 0..<0, 0..<0, 0..<0), partialRuleStrings.last())
                }
            )
            name to rule
        }
    )

    fun parseParts(input: List<String>) = input
        .map { line ->
            Part(
                x = partXRegex.findAll(line).map { it.groupValues[1].toInt() }.first(),
                m = partMRegex.findAll(line).map { it.groupValues[1].toInt() }.first(),
                a = partARegex.findAll(line).map { it.groupValues[1].toInt() }.first(),
                s = partSRegex.findAll(line).map { it.groupValues[1].toInt() }.first()
            )
        }


    fun part1(input: List<String>): Long {
        val ruleSet = parseRuleSetStrings(input.takeWhile { it != "" })
        val parts = parseParts(input.dropWhile { it != "" }.drop(1))
        return parts
            .filter { ruleSet.isAccepted(it) }
            .sumOf { it.score }
    }

    fun part2(input: List<String>): Long {
        val ruleSet = parseRangeRuleSetStrings(input.takeWhile { it != "" })
        return ruleSet.getNumAccepted(PartRange(1..4000, 1..4000, 1..4000, 1..4000))
    }

// test if implementation meets criteria from the description, like:
    val testInput = readInput("Day19_test")
    check(part1(testInput) == 19114L)
    { "Wrong solution: ${part1(testInput)}" }
    check(part2(testInput) == 167409079868000L)
    { "Wrong solution: ${part2(testInput)}" }

    val input = readInput("Day19")
    part1(input).println()
    part2(input).println()
}