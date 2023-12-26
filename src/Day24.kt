import kotlin.math.abs

fun main() {
    data class HailTrajectory2d(val px: Double, val py: Double, val vx: Double, val vy: Double) {
        val m = vy / vx

        fun getY(x: Double) = py + (x - px) * m

        fun getOverLappingRange(
            other: HailTrajectory2d,
            originalRange: ClosedRange<Double>
        ): ClosedFloatingPointRange<Double> {
            val newStart = listOfNotNull(
                originalRange.start, if (vx > 0) px else null, if (other.vx > 0) other.px else null
            ).max()
            val newEnd = listOfNotNull(
                originalRange.endInclusive, if (vx < 0) px else null, if (other.vx < 0) other.px else null
            ).min()

            return newStart..newEnd
        }

        fun getIntersectingX(other: HailTrajectory2d) = (other.py - py + px * m - other.px * other.m) / (m - other.m)
        fun isIntersectingInRange(
            other: HailTrajectory2d, totalRange: ClosedRange<Double>, onlyFuture: Boolean
        ): Boolean {
            val range = if (onlyFuture) getOverLappingRange(other, totalRange) else totalRange
            val x = getIntersectingX(other)
            return (x in range) and (getY(x) in totalRange)
        }
    }

    fun parseInput(input: List<String>): List<HailTrajectory2d> {
        return input.map {
            val numbers = getSignedDoublesFromString(it).toList()
            HailTrajectory2d(numbers[0], numbers[1], numbers[3], numbers[4])
        }
    }

    fun part1(
        input: List<String>, range: ClosedRange<Double>, onlyFuture: Boolean
    ): Long {
        val trajectories = parseInput(input)

        val numIntersections = trajectories.withIndex().sumOf { t1 ->
            (t1.index + 1..trajectories.lastIndex).count {
                val t2 = trajectories[it]
                t1.value.isIntersectingInRange(t2, range, onlyFuture)
            }
        }
        return numIntersections.toLong()
    }

//    fun part2(input: List<String>): Long {
//        return 0L
//    }

// test if implementation meets criteria from the description, like:
    val testRange = 7.toDouble()..27.toDouble()
    val testInput = readInput("Day24_test")
    check(part1(testInput, testRange, true) == 2L) { "Wrong solution: ${part1(testInput, testRange, true)}" }
//    check(part2(testInput) == 2L) { "Wrong solution: ${part1(testInput)}" }

    val input = readInput("Day24")
    part1(
        input, 200_000_000_000_000.0..400_000_000_000_000.0, true
    ).println()
    //15736
//    16146
//    part2(input).println()
}
