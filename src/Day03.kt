fun main() {
    val numRegex = Regex("""\d+""")

    data class Symbol(val symbol: Char, val number: Int, val coordinate: Pair<Int, Int>)

    fun getSymbols(input: List<String>): List<Symbol> {
            val isSymbolMap = input.map { row -> row.map { it != '.' && !it.isDigit() } }
            val mutableSymbols = mutableListOf<Symbol>()

            for (match in numRegex.findAll(input.first())) {
                val indSymFirst = (if (match.range.first > 0) match.range.first - 1 else 0)
                val indSymLast =
                    (if (match.range.last < input.first().lastIndex) match.range.last + 1 else input.first().lastIndex)
                symbolSearch@ for (iRowSym in 0..1) {
                    for (iColSym in indSymFirst..indSymLast) {
                        if (isSymbolMap[iRowSym][iColSym]) {
                            mutableSymbols.add(
                                Symbol(
                                    input[iRowSym][iColSym],
                                    match.groupValues.first().toInt(),
                                    Pair(iRowSym, iColSym)
                                )
                            )
                            break@symbolSearch
                        }
                    }
                }
            }

            for ((iRow, row) in input.withIndex()) {
                if (iRow == 0 || iRow == input.lastIndex) continue

                for (match in numRegex.findAll(row)) {
                    val indSymFirst = (if (match.range.first > 0) match.range.first - 1 else 0)
                    val indSymLast =
                        (if (match.range.last < row.lastIndex) match.range.last + 1 else row.lastIndex)
                    symbolSearch@ for (iRowSym in iRow-1..iRow+1) {
                        for (iColSym in indSymFirst..indSymLast) {
                            if (isSymbolMap[iRowSym][iColSym]) {
                                mutableSymbols.add(
                                    Symbol(
                                        input[iRowSym][iColSym],
                                        match.groupValues.first().toInt(),
                                        Pair(iRowSym, iColSym)
                                    )
                                )
                                break@symbolSearch
                            }
                        }
                    }
                }

            }

            for (match in numRegex.findAll(input.last())) {
                val indSymFirst = (if (match.range.first > 0) match.range.first - 1 else 0)
                val indSymLast =
                    (if (match.range.last < input.last().lastIndex) match.range.last + 1 else input.last().lastIndex)
                symbolSearch@ for (iRowSym in input.lastIndex-1..input.lastIndex) {
                    for (iColSym in indSymFirst..indSymLast) {
                        if (isSymbolMap[iRowSym][iColSym]) {
                            mutableSymbols.add(
                                Symbol(
                                    input[iRowSym][iColSym],
                                    match.groupValues.first().toInt(),
                                    Pair(iRowSym, iColSym)
                                )
                            )
                            break@symbolSearch
                        }
                    }
                }
            }

            return mutableSymbols.toList()
    }

    fun part1(input: List<String>): Int {
        val symbols = getSymbols(input)
        return symbols.sumOf { it.number }
    }

    fun part2(input: List<String>): Int {
        val symbols = getSymbols(input)
        val gearRatios = symbols.groupBy { it.coordinate }.filter { it.value.size==2 }.map { it.value.first().number * it.value.last().number}
        return gearRatios.sum()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day03_test")
    check(part1(testInput) == 4361) { "Wrong solution: ${part1(testInput)}" }
    check(part2(testInput) == 467835) { "Wrong solution: ${part2(testInput)}" }

    val input = readInput("Day03")
    part1(input).println()
    part2(input).println()
}
