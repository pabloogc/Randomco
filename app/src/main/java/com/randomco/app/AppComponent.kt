package com.randomco.app

import android.app.Application
import android.content.Context
import com.randomco.MainActivity
import com.randomco.network.NetworkModule
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import dagger.android.ContributesAndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import mini.dagger.ActivityScope
import mini.dagger.AppScope
import mini.flux.Dispatcher
import mini.flux.StoreHolderComponent
import mini.log.LoggerModule

@Component(modules = [
    AndroidInjectionModule::class,
    AndroidSupportInjectionModule::class,
    AndroidBindingsModule::class,
    AppModule::class,
    LoggerModule::class,
    NetworkModule::class
])

@AppScope
interface AppComponent : StoreHolderComponent, AndroidInjector<App> {
    fun dispatcher(): Dispatcher
}

@Module
interface AndroidBindingsModule {
    @ActivityScope @ContributesAndroidInjector
    fun mainActivity(): MainActivity
}

@Module
class AppModule(val app: App) {
    @Provides @AppScope
    fun provideDispatcher() = Dispatcher()

    @Provides
    fun provideApplication(): Application = app

    @Provides
    fun provideAppContext(): Context = app
}