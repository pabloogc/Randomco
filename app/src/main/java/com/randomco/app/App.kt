package com.randomco.app

import com.randomco.randomco.BuildConfig
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import mini.flux.FluxUtil
import org.jetbrains.annotations.TestOnly
import timber.log.Timber
import kotlin.properties.Delegates

private var appInstance: App by Delegates.notNull()
val app: App get() = appInstance

class App : DaggerApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return component
    }

    val exceptionHandlers: MutableList<Thread.UncaughtExceptionHandler> = ArrayList()

    private var componentInstance: AppComponent? = null
    val component: AppComponent
        get() {
            if (componentInstance == null) {
                componentInstance = DaggerAppComponent.builder()
                    .appModule(AppModule(app))
                    .build()
            }
            return componentInstance!!
        }

    override fun onCreate() {
        appInstance = this
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        val stores = component.stores()
        FluxUtil.initStores(stores.values.toList())
        FluxUtil.registerSystemCallbacks(component.dispatcher(), this)

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        exceptionHandlers.add(defaultHandler)
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            exceptionHandlers.forEach { it.uncaughtException(thread, error) }
        }

        component.inject(this)
    }

    /** Set or replace the component with a test one. */
    @TestOnly
    fun setAppComponent(appComponent: AppComponent) {
        componentInstance = appComponent
    }
}
