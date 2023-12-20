import kotlin.coroutines.cancellation.CancellationException

fun main() {
    abstract class Component(val receivers: List<String>) {
        open fun reset() {}

        //Return value (signal, propagate)
        abstract fun processInput(isHighSignal: Boolean, sender: String): Pair<Boolean, Boolean>
    }

    class Broadcaster(receivers: List<String>) : Component(receivers) {
        override fun processInput(isHighSignal: Boolean, sender: String): Pair<Boolean, Boolean> {
            return Pair(isHighSignal, true)
        }
    }

    class Debugger : Component(listOf()) {
        private var privateLastSignal: Boolean? = null
        override fun processInput(isHighSignal: Boolean, sender: String): Pair<Boolean, Boolean> {
            privateLastSignal = isHighSignal
            return Pair(isHighSignal, false)
        }
    }

    class FlipFlop(receivers: List<String>) : Component(receivers) {
        private var isOn = false

        override fun reset() {
            isOn = false
        }

        override fun processInput(isHighSignal: Boolean, sender: String): Pair<Boolean, Boolean> {
            if (isHighSignal) {
                return Pair(isOn, false)
            } else {
                isOn = !isOn
                return Pair(isOn, true)
            }
        }
    }

    class Conjunction(receivers: List<String>, senders: List<String>) : Component(receivers) {
        private val mutableLastReceivedSignals = senders.associateWith { false }.toMutableMap()
        val lastReceivedSignals
            get() = mutableLastReceivedSignals.toMap()

        override fun reset() {
            mutableLastReceivedSignals.keys.forEach { mutableLastReceivedSignals[it] = false }
        }

        override fun processInput(isHighSignal: Boolean, sender: String): Pair<Boolean, Boolean> {
            mutableLastReceivedSignals[sender] = isHighSignal
            val signal = !mutableLastReceivedSignals.all { it.value }
            return Pair(signal, true)
        }
    }

    class Circuit(components: Map<String, Component>) {
        val components = components.toMutableMap()
            .apply {
                putAll((components.values.flatMap { it.receivers }.toSet() - components.keys).map {
                    it to Debugger()
                })
            }

        private val counters = mutableMapOf(true to 0L, false to 0L)

        fun reset() {
            components.values.forEach { it.reset() }
        }

        fun getScore() = counters[true]!! * counters[false]!!

        fun pushButton(evaluateCancellation: () -> Boolean = { false }) {
            val queue = mutableListOf(Triple("button", false, ""))

            while (queue.isNotEmpty()) {
                val (receiverName, inputSignal, senderName) = queue.removeAt(0)
                val receiver = components[receiverName]!!
                val (outputSignal, shouldOutputPropagate) = receiver.processInput(inputSignal, senderName)
                if (shouldOutputPropagate) {
                    counters[outputSignal] = counters[outputSignal]!! + receiver.receivers.size
                    receiver.receivers.forEach {
                        queue.add(Triple(it, outputSignal, receiverName))
                    }
                }
                if (evaluateCancellation()) {
                    throw CancellationException()
                }
            }
        }
    }


    fun parseInput(input: List<String>): Circuit {
        val receiversToSenders = mutableMapOf<String, MutableSet<String>>()
        input.map { line ->
            val (sender, receiverString) = line.split(" -> ")
            receiverString.split(", ")
                .forEach { receiverName ->
                    val senderName = sender.dropWhile { it == '%' || it == '&' }
                    if (receiverName !in receiversToSenders) {
                        receiversToSenders[receiverName] = mutableSetOf(senderName)
                    } else {
                        receiversToSenders[receiverName]!!.add(senderName)
                    }
                }
        }

        val components = (input + "button -> broadcaster").associate {
            val (name, receiverString) = it.split(" -> ")
            val receiverNames = receiverString.split(", ")
            when {
                name.first() == '%' -> name.drop(1) to FlipFlop(receiverNames)

                name.first() == '&' ->
                    name.drop(1) to Conjunction(receiverNames, receiversToSenders[name.drop(1)]!!.toList())

                receiverNames.isNotEmpty() -> name to Broadcaster(receiverNames)
                else -> name to Debugger()
            }
        }.toMap()
        return Circuit(components)
    }

    fun part1(input: List<String>): Long {
        val circuit = parseInput(input)
        repeat(1000) {
            circuit.pushButton()
        }
        return circuit.getScore()
    }

    fun part2(input: List<String>): Long {
        val circuit = parseInput(input)
        val preTargetComponent = circuit.components.values
            .filter { "rx" in it.receivers }
            .also { check(it.size == 1) }
            .first() as Conjunction

        val offsetsRecurrences = preTargetComponent.lastReceivedSignals.keys.map {
            circuit.reset()

            var counter = 0L
            var firstOccurrence: Long? = null
            var secondOccurrence: Long? = -1L
            while (true) {
                ++counter
                try {
                    circuit.pushButton {
                        if (preTargetComponent.lastReceivedSignals[it] == true) {
                            if (firstOccurrence == null) {
                                firstOccurrence = counter
                            } else if (secondOccurrence == null) {
                                secondOccurrence = counter
                                return@pushButton true
                            }
                        }
                        return@pushButton false
                    }
                } catch (e: CancellationException) {
                    break
                }
                if (firstOccurrence != null && (secondOccurrence?.compareTo(0L) ?: 0) < 0) secondOccurrence = null
            }
            val recurrence = secondOccurrence!! - firstOccurrence!!
            Pair(firstOccurrence!! % recurrence, recurrence)
        }

        return if (offsetsRecurrences.all { it.first == 0L }) {
            offsetsRecurrences.fold(1) { acc: Long, pair: Pair<Long, Long> -> acc * pair.second }
        } else {
            solveChineseRemainderTheorem(offsetsRecurrences)
        }
    }

// test if implementation meets criteria from the description, like:
    val testInputA = readInput("Day20_testA")
    check(part1(testInputA) == 32000000L) { "Wrong solution: ${part1(testInputA)}" }
    val testInputB = readInput("Day20_testB")
    check(part1(testInputB) == 11687500L) { "Wrong solution: ${part1(testInputB)}" }

    val input = readInput("Day20")
    part1(input).println()
    part2(input).println()
}