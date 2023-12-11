import kotlin.io.path.Path
import kotlin.io.path.readLines

val unsignedIntegerRegex = Regex("""\d+""")
val signedIntegerRegex = Regex("""-?\d+""")

typealias IntCoordinate = Pair<Int, Int>

fun <T> MutableSet<T>.pop(): T? = this.first().also{this.remove(it)}

fun <T> Sequence<T>.repeatInfinitely() = sequence { while (true) yieldAll(this@repeatInfinitely) }

fun Long.pow(exp: Int): Long {
    if (exp == 0) return 1L
    var out = this
    repeat(exp - 1) {
        out *= this
    }
    return out
}

fun getPrimeFactors(number: Long): List<Long> {
    var n = number
    val primes = mutableListOf<Long>()
    for (i in 2L..n / 2) {
        while (n % i == 0L) {
            primes.add(i)
            n /= i
        }
    }
    if (n != 1L) {
        primes.add(n)
    }
    return primes.toList()
}

fun getLcm(a: Long, b: Long): Long {
    val primesA = getPrimeFactors(a).groupBy { it }.map { it.key to it.value.size }
    val primesB = getPrimeFactors(b).groupBy { it }.map { it.key to it.value.size }
    return (primesA + primesB)
        .groupBy { it.first }
        .map { it.key to it.value.maxOf { v -> v.second } }
        .map { it.first.pow(it.second) }
        .reduce { acc, it -> acc * it }
}

/**
 * Reads lines from the given input txt file.
 */
fun readInput(name: String) = Path("src/$name.txt").readLines()

/**
 * The cleaner shorthand for printing output.
 */
fun Any?.println() = println(this)
