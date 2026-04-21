package cz.cvut.fel.zan.cookbook

import android.app.Application
import cz.cvut.fel.zan.cookbook.di.appModule
import cz.cvut.fel.zan.cookbook.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class CookbookApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.INFO)
            androidContext(this@CookbookApp)
            modules(appModule, viewModelModule)
        }
    }
}