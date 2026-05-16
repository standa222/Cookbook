package cz.cvut.fel.zan.cookbook.feature.recipeeditor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.cvut.fel.zan.cookbook.core.data.model.Category
import cz.cvut.fel.zan.cookbook.core.data.model.Ingredient
import cz.cvut.fel.zan.cookbook.core.data.model.InstructionStep
import cz.cvut.fel.zan.cookbook.core.data.model.Recipe
import cz.cvut.fel.zan.cookbook.core.data.model.RecipeIngredient
import cz.cvut.fel.zan.cookbook.core.data.repository.RecipeRepository
import cz.cvut.fel.zan.cookbook.core.data.repository.IngredientRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RecipeEditorUiState(
    val title: String = "",
    val description: String = "",
    val category: Category = Category.DEFAULT,
    val ingredients: List<RecipeIngredient> = emptyList(),
    val steps: List<InstructionStep> = emptyList(),
    val isEditMode: Boolean = false,
    val isSaving: Boolean = false,
    val isValid: Boolean = false,
)

sealed interface RecipeEditorEvent {
    data class TitleChanged(val title: String) : RecipeEditorEvent
    data class DescriptionChanged(val description: String) : RecipeEditorEvent
    data class CategoryChanged(val category: Category) : RecipeEditorEvent

    // Ingredient Events
    data class IngredientNameChanged(val index: Int, val name: String) : RecipeEditorEvent
    data class IngredientAmountChanged(val index: Int, val amount: String) : RecipeEditorEvent
    data class IngredientSelected(val index: Int, val ingredient: Ingredient) : RecipeEditorEvent
    data object AddIngredient : RecipeEditorEvent
    data class RemoveIngredient(val index: Int) : RecipeEditorEvent

    // Step Events
    data class StepDescriptionChanged(val index: Int, val description: String) : RecipeEditorEvent
    data class StepPrepTimeChanged(val index: Int, val prepTime: String) : RecipeEditorEvent
    data object AddStep : RecipeEditorEvent
    data class RemoveStep(val index: Int) : RecipeEditorEvent

