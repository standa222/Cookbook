package cz.cvut.fel.zan.cookbook.di

import androidx.room.Room
import cz.cvut.fel.zan.cookbook.core.data.database.AppDatabase
import cz.cvut.fel.zan.cookbook.core.data.repository.RecipeRepository
import cz.cvut.fel.zan.cookbook.core.data.repository.RecipeRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {

    // Database
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "cookbook_database"
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    // DAO – derived from the singleton database
    single { get<AppDatabase>().recipeDao() }
    single { get<AppDatabase>().ingredientDao() }

    // TODO A4 Network - REST API

    // Repository
    single<RecipeRepository> {
        RecipeRepositoryImpl(
            recipeDao = get(),
            ingredientDao = get(),
            // TODO A4 Network - REST API
        )
    }
}