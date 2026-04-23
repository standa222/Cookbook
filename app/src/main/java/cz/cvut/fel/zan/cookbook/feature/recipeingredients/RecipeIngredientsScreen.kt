package cz.cvut.fel.zan.cookbook.feature.recipeingredients

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.cvut.fel.zan.cookbook.core.data.model.RecipeIngredient
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeIngredientsScreen(
    onNavigateUp: () -> Unit,
    onNavigateToSteps: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecipeIngredientsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is RecipeIngredientsUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is RecipeIngredientsUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.message, color = MaterialTheme.colorScheme.error)
            }
        }

        is RecipeIngredientsUiState.Content -> {
            val recipe = state.recipe

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(recipe.title) },
                        navigationIcon = {
                            IconButton(onClick = onNavigateUp) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    )
                },
                modifier = modifier,
            ) { innerPadding ->
                IngredientsContent(
                    recipeDescription = state.recipe.description,
                    ingredients = state.recipe.ingredients,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

@Composable
private fun IngredientsContent(
    recipeDescription: String,
    ingredients: List<RecipeIngredient>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
//        Text(recipeTitle, style = MaterialTheme.typography.headlineSmall)
        Text(recipeDescription, style = MaterialTheme.typography.bodyMedium)
        Text("Ingredients", style = MaterialTheme.typography.titleMedium)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(ingredients, key = { it.id }) { ingredient ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(ingredient.name, modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(12.dp))
                    Text(ingredient.quantity, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
