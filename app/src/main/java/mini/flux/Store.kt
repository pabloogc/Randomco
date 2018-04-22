package mini.flux

import android.support.annotation.CallSuper
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import org.jetbrains.annotations.TestOnly
import java.lang.reflect.ParameterizedType

/**
 * Generic store that exposes its state as a [Flowable] and emits change events
 * when [.setState] is called.
 *
 * @param <S> The state type.
 */
abstract class Store<S : Any> : AutoCloseable {

    open val properties: StoreProperties = StoreProperties()

    private val disposables = CompositeDisposable()
    private var _state: S? = null
    private val processor = PublishProcessor.create<S>()

    var state: S
        get() {
            if (_state == null) _state = initialState()
            return _state!!
        }
        protected set(value) {
            if (value != _state) {
                _state = value
                processor.onNext(value)
            }
        }

    @Suppress("UNCHECKED_CAST")
    protected open fun initialState(): S {
        val type = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
            as Class<S>
        try {
            val constructor = type.getDeclaredConstructor()
            constructor.isAccessible = true
            return constructor.newInstance()
        } catch (e: Exception) {
            throw RuntimeException("Missing default no-args constructor for the state $type", e)
        }
    }

    /**
     * Returns a [Flowable] to listen to state changes in the store.
     */
    fun flowable(): Flowable<S> {
        return processor.startWith { s ->
            s.onNext(state)
            s.onComplete()
        }
    }

    /**
     * Tracks a given subscription.
     */
    fun Disposable.track() {
        disposables.add(this)
    }

    @CallSuper
    override fun close() {
        disposables.dispose()
    }

    /**
     * Set dummy state in this store. Only for tests.
     * Can be called from any thread, the change will run on UI if called
     * from a background thread, mostly used to simplify Espresso tests.
     */
    @TestOnly
    fun setTestState(other: S) {
        onUiSync {
            this.state = other
        }
    }

    /**
     * Reset initial state in this store.
     * Can be called from any thread, the change will run on UI if called
     * from a background thread, mostly used to simplify Espresso tests.
     */
    @TestOnly
    fun resetState() {
        setTestState(initialState())
    }

    /**
     * Initialize the store. Called after all stores constructors ar
     */
    abstract fun init()
}

/**
 * Store meta properties.
 *
 * @param initOrder After construction invocation priority. Higher is lower.
 */
data class StoreProperties(
    val initOrder: Int = DEFAULT_INIT_PRIORITY) {
    companion object {
        const val DEFAULT_INIT_PRIORITY = 100
    }
}