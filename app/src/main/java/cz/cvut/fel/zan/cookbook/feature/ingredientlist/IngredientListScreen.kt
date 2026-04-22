package cz.cvut.fel.zan.cookbook.feature.ingredientlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.cvut.fel.zan.cookbook.R
import cz.cvut.fel.zan.cookbook.core.data.model.IngredientUsageCount
import org.koin.androidx.compose.koinViewModel

@Composable
fun IngredientListScreen(
    modifier: Modifier = Modifier,
    viewModel: IngredientListViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        IngredientListUiState.Loading -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is IngredientListUiState.Error -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.message, color = MaterialTheme.colorScheme.error)
            }
        }

        is IngredientListUiState.Content -> {
            IngredientListContent(
                uiState = state,
                onEvent = viewModel::onEvent,
                modifier = modifier,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngredientListContent(
    uiState: IngredientListUiState.Content,
    onEvent: (IngredientListEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            if (uiState.isSelectionMode) {
                SelectionTopBar(
                    selectedCount = uiState.selectedIds.size,
                    onClearSelection = { onEvent(IngredientListEvent.ClearSelection) },
                    onSelectAll = { onEvent(IngredientListEvent.SelectAll) },
                    onMergeSelected = { onEvent(IngredientListEvent.MergeSelected) },
                )
            } else {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { onEvent(IngredientListEvent.SearchQueryChanged(it)) },
                    label = { Text("Search Ingredients") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        },
        modifier = modifier,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(uiState.ingredients, key = { it.id }) { ingredient ->
                IngredientCard(
                    ingredient = ingredient,
                    isSelected = ingredient.id in uiState.selectedIds,
                    isSelectionMode = uiState.isSelectionMode,
                    onCardClick = {
                        if (uiState.isSelectionMode) onEvent(IngredientListEvent.ToggleSelection(ingredient.id))
                    },
                    onLongClick = { onEvent(IngredientListEvent.ToggleSelection(ingredient.id)) },
                    onDeleteClick = { onEvent(IngredientListEvent.DeleteIngredient(ingredient.id)) },
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun IngredientCard(
    ingredient: IngredientUsageCount,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onCardClick: () -> Unit,
    onLongClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = onCardClick,
                onLongClick = onLongClick,
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Checkbox(checked = isSelected, onCheckedChange = { onCardClick() })
                Spacer(Modifier.width(8.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(ingredient.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = recipeCountLabel(ingredient.recipeCount),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                )
            }

            if (!isSelectionMode && ingredient.recipeCount == 0) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Ingredient",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

private fun recipeCountLabel(count: Int): String {
    return when (count) {
        1 -> "Used in 1 recipe"
        else -> "Used in $count recipes"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionTopBar(
    selectedCount: Int,
    onClearSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onMergeSelected: () -> Unit,
) {
    TopAppBar(
        title = { Text("$selectedCount Selected") },
        navigationIcon = {
            IconButton(onClick = onClearSelection) { Icon(Icons.Default.Close, null) }
        },
        actions = {
            IconButton(onClick = onSelectAll) { Icon(painterResource(R.drawable.done_all_24px), "Select All") }
            IconButton(onClick = onMergeSelected) { Icon(Icons.AutoMirrored.Filled.ArrowForward, "Merge Selected") }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    )
}
