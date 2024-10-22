import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.io.path.Path
import kotlin.io.path.readLines

val unsignedIntegerRegex = Regex("""\d+""")
val signedIntegerRegex = Regex("""-?\d+""")
fun getLongsFromString(s: String) = signedIntegerRegex.findAll(s).map { it.groupValues[0].toLong() }
fun getUnsignedIntsFromString(s: String) = unsignedIntegerRegex.findAll(s).map { it.groupValues[0].toInt() }
fun getSignedDoublesFromString(s: String) = signedIntegerRegex.findAll(s).map { it.groupValues[0].toDouble() }

typealias IntCoordinate = Pair<Int, Int>
operator fun <T> List<List<T>>.get(coordinate: IntCoordinate) = this[coordinate.first][coordinate.second]

fun <T> MutableSet<T>.pop(): T? = this.first().also { this.remove(it) }

suspend fun <A, B> Iterable<A>.pmap(f: suspend (A) -> B): List<B> = coroutineScope {
    map { async { f(it) } }.awaitAll()
}

fun <T> Sequence<T>.repeatInfinitely() = sequence { while (true) yieldAll(this@repeatInfinitely) }

fun <T> List<List<T>>.rotateRight(): List<List<T>> {
    val lastOldColIndex = this.first().lastIndex
    val lastOldRowIndex = this.lastIndex
    return (0..lastOldColIndex).map { iNewRow ->
        (0..lastOldRowIndex).map { this[lastOldRowIndex - it][iNewRow] }
    }
}

fun <T> List<List<T>>.rotateLeft(): List<List<T>> {
    val lastOldColIndex = this.first().lastIndex
    val lastOldRowIndex = this.lastIndex
    return (lastOldColIndex downTo 0).map { iNewRow ->
        (0..lastOldRowIndex).map { this[it][iNewRow] }
    }
}

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
fun List<String>.toCharLists() = this.map { it.toList() }

fun List<String>.println() = println(this.joinToString("\n"))

/**
 * The cleaner shorthand for printing output.
 */
fun Any?.println() = println(this)
