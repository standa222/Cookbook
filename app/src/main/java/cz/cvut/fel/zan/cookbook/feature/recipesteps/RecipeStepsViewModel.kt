package cz.cvut.fel.zan.cookbook.feature.recipesteps

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

sealed interface RecipeStepsUiState {
    data object Loading : RecipeStepsUiState
    data class Content(val recipe: Recipe) : RecipeStepsUiState
    data class Error(val message: String) : RecipeStepsUiState
}

sealed interface RecipeStepsEvent {
    data object DeleteRecipe : RecipeStepsEvent
}

class RecipeStepsViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: RecipeRepository,
) : ViewModel() {

    private val recipeId: Long = checkNotNull(savedStateHandle.get<Long>("recipeId"))

    val uiState: StateFlow<RecipeStepsUiState> = repository.getRecipeById(recipeId)
        .map { recipe ->
            if (recipe == null) RecipeStepsUiState.Error("Recipe not found")
            else RecipeStepsUiState.Content(recipe)
        }
        .catch { e -> emit(RecipeStepsUiState.Error(e.message ?: "Unknown error")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RecipeStepsUiState.Loading,
        )

    fun onEvent(event: RecipeStepsEvent) {
        when (event) {
            RecipeStepsEvent.DeleteRecipe -> {
                viewModelScope.launch {
                    repository.deleteRecipesByIds(setOf(recipeId))
                }
            }
        }
    }
}
