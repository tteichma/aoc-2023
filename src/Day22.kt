fun main() {
    data class Block(val id: Int, val x: IntRange, val y: IntRange, val z: IntRange)

    data class BlockGroup(val blocks: List<Block>) {
        fun getNumRemovableBlocks() = blocks
            .indices
            .map { BlockGroup(blocks.filterIndexed { ind, _ -> ind != it }) }
            .count { it.getNumMovedBlocksAfterFalling() == 0}


        fun getRemovalNumber() = blocks
            .indices
            .map { BlockGroup(blocks.filterIndexed { ind, _ -> ind != it }) }
            .sumOf { it.getNumMovedBlocksAfterFalling().toLong()}


        fun getNumMovedBlocksAfterFalling(): Int {
            val blocksAfterFallingById = getBlockGroupAfterFalling().blocks.associateBy { it.id }
            val result = blocks.count { it != blocksAfterFallingById[it.id] }
            return result
        }

        fun getBlockGroupAfterFalling(): BlockGroup {
            var newBlockGroup = this
            do {
                val (stableBlocks, fallingBlocks) = newBlockGroup.getBlocksToSupportingBlocks()
                    .toList()
                    .partition { it.first.z.first == 1 || it.second.isNotEmpty() }

                newBlockGroup =
                    BlockGroup(stableBlocks.map { it.first } + fallingBlocks.map { it.first.copy(z = (it.first.z.first - 1)..<it.first.z.last) })
            } while (fallingBlocks.isNotEmpty())
            return newBlockGroup
        }

        fun getBlocksToSupportingBlocks(): Map<Block, List<Block>> {
            val blocksByLevelAbove = blocks.groupBy { it.z.last + 1 }

            return blocks.associateWith { block ->
                blocksByLevelAbove[block.z.first]?.filter {
                    it.x.intersect(block.x).isNotEmpty() && it.y.intersect(block.y).isNotEmpty()
                } ?: listOf()
            }
        }
    }

    fun parseInput(input: List<String>): BlockGroup {
        var counter = 0
        return BlockGroup(
            input.map { line ->
                getUnsignedIntsFromString(line)
                    .toList()
                    .let { Block(counter++, it[0]..it[3], it[1]..it[4], it[2]..it[5]) }
            }
        )
    }

    fun part1(input: List<String>): Long {
        val blockGroup = parseInput(input).getBlockGroupAfterFalling()
        return blockGroup.getNumRemovableBlocks().toLong()
    }

    fun part2(input: List<String>): Long {
        val blockGroup = parseInput(input).getBlockGroupAfterFalling()
        return blockGroup.getRemovalNumber()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day22_test")
    check(part1(testInput) == 5L) { "Wrong solution: ${part1(testInput)}" }
    check(part2(testInput) == 7L) { "Wrong solution: ${part2(testInput)}" }

    val input = readInput("Day22")
    part1(input).println()
    part2(input).println()
}
