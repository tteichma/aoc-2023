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
    }
}


open class DataMap<T>(val data: List<List<T>>) {
    val lastRowIndex = data.lastIndex
    val lastColIndex = data.first().lastIndex

    protected fun IntCoordinate.isWithinBoundaries() =
        (this.first in 0..lastRowIndex && this.second in 0..lastColIndex)
}