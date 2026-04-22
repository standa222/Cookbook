package cz.cvut.fel.zan.cookbook.feature.recipeeditor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.cvut.fel.zan.cookbook.core.data.model.Category
import cz.cvut.fel.zan.cookbook.core.data.model.Ingredient
import cz.cvut.fel.zan.cookbook.core.data.model.InstructionStep
import cz.cvut.fel.zan.cookbook.core.data.model.RecipeIngredient
import cz.cvut.fel.zan.cookbook.core.data.model.label
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeEditorScreen(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecipeEditorViewModel = koinViewModel(),
) {
    val state = viewModel.uiState
    val suggestions by viewModel.ingredientSuggestions.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (state.isEditMode) "Edit Recipe" else "New Recipe")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(
                            onClick = { viewModel.saveRecipe { onNavigateUp() } },
                            enabled = state.isValid,
                        ) {
                            Icon(Icons.Default.Check, "Save")
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // --- Section: Basic Info ---
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.title,
                    onValueChange = { viewModel.onEvent(RecipeEditorEvent.TitleChanged(it)) },
                    label = { Text("Recipe Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = state.description,
                    onValueChange = { viewModel.onEvent(RecipeEditorEvent.DescriptionChanged(it)) },
                    label = { Text("Description *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category Selection (Simplified for brevity)
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = state.category.label(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            Category.entries.filter { it != Category.DEFAULT }.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.label()) },
                                    onClick = {
                                        viewModel.onEvent(RecipeEditorEvent.CategoryChanged(cat))
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = state.prepTime,
                        onValueChange = { viewModel.onEvent(RecipeEditorEvent.PrepTimeChanged(it)) },
                        label = { Text("Total Mins") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.6f),
                        singleLine = true
                    )
                }
            }

            HorizontalDivider()

            // --- Section: Ingredients ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ingredients", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = { viewModel.onEvent(RecipeEditorEvent.AddIngredient) }) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(4.dp))
                        Text("Add")
                    }
                }

                state.ingredients.forEachIndexed { index, ingredient ->
                    IngredientEditRow(
                        ingredient = ingredient,
                        suggestions = suggestions,
                        onNameChange = { viewModel.onEvent(RecipeEditorEvent.IngredientNameChanged(index, it)) },
                        onAmountChange = { viewModel.onEvent(RecipeEditorEvent.IngredientAmountChanged(index, it)) },
                        onSuggestionSelected = { viewModel.onEvent(RecipeEditorEvent.IngredientSelected(index, it)) },
                        onRemove = { viewModel.onEvent(RecipeEditorEvent.RemoveIngredient(index)) }
                    )
                }
            }

            HorizontalDivider()

            // --- Section: Instructions ---
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Instructions", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = { viewModel.onEvent(RecipeEditorEvent.AddStep) }) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(4.dp))
                        Text("Add Step")
                    }
                }

                state.steps.forEachIndexed { index, step ->
                    StepEditCard(
                        step = step,
                        index = index,
                        onDescriptionChange = { viewModel.onEvent(RecipeEditorEvent.StepDescriptionChanged(index, it)) },
                        onTimeChange = { viewModel.onEvent(RecipeEditorEvent.StepPrepTimeChanged(index, it)) },
                        onRemove = { viewModel.onEvent(RecipeEditorEvent.RemoveStep(index)) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientEditRow(
    ingredient: RecipeIngredient,
    suggestions: List<Ingredient>,
    onNameChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onSuggestionSelected: (Ingredient) -> Unit,
    onRemove: () -> Unit
) {
    var showSuggestions by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = ingredient.name,
                onValueChange = {
                    onNameChange(it)
                    showSuggestions = it.length >= 2
                },
                label = { Text("Ingredient") },
                singleLine = true
            )

            if (showSuggestions && suggestions.isNotEmpty()) {
                DropdownMenu(
                    expanded = true,
                    onDismissRequest = { showSuggestions = false },
                    properties = PopupProperties(focusable = false)
                ) {
                    suggestions.forEach { suggestion ->
                        DropdownMenuItem(
                            text = { Text(suggestion.name) },
                            onClick = {
                                onSuggestionSelected(suggestion)
                                showSuggestions = false
                            }
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = ingredient.quantity,
            onValueChange = onAmountChange,
            label = { Text("Qty") },
            modifier = Modifier.weight(0.5f),
            singleLine = true
        )

        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Delete, "Remove", tint = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun StepEditCard(
    step: InstructionStep,
    index: Int,
    onDescriptionChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Step ${index + 1}", style = MaterialTheme.typography.labelLarge)
                IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, "Remove Step")
                }
            }

            OutlinedTextField(
                value = step.description,
                onValueChange = onDescriptionChange,
                label = { Text("Describe the process...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            OutlinedTextField(
                value = if (step.prepTime == 0) "" else step.prepTime.toString(),
                onValueChange = onTimeChange,
                label = { Text("Step Time (mins)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.width(150.dp)
            )
        }
    }
}