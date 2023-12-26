fun main() {
    data class Edge<T>(val nodes: Set<T>, val weight: Int) {
        fun getOtherNode(node: T) = nodes.first { it != node }
    }

    // Stoer-Wagner algorithm
    @Suppress("SpellCheckingInspection")
    data class Graph<T>(val nodeWeights: Map<T, Int>, val edges: List<Edge<T>>) {
        fun getCutFlowAndWeights(): Pair<Int, Pair<Int, Int>> {
            check(nodeWeights.size == 2 && edges.size == 1)
            val nodeList = nodeWeights.values.toList()
            return Pair(edges.first().weight, Pair(nodeList[0], nodeList[1]))
        }

        fun getMinimumCutNodeCounts( targetCut: Int = Int.MAX_VALUE): Pair<Int, Int> {
            var resultMergedGraph = this
            var bestCut = Int.MAX_VALUE
            var bestWeights = Pair(0, 0)
            while (resultMergedGraph.nodeWeights.size > 2) {
                var iterationMergedGraph = resultMergedGraph
                val nodeToKeepInIteration =
                    iterationMergedGraph.nodeWeights.keys.first()
                while (iterationMergedGraph.nodeWeights.size > 2) {
                    val nodeToRemoveInIteration =
                        iterationMergedGraph.getOtherNodeInWeightiestEdge(nodeToKeepInIteration)
                    iterationMergedGraph =
                        iterationMergedGraph.getCopyWithMergedNodes(
                            nodeToKeepInIteration,
                            nodeToRemoveInIteration
                        )
                }
                val (iterationCut, iterationWeights) = iterationMergedGraph.getCutFlowAndWeights()
                if (iterationCut < bestCut) {
                    if (iterationCut == targetCut) return iterationWeights
                    bestCut = iterationCut
                    bestWeights = iterationWeights
                }

                val nodeToRemoveInResult = iterationMergedGraph.nodeWeights.keys.first { it != nodeToKeepInIteration }
                val nodeToKeepInResult = resultMergedGraph.getOtherNodeInWeightiestEdge(nodeToRemoveInResult)
                resultMergedGraph = resultMergedGraph.getCopyWithMergedNodes(nodeToKeepInResult, nodeToRemoveInResult)
            }

            val (finalCut, finalWeights) = resultMergedGraph.getCutFlowAndWeights()
            return if (finalCut < bestCut) finalWeights else bestWeights
        }

        fun getCopyWithMergedNodes(keptNode: T, replacedNode: T): Graph<T> {
            val newNodes = nodeWeights
                .mapNotNull {
                    when (it.key) {
                        keptNode -> it.key to it.value + nodeWeights[replacedNode]!!
                        replacedNode -> null
                        else -> it.key to it.value
                    }
                }
                .toMap()
            val newEdges = edges
                .map {
                    if (replacedNode in it.nodes) it.copy(
                        nodes = setOf(
                            it.getOtherNode(replacedNode),
                            keptNode
                        )
                    ) else it
                }
                .filter { it.nodes.size == 2 }
                .groupBy { it.nodes }
                .map { Edge(it.key, it.value.sumOf { e -> e.weight }) }
            return Graph(newNodes, newEdges)
        }

        fun getOtherNodeInWeightiestEdge(node: T) = edges
            .filter { node in it.nodes }
            .maxBy { it.weight }
            .getOtherNode(node)
    }

    fun parseInput(input: List<String>): Graph<String> {
        val edges = input.flatMap { line ->
            val splitLine = line.replace(":", "").split(" ")
            val n1 = splitLine.first()
            splitLine.drop(1).map { Edge(setOf(n1, it), 1) }
        }
        val nodes = edges.flatMap { it.nodes }.toSet().associateWith { 1 }
        return Graph(nodes, edges)
            .getCopyWithMergedNodes("", "")
    }

    fun solve(input: List<String>): Int {
        val graph = parseInput(input)
        val minimumCutNodeCounts = graph.getMinimumCutNodeCounts(3)
        return (minimumCutNodeCounts.first * minimumCutNodeCounts.second)
    }

    fun part2(input: List<String>): Int {
        return solve(input)
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day25_test")
    check(solve(testInput) == 54) { "Wrong solution: ${solve(testInput)}" }

    val input = readInput("Day25")
    solve(input).println()
}
