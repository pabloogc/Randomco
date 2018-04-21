package mini.log

import com.randomco.randomco.BuildConfig
import mini.flux.Action
import mini.flux.Chain
import mini.flux.Interceptor
import mini.flux.Store
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.reflect.KVisibility
import kotlin.reflect.jvm.kotlinProperty

/** Actions implementing this interface won't log anything */
interface SilentTag

internal class LoggerInterceptor constructor(stores: Collection<Store<*>>) : Interceptor {
    private val stores = stores.toList()
    private val states by lazy { stores.map { it.state }.toTypedArray() }
    private var lastActionTime = System.currentTimeMillis()
    private var actionCounter: Long = 0
    private var diffFinder = DiffFinder("com.randomco")

    override fun invoke(action: Action, chain: Chain): Action {
        states //eval lazy for first action

        //send the action
        val start = System.currentTimeMillis()
        val timeSinceLastAction = Math.min(start - lastActionTime, 9999)
        lastActionTime = start
        actionCounter++
        val out = chain.proceed(action)
        val processTime = System.currentTimeMillis() - start

        if (action is SilentTag) { //Just update the states
            stores.forEachIndexed { index, store ->
                states[index] = store.state
            }
        } else {
            val sb = StringBuilder()
            sb.append("┌────────────────────────────────────────────\n")
            sb.append(String.format("├─> %s %dms [+%dms][%d] - %s",
                action.javaClass.simpleName, processTime, timeSinceLastAction, actionCounter % 10, action))
                .append("\n")

            stores.forEachIndexed { index, store ->
                val oldState = states[index]
                val newState = store.state
                if (oldState !== newState) {
                    //This operation is costly, don't do it in prod
                    val fn = if (BuildConfig.DEBUG) {
                        {
                            val diff = diffFinder.diff(oldState, newState)
                            "${store.javaClass.simpleName}: $diff"
                        }
                    } else {
                        { store.state.toString() }
                    }
                    sb.append(String.format("│   %s", fn())).append("\n")
                }
                states[index] = newState
            }

            sb.append("└────────────────────────────────────────────").append("\n")
            if (action !is SilentTag) {
                Timber.tag("LoggerStore").i(sb.toString())
            }
        }

        return out
    }
}

private data class DiffResult(val diffs: List<Diff>, val diffTime: Long) {
    override fun toString(): String {
        return diffs.joinToString(separator = ", ")
    }
}

private data class Diff(val path: String, val a: Any?, val b: Any?) {
    override fun toString(): String {
        return "$path=($a ~> $b)"
    }
}

/**
 * Scan public properties and find differences for objects inside the application package.
 * Java / Kotlin types are ignored, only default equality is performed.
 */
private data class DiffFinder(private val packagePath: String) {

    /**
     * Diff the two objects, must have the same type.
     */
    fun <T : Any> diff(a: T?, b: T?): DiffResult {
        val start = System.nanoTime()
        val out = ArrayList<Diff>()
        diff(Stack(), out, a, b)
        val elapsed = System.nanoTime() - start
        return DiffResult(out, TimeUnit.NANOSECONDS.toMillis(elapsed))
    }

    private fun <T : Any> diff(crumbs: Stack<String>,
                               diffs: MutableList<Diff>,
                               a: T?, b: T?) {

        val anyIsNull = a == null || b == null
        //Enums blow up for some unknown reason
        val isEnum = a?.javaClass?.isEnum ?: false
        //Need this to avoid performing reflection for external types
        val fromTargetPackage = a?.javaClass
            ?.`package`
            ?.name
            ?.startsWith(packagePath)
            ?: false
        if (anyIsNull || isEnum || !fromTargetPackage) {
            if (a != b) {
                diffs.add(Diff(crumbs.joinToString(separator = "."), a, b))
            }
        } else {
            for (field in a!!.javaClass.declaredFields) {
                val prop = field.kotlinProperty ?: continue
                //No reason to inspect non-public props
                if (prop.getter.visibility != KVisibility.PUBLIC) continue
                crumbs.push(prop.name)
                diff(crumbs, diffs, prop.getter.call(a), prop.getter.call(b))
                crumbs.pop()
            }
        }
    }
}