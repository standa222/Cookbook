package cz.cvut.fel.zan.cookbook.feature.recipelist

import androidx.lifecycle.ViewModel
import cz.cvut.fel.zan.cookbook.core.data.model.RecipeIngredient
import cz.cvut.fel.zan.cookbook.core.data.model.Recipe
import cz.cvut.fel.zan.cookbook.core.data.repository.RecipeRepository

sealed interface RecipeListUiState {
    object Loading : RecipeListUiState
    data class Content(
        val recipes: List<Recipe>,
        val searchQuery: String,
        val showFavouritesOnly: Boolean = false,
        val selectedIngredients: Set<RecipeIngredient> = emptySet(),
        val selectedIds: Set<Long> = emptySet(),
        val isSelectionMode: Boolean = false,
    ) : RecipeListUiState
    data class Error(val message: String) : RecipeListUiState
}

sealed interface RecipeListEvent {
    data class ToggleFavourite(val id: Long) : RecipeListEvent
    data class SearchQueryChanged(val query: String) : RecipeListEvent
    data class ToggleFavouritesFilter(val enabled: Boolean) : RecipeListEvent
    data class ToggleIngredientFilter(val ingredient: RecipeIngredient) : RecipeListEvent
    data class ToggleSelection(val id: Long) : RecipeListEvent
    data object SelectAll : RecipeListEvent
    data object ClearSelection : RecipeListEvent
    data object DeleteSelected : RecipeListEvent
    data object FavouriteSelected : RecipeListEvent
}

class RecipeListViewModel(
    private val repository: RecipeRepository,
) : ViewModel() {
}