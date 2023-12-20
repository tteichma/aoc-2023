fun solveChineseRemainderTheorem(inputs: List<Pair<Long, Long>>): Long {
    // NOTE: Does not work for recurrences sharing lcd, yet.
    val offsets = inputs.map { it.first }  // a_i
    val recurrences = inputs.map { it.second }  // m_i

    val recurrencesProduct = recurrences.reduce { acc, n -> acc * n }
    val factors = recurrences.map { recurrencesProduct / it }
    val x = inputs.indices.map { i -> (1..<recurrences[i]).first { (it * factors[i]) % recurrences[i] == 1L } }

    val solution = inputs.indices.sumOf { offsets[it] * factors[it] * x[it] }
    return solution % recurrencesProduct
}
