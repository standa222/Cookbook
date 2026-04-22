package cz.cvut.fel.zan.cookbook.feature.ingredientlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.cvut.fel.zan.cookbook.core.data.model.IngredientUsageCount
import cz.cvut.fel.zan.cookbook.core.data.repository.IngredientRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

sealed interface IngredientListUiState {
    data object Loading : IngredientListUiState

    data class Content(
        val ingredients: List<IngredientUsageCount>,
        val searchQuery: String,
        val selectedIds: Set<Long> = emptySet(),
        val isSelectionMode: Boolean = false,
    ) : IngredientListUiState

    data class Error(val message: String) : IngredientListUiState
}

sealed interface IngredientListEvent {
    data class SearchQueryChanged(val query: String) : IngredientListEvent
    data class ToggleSelection(val id: Long) : IngredientListEvent
    data object SelectAll : IngredientListEvent
    data object ClearSelection : IngredientListEvent

    data class DeleteIngredient(val id: Long) : IngredientListEvent

    /**
     * Placeholder action: merge selected ingredients into one.
     * Not implemented yet (we'll add the actual merge behavior later).
     */
    data object MergeSelected : IngredientListEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
class IngredientListViewModel(
    private val repository: IngredientRepository,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedIds = MutableStateFlow(emptySet<Long>())

    private val ingredientsFlow = _searchQuery
        .flatMapLatest { query ->
            repository.searchIngredientsWithRecipeCount(query)
        }

    private val _uiState: StateFlow<IngredientListUiState> = combine(
        ingredientsFlow,
        _searchQuery,
        _selectedIds,
    ) { ingredients, query, selectedIds ->
        IngredientListUiState.Content(
            ingredients = ingredients,
            searchQuery = query,
            selectedIds = selectedIds,
            isSelectionMode = selectedIds.isNotEmpty(),
        ) as IngredientListUiState
    }
        .catch { e ->
            emit(IngredientListUiState.Error(e.message ?: "Unknown error"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = IngredientListUiState.Loading,
        )

    val uiState: StateFlow<IngredientListUiState> = _uiState

    fun onEvent(event: IngredientListEvent) {
        when (event) {
            is IngredientListEvent.SearchQueryChanged -> {
                _searchQuery.value = event.query
            }

            is IngredientListEvent.ToggleSelection -> {
                _selectedIds.update { current ->
                    if (event.id in current) current - event.id else current + event.id
                }
            }

            IngredientListEvent.SelectAll -> {
                val current = _uiState.value
                if (current is IngredientListUiState.Content) {
                    _selectedIds.value = current.ingredients.map { it.id }.toSet()
                }
            }

            IngredientListEvent.ClearSelection -> {
                _selectedIds.value = emptySet()
            }

            is IngredientListEvent.DeleteIngredient -> {
                viewModelScope.launch {
                    val deleted = repository.deleteIngredientIfUnused(event.id)
                    if (deleted) {
                        _selectedIds.update { it - event.id }
                    }
                }
            }

            IngredientListEvent.MergeSelected -> {
                // TODO: implement merge UI + repository logic later.
                // For now, just leave selection intact.
            }
        }
    }
}
