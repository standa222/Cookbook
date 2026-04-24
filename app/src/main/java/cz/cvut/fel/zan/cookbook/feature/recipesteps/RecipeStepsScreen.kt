package cz.cvut.fel.zan.cookbook.feature.recipesteps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import cz.cvut.fel.zan.cookbook.core.data.model.InstructionStep
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeStepsScreen(
    onNavigateUp: () -> Unit,
    onNavigateToIngredients: (Long) -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecipeStepsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        RecipeStepsUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is RecipeStepsUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.message, color = MaterialTheme.colorScheme.error)
            }
        }

        is RecipeStepsUiState.Content -> {
            val recipe = state.recipe

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(recipe.title) },
                        navigationIcon = {
                            IconButton(onClick = onNavigateUp) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {
                            IconButton(onClick = { onNavigateToEdit(recipe.id) }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit recipe")
                            }
                            IconButton(
                                onClick = {
                                    viewModel.onEvent(RecipeStepsEvent.DeleteRecipe)
                                    onNavigateUp()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete recipe",
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            }
                        },
                    )
                },
                modifier = modifier,
            ) { innerPadding ->
                StepsContent(
                    recipeDescription = recipe.description,
                    steps = recipe.instructionSteps,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }

}

@Composable
private fun StepsContent(
    recipeDescription: String,
    steps: List<InstructionStep>,
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
        Text("Steps", style = MaterialTheme.typography.titleMedium)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(steps, key = { it.stepNumber }) { step ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Step ${step.stepNumber}", style = MaterialTheme.typography.labelLarge)
                        Text(step.description, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "Time: ${step.prepTime} mins",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}