    data object Save : RecipeEditorEvent
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class RecipeEditorViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: RecipeRepository,
    private val ingredientRepository: IngredientRepository,
) : ViewModel() {

    private val _ingredientQuery = MutableStateFlow("")
    val ingredientSuggestions: StateFlow<List<Ingredient>> = _ingredientQuery
        .debounce(300)
        .filter { it.length >= 2 }
        .distinctUntilChanged()
        .flatMapLatest { query ->
            ingredientRepository.searchIngredients(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
    private val recipeId: Long? = savedStateHandle.get<Long>("recipeId")

    var uiState by mutableStateOf(RecipeEditorUiState())
        private set

    init {
        if (recipeId != null) {
            // Edit mode — load existing data
            viewModelScope.launch {
                repository.getRecipeById(recipeId).collect { recipe ->
                    if (recipe != null) {
                        uiState = uiState.copy(
                            title = recipe.title,
                            description = recipe.description,
                            category = recipe.category,
                            ingredients = recipe.ingredients,
                            steps = recipe.instructionSteps,
                            isEditMode = true,
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: RecipeEditorEvent) {
        when (event) {
            is RecipeEditorEvent.TitleChanged -> {
                uiState = uiState.copy(title = event.title)
            }
            is RecipeEditorEvent.DescriptionChanged -> {
                uiState = uiState.copy(description = event.description)
            }
            is RecipeEditorEvent.CategoryChanged -> {
                uiState = uiState.copy(category = event.category)
            }

            // --- Ingredient Logic ---
            is RecipeEditorEvent.IngredientNameChanged -> {
                val newList = uiState.ingredients.toMutableList()
                newList[event.index] = newList[event.index].copy(name = event.name, ingredientId = 0L)
                uiState = uiState.copy(ingredients = newList)
                _ingredientQuery.value = event.name // Trigger the Flow search
            }
            is RecipeEditorEvent.IngredientAmountChanged -> {
                val newList = uiState.ingredients.toMutableList()
                newList[event.index] = newList[event.index].copy(quantity = event.amount)
                uiState = uiState.copy(ingredients = newList)
            }
            is RecipeEditorEvent.IngredientSelected -> {
                val newList = uiState.ingredients.toMutableList()
                newList[event.index] = newList[event.index].copy(
                    name = event.ingredient.name,
                    ingredientId = event.ingredient.id
                )
                uiState = uiState.copy(ingredients = newList)
                _ingredientQuery.value = ""
            }
            RecipeEditorEvent.AddIngredient -> {
                uiState = uiState.copy(ingredients = uiState.ingredients + RecipeIngredient(name = "", quantity = ""))
            }
            is RecipeEditorEvent.RemoveIngredient -> {
                uiState = uiState.copy(ingredients = uiState.ingredients.filterIndexed { i, _ -> i != event.index })
            }

            // --- Step Logic ---
            is RecipeEditorEvent.StepDescriptionChanged -> {
                val newList = uiState.steps.toMutableList()
                newList[event.index] = newList[event.index].copy(description = event.description)
                uiState = uiState.copy(steps = newList)
            }
            is RecipeEditorEvent.StepPrepTimeChanged -> {
                if (event.prepTime.all { it.isDigit() } || event.prepTime.isEmpty()) { // digits only
                    val newList = uiState.steps.toMutableList()
                    newList[event.index] = newList[event.index].copy(
                        prepTime = (event.prepTime.toIntOrNull() ?: 0).coerceAtLeast(0)
                    )
                    uiState = uiState.copy(steps = newList)
                }
            }
            RecipeEditorEvent.AddStep -> {
                val nextOrder = uiState.steps.size + 1
                uiState = uiState.copy(steps = uiState.steps + InstructionStep(stepNumber = nextOrder, description = "", prepTime = 0))
            }
            is RecipeEditorEvent.RemoveStep -> {
                val filtered = uiState.steps.filterIndexed { i, _ -> i != event.index }
                // Re-index steps so they remain 1, 2, 3...
                val reindexed = filtered.mapIndexed { i, step -> step.copy(stepNumber = i + 1) }
                uiState = uiState.copy(steps = reindexed)
            }

            RecipeEditorEvent.Save -> saveRecipe()
        }
        validateForm() // Run validation after every event
    }

    fun saveRecipe(onSaved: () -> Unit = {}) {
        if (!uiState.isValid || uiState.isSaving) return
        uiState = uiState.copy(isSaving = true)

        val computedPrepTime = uiState.steps.sumOf { it.prepTime.coerceAtLeast(0) }

        val recipe = Recipe(
            id = recipeId ?: 0L,
            title = uiState.title.trim(),
            description = uiState.description.trim(),
            category = uiState.category,
            prepTime = computedPrepTime,
            ingredients = uiState.ingredients,
            instructionSteps = uiState.steps,
        )

        viewModelScope.launch {
            if (uiState.isEditMode) {
                repository.updateRecipe(recipe)
            } else {
                repository.addRecipe(recipe)
            }

            uiState = uiState.copy(isSaving = false)
            onSaved()
        }
    }

    private fun validateForm() {
        val areIngredientsValid = uiState.ingredients.isNotEmpty() && uiState.ingredients.all { ingredient ->
            ingredient.name.isNotBlank() && ingredient.quantity.isNotBlank()
        }

        val areStepsValid = uiState.steps.isNotEmpty() && uiState.steps.all { step ->
            step.description.isNotBlank()
        }

        uiState = uiState.copy(
            isValid = uiState.title.isNotBlank()
                    && uiState.description.isNotBlank()
                    && areIngredientsValid
                    && areStepsValid
                    && uiState.category != Category.DEFAULT
        )
    }
}