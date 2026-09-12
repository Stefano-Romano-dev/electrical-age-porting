package mods.eln.sim.mna

import mods.eln.sim.mna.component.Component
import mods.eln.sim.mna.misc.IDestructor
import mods.eln.sim.mna.misc.ISubSystemProcessI
import mods.eln.sim.mna.state.State
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.RealMatrix
import org.apache.commons.math3.linear.SingularMatrixException
import org.apache.commons.math3.linear.QRDecomposition

/**
 * A platform-independent modified nodal analysis system.
 *
 * This first port deliberately retains the 1.24.8 matrix construction and QR
 * inverse workflow so numerical parity can be established before refactoring.
 */
class SubSystem(
    val root: RootSystem? = null,
    val dt: Double,
) {
    private val mutableComponents = mutableListOf<Component>()
    private val mutableStates = mutableListOf<State>()
    private val rhsProcesses = mutableListOf<ISubSystemProcessI>()

    val components: List<Component>
        get() = mutableComponents

    val states: List<State>
        get() = mutableStates

    val interSystemConnectivity = mutableSetOf<SubSystem>()
    val breakDestructors = ArrayDeque<IDestructor>()

    private var broken = false

    private var matrixValid = false
    private var singularMatrix = false
    private var matrix: RealMatrix? = null
    private var inverseData: Array<DoubleArray> = emptyArray()
    private var rhsData: DoubleArray = doubleArrayOf()
    private var pendingStateData: DoubleArray = doubleArrayOf()
    private var stateTable: Array<State> = emptyArray()

    val stateCount: Int
        get() = mutableStates.size

    val componentCount: Int
        get() = mutableComponents.size

    val isSingular: Boolean
        get() {
            ensureMatrix()
            return singularMatrix
        }

    fun addState(state: State): SubSystem = apply {
        require(state.subSystem == null || state.subSystem === this) {
            "State already belongs to another subsystem"
        }
        if (state !in mutableStates) {
            mutableStates += state
            state.attachTo(this)
            invalidate()
        }
    }

    fun removeState(state: State) {
        if (mutableStates.remove(state)) {
            state.detachFromSubSystem()
            invalidate()
        }
    }

    fun addComponent(component: Component): SubSystem = apply {
        require(component.subSystem == null || component.subSystem === this) {
            "Component already belongs to another subsystem"
        }
        if (component !in mutableComponents) {
            mutableComponents += component
            component.addToSubsystem(this)
            invalidate()
        }
    }

    fun removeComponent(component: Component) {
        if (mutableComponents.remove(component)) {
            component.quitSubSystem()
            invalidate()
        }
    }

    fun addProcess(process: ISubSystemProcessI) {
        if (process !in rhsProcesses) rhsProcesses += process
    }

    fun removeProcess(process: ISubSystemProcessI) {
        rhsProcesses -= process
    }

    fun invalidate() {
        matrixValid = false
    }

    fun contains(state: State): Boolean = state in mutableStates

    fun breakSystem(): Boolean {
        if (broken) return false

        while (breakDestructors.isNotEmpty()) {
            breakDestructors.removeFirst().destruct()
        }

        mutableComponents.toList().forEach { it.quitSubSystem() }
        mutableStates.toList().forEach { it.detachFromSubSystem() }

        root?.let { rootSystem ->
            mutableComponents.forEach { it.returnToRootSystem(rootSystem) }
            mutableStates.forEach { it.returnToRootSystem(rootSystem) }
            rootSystem.systems.remove(this)
        }

        invalidate()
        broken = true
        return true
    }

    internal fun addToA(a: State?, b: State?, value: Double) {
        if (a == null || b == null) return
        matrix!!.addToEntry(a.id, b.id, value)
    }

    /** Retains the legacy assignment semantics until numerical parity is locked. */
    internal fun addToI(state: State?, value: Double) {
        if (state == null) return
        rhsData[state.id] = value
    }

    fun step() {
        stepCalc()
        stepFlush()
    }

    fun stepCalc() {
        ensureMatrix()
        if (singularMatrix) return

        rhsData.fill(0.0)
        rhsProcesses.forEach { it.simProcessI(this) }

        for (row in pendingStateData.indices) {
            var value = 0.0
            for (column in rhsData.indices) {
                value += inverseData[row][column] * rhsData[column]
            }
            pendingStateData[row] = value
        }
    }

    fun stepFlush() {
        ensureMatrix()
        if (singularMatrix) {
            stateTable.forEach { it.state = 0.0 }
        } else {
            stateTable.forEachIndexed { index, state -> state.state = pendingStateData[index] }
        }
    }

    fun solve(state: State): Double {
        ensureMatrix()
        if (singularMatrix) return 0.0

        rhsData.fill(0.0)
        rhsProcesses.forEach { it.simProcessI(this) }
        return rhsData.indices.sumOf { inverseData[state.id][it] * rhsData[it] }
    }

    private fun ensureMatrix() {
        if (!matrixValid) generateMatrix()
    }

    private fun generateMatrix() {
        mutableStates.forEachIndexed { index, state -> state.id = index }
        matrix = MatrixUtils.createRealMatrix(mutableStates.size, mutableStates.size)
        rhsData = DoubleArray(mutableStates.size)
        pendingStateData = DoubleArray(mutableStates.size)
        stateTable = mutableStates.toTypedArray()

        mutableComponents.forEach { it.applyToSubsystem(this) }

        try {
            inverseData = QRDecomposition(matrix).solver.inverse.data
            singularMatrix = false
        } catch (_: SingularMatrixException) {
            inverseData = emptyArray()
            singularMatrix = true
        }
        matrixValid = true
    }
}
