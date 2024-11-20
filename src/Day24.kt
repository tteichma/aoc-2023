import kotlin.math.abs
import kotlin.math.sqrt

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

    fun getDivisors(positiveNumber: Long): Sequence<Long> = sequence {
        yield(1)
        var n = positiveNumber
        val seenDivisors = mutableSetOf<Long>(1)
        for (i in 2..sqrt(n.toDouble()).toLong() + 1) {
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

    fun getDivisorsWithAnySign(number: Long): Sequence<Long> = getDivisors(abs(number)).flatMap { sequenceOf(it, -it) }

    fun getVCandidatesFromPvPairsWithSameSpeed(pVPairsWithSameSpeed: List<Pair<Long, Long>>): Set<Long> {
        val particleV = pVPairsWithSameSpeed.first().second
        val deltaPCandidates = pVPairsWithSameSpeed
            .map { pVRef ->
                pVPairsWithSameSpeed
                    .filter { it.first != pVRef.first }
                    .map { pVRef.first - it.first }
            }
        val vCandidates = deltaPCandidates.flatMap { deltaPsWithSameRef ->
            deltaPsWithSameRef
                .map {
                    getDivisorsWithAnySign(it)
                        .map { v -> v + particleV }
                        .toSet()
                }
        }
            .reduce { acc, candidateSet -> acc.intersect(candidateSet) }
        return vCandidates
    }

    fun getPosFromComponentPairs(
        pVPairs: List<Pair<Long, Long>>,
        initialTimes: List<Long?> = List(pVPairs.size) { null }
    ): Pair<Long, List<Long?>> {
        // Given a pair of particles r and i, we have the following equation:
        //     pr + vr * tr + v * (ti - tr) == pi + vi * ti
        //         where tx is time of collision with particle x, v is the throw speed we solve for.
        // Grouping particles such that vr == vi, we get:
        //    pr - pi == (v - vi) * (tr - ti)
        // As all numbers are integers (v - vi) must be a divisor of (pr - pi)

        val pVPairGroupsWithSameSpeed = pVPairs
            .groupBy { it.second }
            .values
            .map { it.toSet().toList() }
            .filter { it.size > 1 }
            .sortedBy { -it.size }

        val vCandidates = pVPairGroupsWithSameSpeed
            .map { getVCandidatesFromPvPairsWithSameSpeed(it) }
            .reduce { acc, candidateSet -> acc.intersect(candidateSet) }

        if (initialTimes.any { it != null }) {
            for (vCandidate in vCandidates) {
                val (pV, t) = pVPairs.zip(initialTimes).first { it.second != null }

                // Checking the many times from the previous time-step seems good enough.
                val pCandidate = pV.first + (pV.second - vCandidate) * t!!
                if (pVPairs.zip(initialTimes).any { (pV, t) ->
                        (t != null) && (pCandidate + vCandidate * t != pV.first + pV.second * t)
                    }) {
                    continue
                }

                return Pair(pCandidate, initialTimes)
            }
        }

        val pVCandidates = sequence {
            val vSequenceDeque = ArrayDeque<Pair<Iterator<Long>, Long>>(
                vCandidates.map { v ->
                    val pMin = pVPairs.filter { it.second >= v }.maxOfOrNull { it.first + (it.second - v) } ?: 1
                    val pMax = pVPairs.filter { it.second <= v }.minOfOrNull { it.first + (it.second - v) }
                        ?: Long.MAX_VALUE

                    Pair((pMin..pMax).iterator(), v)
                }
            )

            while (vSequenceDeque.isNotEmpty()) {
                val vAndNextPVSequence = vSequenceDeque.removeFirst()
                if (!vAndNextPVSequence.first.hasNext()) {
                    continue
                }

                yield(Pair(vAndNextPVSequence.first.next(), vAndNextPVSequence.second))
                vSequenceDeque.add(vAndNextPVSequence)
            }
        }

        for ((pCandidate, vCandidate) in pVCandidates) {
            val tCandidates = pVPairs.map {
                if (vCandidate != it.second) (it.first - pCandidate) / (vCandidate - it.second) else null
            }

            val initialAndCandidateTimes = initialTimes.zip(tCandidates)

            if (pVPairs.zip(tCandidates).any { (pV, t) ->
                    (t != null) && (pCandidate + vCandidate * t != pV.first + pV.second * t)
                }) {
                continue
            }

            if (initialAndCandidateTimes.any { it.first != null && it.second != null && it.first != it.second }) {
                continue
            }

            return Pair(pCandidate, initialAndCandidateTimes.map { it.first ?: it.second })
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

        println("Evaluating y...")
        val (py, yTimes) = getPosFromComponentPairs(
            trajectories.map { Pair(it.py, it.vy) }
        )
        println("Evaluating x...")
        val (px, xTimes) = getPosFromComponentPairs(trajectories.map { Pair(it.px, it.vx) }, yTimes)
        println("Evaluating z...")
        val (pz, _) = getPosFromComponentPairs(trajectories.map { Pair(it.pz, it.vz) }, xTimes)
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
