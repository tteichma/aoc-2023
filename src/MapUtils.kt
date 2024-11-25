sealed class Direction(val nextCoordinate: (IntCoordinate) -> IntCoordinate) {
    data object LR : Direction({ Pair(it.first, it.second + 1) })
    data object RL : Direction({ Pair(it.first, it.second - 1) })
    data object UD : Direction({ Pair(it.first + 1, it.second) })
    data object DU : Direction({ Pair(it.first - 1, it.second) })

    val opposite
        get() = oppositeDirs[this]!!
    val perpendicular
        get() = perpendicularDirsLists[this] ?: listOf()

    companion object {
        val entries
            get() = allEntries

        private val oppositeDirs by lazy {
            mapOf(
                LR to RL,
                RL to LR,
                UD to DU,
                DU to UD
            )
        }
        private val perpendicularDirsLists by lazy {
            mapOf(
                LR to listOf(UD, DU),
                RL to listOf(UD, DU),
                UD to listOf(LR, RL),
                DU to listOf(LR, RL)
            )
        }
        private val allEntries by lazy { listOf(LR, RL, UD, DU) }
    }
}

operator fun IntCoordinate.plus(direction: Direction) = when (direction) {
    Direction.DU -> Pair(this.first - 1, this.second)
    Direction.UD -> Pair(this.first + 1, this.second)
    Direction.RL -> Pair(this.first, this.second - 1)
    Direction.LR -> Pair(this.first, this.second + 1)
}

open class DataMap<T>(val data: List<List<T>>) {
    init {
        check(data.map { it.size }.toSet().size == 1)
    }

    val lastRowIndex = data.lastIndex
    val lastColIndex = data.first().lastIndex
    val rowSize = data.size
    val colSize = data.first().size

    val rowIndices = data.indices
    val colIndices = data.first().indices

    protected fun IntCoordinate.isWithinBoundaries() =
        (this.first in rowIndices && this.second in colIndices)

    override fun toString() = data.joinToString("\n") { row -> row.joinToString("") }
}