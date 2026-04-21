package cz.cvut.fel.zan.cookbook.di

import cz.cvut.fel.zan.cookbook.feature.recipelist.RecipeListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { RecipeListViewModel(repository = get()) }
}