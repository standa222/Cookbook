package cz.cvut.fel.zan.cookbook.di

import cz.cvut.fel.zan.cookbook.feature.ingredientlist.IngredientListViewModel
import cz.cvut.fel.zan.cookbook.feature.recipeingredients.RecipeIngredientsViewModel
import cz.cvut.fel.zan.cookbook.feature.recipeeditor.RecipeEditorViewModel
import cz.cvut.fel.zan.cookbook.feature.recipesteps.RecipeStepsViewModel
import cz.cvut.fel.zan.cookbook.feature.recipelist.RecipeListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { RecipeListViewModel(repository = get()) }
    viewModel { RecipeEditorViewModel(repository = get(), ingredientRepository = get(), savedStateHandle = get()) }
    viewModel { IngredientListViewModel(repository = get()) }
    viewModel { RecipeIngredientsViewModel(repository = get(), savedStateHandle = get()) }
    viewModel { RecipeStepsViewModel(repository = get(), savedStateHandle = get()) }
}