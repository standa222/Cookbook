package cz.cvut.fel.zan.cookbook.feature.recipelist

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.cvut.fel.zan.cookbook.R
import cz.cvut.fel.zan.cookbook.core.data.model.Category
import cz.cvut.fel.zan.cookbook.core.data.model.Recipe
import org.koin.androidx.compose.koinViewModel

@Composable
fun RecipeListScreen(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToEditor: (Long?) -> Unit,
    viewModel: RecipeListViewModel = koinViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val refreshError by viewModel.refreshError.collectAsStateWithLifecycle()
    val pullToRefreshState = rememberPullToRefreshState()

    when (val state = uiState) {
        is RecipeListUiState.Loading -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is RecipeListUiState.Error -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.message, color = MaterialTheme.colorScheme.error)
            }
        }

        is RecipeListUiState.Content -> {
            RecipeListContent(
                uiState = state,
                onEvent = viewModel::onEvent,
                onNavigateToDetail = onNavigateToDetail,
                onNavigateToEditor = onNavigateToEditor,
                isRefreshing = isRefreshing,
                refreshError = refreshError,
                pullToRefreshState = pullToRefreshState,
                onRefresh = { viewModel.refreshRecipes() },
                modifier = modifier,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeListContent(
    uiState: RecipeListUiState.Content,
    onEvent: (RecipeListEvent) -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToEditor: (Long?) -> Unit,
    isRefreshing: Boolean,
    refreshError: String?,
    pullToRefreshState: PullToRefreshState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = listOf("All", "Favourites")


    Box(
        modifier = Modifier.pullToRefresh(
            isRefreshing = isRefreshing,
            state = pullToRefreshState,
            onRefresh = onRefresh,
        )
    ) {
        Scaffold(
            floatingActionButton = {
                if (!uiState.isSelectionMode) {
                    FloatingActionButton(onClick = { onNavigateToEditor(null) }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Recipe")
                    }
                }
            },
            topBar = {
                if (uiState.isSelectionMode) {
                    SelectionTopBar(
                        selectedCount = uiState.selectedIds.size,
                        onClearSelection = { onEvent(RecipeListEvent.ClearSelection) },
                        onSelectAll = { onEvent(RecipeListEvent.SelectAll) },
                        onDeleteSelected = { onEvent(RecipeListEvent.DeleteSelected) },
                        onFavouriteSelected = { onEvent(RecipeListEvent.FavouriteSelected) }
                    )
                } else {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { onEvent(RecipeListEvent.SearchQueryChanged(it)) },
                        label = { Text("Search Recipes") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // All / Favourites Toggle
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    tabs.forEachIndexed { index, label ->
                        SegmentedButton(
                            selected = (index == 1 && uiState.showFavouritesOnly) || (index == 0 && !uiState.showFavouritesOnly),
                            onClick = { onEvent(RecipeListEvent.ToggleFavouritesFilter(index == 1)) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = tabs.size
                            ),
                            label = { Text(label) }
                        )
                    }
                }

                // Category Filters
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    items(Category.entries) { category ->
                        FilterChip(
                            selected = category in uiState.selectedCategories,
                            onClick = { onEvent(RecipeListEvent.ToggleCategoryFilter(category)) },
                            label = { Text(category.name) },
                            leadingIcon = if (category in uiState.selectedCategories) {
                                { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                            } else null
                        )
                    }
                }

                // List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(uiState.recipes, key = { it.id }) { recipe ->
                        RecipeCard(
                            recipe = recipe,
                            isSelected = recipe.id in uiState.selectedIds,
                            isSelectionMode = uiState.isSelectionMode,
                            onCardClick = {
                                if (uiState.isSelectionMode) onEvent(
                                    RecipeListEvent.ToggleSelection(
                                        recipe.id
                                    )
                                )
                                else onNavigateToDetail(recipe.id)
                            },
                            onLongClick = { onEvent(RecipeListEvent.ToggleSelection(recipe.id)) },
                            onFavouriteClick = { onEvent(RecipeListEvent.ToggleFavourite(recipe.id)) }
                        )
                    }
                }
            }
        }
        PullToRefreshDefaults.Indicator(
            state = pullToRefreshState,
            isRefreshing = isRefreshing,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
    refreshError?.let { error ->
        LaunchedEffect(error) {
            // show snackbar or Toast
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun RecipeCard(
    recipe: Recipe,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onCardClick: () -> Unit,
    onLongClick: () -> Unit,
    onFavouriteClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(onClick = onCardClick, onLongClick = onLongClick),
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
                Text(recipe.title, style = MaterialTheme.typography.titleMedium)
                Text(countPrepTime(recipe.prepTime), style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }

            IconButton(onClick = onFavouriteClick) {
                Icon(
                    imageVector = if (recipe.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = if (recipe.isFavorite) MaterialTheme.colorScheme.primary else LocalContentColor.current
                )
            }
        }
    }
}

private fun countPrepTime(prepTime: Int): String {
    val hours = prepTime / 60
    val minutes = prepTime % 60
    return if (hours > 0) "Prep time: $hours hours $minutes minutes" else "Prep time: $minutes minutes"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionTopBar(
    selectedCount: Int,
    onClearSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onDeleteSelected: () -> Unit,
    onFavouriteSelected: () -> Unit
) {
    TopAppBar(
        title = { Text("$selectedCount Selected") },
        navigationIcon = {
            IconButton(onClick = onClearSelection) { Icon(Icons.Default.Close, null) }
        },
        actions = {
            IconButton(onClick = onSelectAll) { Icon(painterResource(R.drawable.done_all_24px), "Select All") }
            IconButton(onClick = onFavouriteSelected) { Icon(Icons.Default.Favorite, "Fav Selected") }
            IconButton(onClick = onDeleteSelected) { Icon(Icons.Default.Delete, "Delete Selected") }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    )
}