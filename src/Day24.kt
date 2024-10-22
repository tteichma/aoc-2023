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

    fun parseInputPart1(input: List<String>): List<HailTrajectory2d> {
        return input.map {
            val numbers = getSignedDoublesFromString(it).toList()
            HailTrajectory2d(numbers[0], numbers[1], numbers[3], numbers[4])
        }
    }

    fun part1(
        input: List<String>, range: ClosedRange<Double>, onlyFuture: Boolean
    ): Long {
        val trajectories = parseInputPart1(input)

        val numIntersections = trajectories.withIndex().sumOf { t1 ->
            (t1.index + 1..trajectories.lastIndex).count {
                val t2 = trajectories[it]
                t1.value.isIntersectingInRange(t2, range, onlyFuture)
            }
        }
        return numIntersections.toLong()
    }

    data class HailTrajectory3d(
        val px: Long,
        val py: Long,
        val pz: Long,
        val vx: Long,
        val vy: Long,
        val vz: Long
    )

    fun parseInputPart2(input: List<String>): List<HailTrajectory3d> {
        return input.map {
            val numbers = getLongsFromString(it).toList()
            HailTrajectory3d(numbers[0], numbers[1], numbers[2], numbers[3], numbers[4], numbers[5])
        }
    }

    fun getDivisorsWithSameSign(number: Long): Sequence<Long> = sequence {
        if (number < 0) {
            yieldAll(getDivisorsWithSameSign(-number).map { -it })
        }

        yield(1)
        var n = number
        val seenDivisors = mutableSetOf<Long>(1)
        for (i in 2..n / 2) {
            while (n % i == 0L) {
                val unseenDivisors = seenDivisors.map { it * i }.filterNot { it in seenDivisors }
                yieldAll(unseenDivisors)
                seenDivisors.addAll(unseenDivisors)
                n /= i
            }
        }
        if (n != 1L) {
            yield(n)
        }
    }

    fun getPosFromComponentPair(
        pVPairs: List<Pair<Long, Long>>,
        initialTimes: List<Long?> = List(pVPairs.size) {null}
    ): Pair<Long, List<Long?>> {
        val firstPVIndex = initialTimes.indexOf(1)
        val firstPVPairCandidates = if (firstPVIndex != -1) listOf(pVPairs[firstPVIndex]) else pVPairs
        for (firstPVCandidate in firstPVPairCandidates) {
            val firstPVCandidateAtT1 = Pair(firstPVCandidate.first + firstPVCandidate.second, firstPVCandidate.second)
            val (deltaP, v2) = pVPairs
                .map { Pair(it.first + it.second, it.second) }  // at t=1
                .map { Pair(it.first - firstPVCandidateAtT1.first, it.second) }
                .filter { it.first != 0L }
                .minBy { abs(it.first) }
            for (divisor in getDivisorsWithSameSign(deltaP)) {
                val vCandidate = divisor + v2
                val pCandidate = firstPVCandidateAtT1.first - vCandidate
                val candidateTimes= pVPairs.map { if (vCandidate != it.second) (it.first - pCandidate) / (vCandidate - it.second) else null}
                val times = initialTimes.zip(candidateTimes).map {
                    when {
                        it.first == it.second -> it.first
                        it.second == null -> it.first
                        it.first == null -> it.second
                        else -> -999  // will fail next condition

                    }
                }
                if (times.all { it== null || it > 0 } && pVPairs.zip(times).all  {(pV, t )-> t == null || (pV.first + pV.second * t == pCandidate + vCandidate * t) }) {
                    return Pair(pCandidate, times)
                }
            }
        }

        throw Error("Seems I missed some edge case.")
    }

    fun part2(input: List<String>): Long {
        // Assuming the stone starts at px1 at time 0, we can calculate the delta_ti until it hits particle i:
        //     pxi + vxi * delta_ti = px1 + vx * delta_ti
        //     delta_ti      = (pxi - px1) / (vx - vxi),
        // Since delta_ti must be integer, (vx - vxi) must be a divisor of (pxi - px1), so
        //     vx =  divisor(pxi - px1) + vxi, where divisor(n) has the same sign as n.

        val trajectories = parseInputPart2(input)

        val (px, xTimes) = getPosFromComponentPair(trajectories.map { Pair(it.px, it.vx) })
        val (py, yTimes) = getPosFromComponentPair(trajectories.map { Pair(it.py, it.vy) }, xTimes)
        val (pz, _) = getPosFromComponentPair(trajectories.map { Pair(it.pz, it.vz) }, yTimes)
        return (px + py + pz)
    }

// test if implementation meets criteria from the description, like:
    val testRange = 7.toDouble()..27.toDouble()
    val testInput = readInput("Day24_test")
    check(part1(testInput, testRange, true) == 2L) { "Wrong solution: ${part1(testInput, testRange, true)}" }

    val input = readInput("Day24")
    part1(
        input, 200_000_000_000_000.0..400_000_000_000_000.0, true
    ).println()

    check(part2(testInput) == 47L) { "Wrong solution: ${part2(testInput)}" }
    part2(input).println()
}
