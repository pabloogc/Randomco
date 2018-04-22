package mini.log

import com.randomco.app.app
import dagger.Binds
import dagger.Module
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap
import mini.app.OnActivityLifeCycleAction
import mini.app.OnActivityLifeCycleAction.ActivityStage.DESTROYED
import mini.app.OnActivityLifeCycleAction.ActivityStage.STOPPED
import mini.dagger.AppScope
import mini.flux.Dispatcher
import mini.flux.LazyStoreMap
import mini.flux.Store
import timber.log.Timber
import javax.inject.Inject

/**
 * Store that tracks logging throughout the app.
 */
@AppScope
class LoggerStore @Inject constructor(private val dispatcher: Dispatcher,
                                      private val lazyStoreMap: LazyStoreMap) : Store<LoggerState>() {

    private val fileLogController = FileLogController(app)
    override fun initialState() = LoggerState()

    override fun init() {
        val fileTree = fileLogController.newFileTree()
        fileTree?.let {
            Timber.plant(fileTree)
        }

        dispatcher.subscribe(OnActivityLifeCycleAction::class) {
            if (it.stage == STOPPED || it.stage == DESTROYED) fileTree?.flush()
        }

        dispatcher.addInterceptor(LoggerInterceptor(lazyStoreMap.get().values))
        app.exceptionHandlers.add(Thread.UncaughtExceptionHandler { _, _ ->
            fileTree?.flush()
        })

        Timber.v("${fileLogController.deleteOldLogs()} old log files deleted")
    }
}

@Module
@Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")
interface LoggerModule {
    @Binds @AppScope @IntoMap @ClassKey(LoggerStore::class)
    fun storeToMap(store: LoggerStore): Store<*>
}