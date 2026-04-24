package cz.cvut.fel.zan.cookbook.feature.recipeingredients

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.cvut.fel.zan.cookbook.core.data.model.Recipe
import cz.cvut.fel.zan.cookbook.core.data.repository.RecipeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface RecipeIngredientsUiState {
    data object Loading : RecipeIngredientsUiState
    data class Content(val recipe: Recipe) : RecipeIngredientsUiState
    data class Error(val message: String) : RecipeIngredientsUiState
}

sealed interface RecipeIngredientsEvent {
    data object DeleteRecipe : RecipeIngredientsEvent
}

class RecipeIngredientsViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: RecipeRepository,
) : ViewModel() {

    private val recipeId: Long = checkNotNull(savedStateHandle.get<Long>("recipeId"))

    val uiState: StateFlow<RecipeIngredientsUiState> = repository.getRecipeById(recipeId)
        .map { recipe ->
            if (recipe == null) RecipeIngredientsUiState.Error("Recipe not found")
            else RecipeIngredientsUiState.Content(recipe)
        }
        .catch { e -> emit(RecipeIngredientsUiState.Error(e.message ?: "Unknown error")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RecipeIngredientsUiState.Loading,
        )

    fun onEvent(event: RecipeIngredientsEvent) {
        when (event) {
            RecipeIngredientsEvent.DeleteRecipe -> {
                viewModelScope.launch {
                    repository.deleteRecipesByIds(setOf(recipeId))
                }
            }
        }
    }
}
