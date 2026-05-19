package cz.cvut.fel.zan.cookbook.feature.recipelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.cvut.fel.zan.cookbook.core.data.ApiResult
import cz.cvut.fel.zan.cookbook.core.data.model.Category
import cz.cvut.fel.zan.cookbook.core.data.model.RecipeIngredient
import cz.cvut.fel.zan.cookbook.core.data.model.Recipe
import cz.cvut.fel.zan.cookbook.core.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface RecipeListUiState {
    object Loading : RecipeListUiState
    data class Content(
        val recipes: List<Recipe>,
        val searchQuery: String,
        val showFavouritesOnly: Boolean = false,
        val selectedCategories: Set<Category> = emptySet(),
        val selectedIds: Set<Long> = emptySet(),
        val isSelectionMode: Boolean = false,
    ) : RecipeListUiState
    data class Error(val message: String) : RecipeListUiState
}

sealed interface RecipeListEvent {
    data class ToggleFavourite(val id: Long) : RecipeListEvent
    data class SearchQueryChanged(val query: String) : RecipeListEvent
    data class ToggleFavouritesFilter(val enabled: Boolean) : RecipeListEvent
    data class ToggleCategoryFilter(val category: Category) : RecipeListEvent
    data class ToggleSelection(val id: Long) : RecipeListEvent
    data object SelectAll : RecipeListEvent
    data object ClearSelection : RecipeListEvent
    data object DeleteSelected : RecipeListEvent
    data object FavouriteSelected : RecipeListEvent
}

class RecipeListViewModel(
    private val repository: RecipeRepository,
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    private val _showFavouritesOnly = MutableStateFlow(false)
    private val _selectedCategories = MutableStateFlow(emptySet<Category>())
    private val _selectedIds = MutableStateFlow(emptySet<Long>())
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _refreshError = MutableStateFlow<String?>(null)
    val refreshError: StateFlow<String?> = _refreshError.asStateFlow()

    init {
        refreshRecipes()
    }

    private val _uiState: StateFlow<RecipeListUiState> = combine(
        repository.getRecipes(),
        _searchQuery,
        _showFavouritesOnly,
        _selectedCategories,
        _selectedIds,
    ) { recipes, query, favOnly, categories, selectedIds ->
        val filtered = recipes
            .filter { it.title.contains(query.trim(), ignoreCase = true) }
            .filter { if (favOnly) it.isFavorite else true }
            .filter { categories.isEmpty() || it.category in categories }

        RecipeListUiState.Content(
            recipes = filtered,
            searchQuery = query,
            showFavouritesOnly = favOnly,
            selectedCategories = categories,
            selectedIds = selectedIds,
            isSelectionMode = selectedIds.isNotEmpty(),
        ) as RecipeListUiState
    }
        .catch { e ->
            emit(RecipeListUiState.Error(e.message ?: "Unknown error"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RecipeListUiState.Loading,
        )

    val uiState: StateFlow<RecipeListUiState> = _uiState

    fun onEvent(event: RecipeListEvent) {
        when (event) {
            is RecipeListEvent.ToggleFavourite ->{
                viewModelScope.launch {
                    repository.toggleFavourite(event.id)
                }
            }
            is RecipeListEvent.SearchQueryChanged -> {
                _searchQuery.value = event.query
            }
            is RecipeListEvent.ToggleFavouritesFilter -> {
                _showFavouritesOnly.value = event.enabled
            }
            is RecipeListEvent.ToggleCategoryFilter -> {
                _selectedCategories.update { current ->
                    if (event.category in current) current - event.category
                    else current + event.category
                }
            }
            is RecipeListEvent.ToggleSelection -> {
                _selectedIds.update { current ->
                    if (event.id in current) current - event.id else current + event.id
                }
            }
            is RecipeListEvent.SelectAll -> {
                val current = _uiState.value
                if (current is RecipeListUiState.Content) {
                    _selectedIds.value = current.recipes.map { it.id }.toSet()
                }
            }
            is RecipeListEvent.ClearSelection -> {
                _selectedIds.value = emptySet()
            }
            is RecipeListEvent.DeleteSelected -> {
                val ids = _selectedIds.value
                _selectedIds.value = emptySet()
                viewModelScope.launch {
                    repository.deleteRecipesByIds(ids)
//                    _uiEvents.emit(UiEvent.ShowSnackbar("${ids.size} items deleted"))
                }
            }
            is RecipeListEvent.FavouriteSelected -> {
                val ids = _selectedIds.value
                _selectedIds.value = emptySet()
                viewModelScope.launch {
                    repository.setFavourite(ids)
                }
            }
        }
    }

    fun refreshRecipes() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _refreshError.value = null
            when (val result = repository.refreshRecipes()) {
                is ApiResult.Success -> { /* data flows automatically via Room */ }
                is ApiResult.Error -> {
                    _refreshError.value = result.message
                        ?: "Unknown error: ${result.exception.message}"
                }
                is ApiResult.Loading -> { /* not used here */ }
            }
            _isRefreshing.value = false
        }
    }
}