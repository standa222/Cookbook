package cz.cvut.fel.zan.cookbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import cz.cvut.fel.zan.cookbook.core.navigation.IngredientListRoute
import cz.cvut.fel.zan.cookbook.core.navigation.RecipeEditorRoute
import cz.cvut.fel.zan.cookbook.core.navigation.RecipeIngredientsRoute
import cz.cvut.fel.zan.cookbook.core.navigation.RecipeListRoute
import cz.cvut.fel.zan.cookbook.core.navigation.RecipeStepsRoute
import cz.cvut.fel.zan.cookbook.core.ui.theme.CookbookTheme
import cz.cvut.fel.zan.cookbook.feature.ingredientlist.IngredientListScreen
import cz.cvut.fel.zan.cookbook.feature.recipeeditor.RecipeEditorScreen
import cz.cvut.fel.zan.cookbook.feature.recipelist.RecipeListScreen
import cz.cvut.fel.zan.cookbook.feature.recipeingredients.RecipeIngredientsScreen
import cz.cvut.fel.zan.cookbook.feature.recipesteps.RecipeStepsScreen

private data class BottomNavItem(
    val label: String,
    val iconRes: Int,
    val isSelected: Boolean,
    val onClick: () -> Unit,
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CookbookTheme {
                CookbookMain()
            }
        }
    }
}

@Composable
fun CookbookMain() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            val currentEntry by navController.currentBackStackEntryAsState()
            val destination = currentEntry?.destination

            val isRecipeDetail =
                destination?.hasRoute<RecipeIngredientsRoute>() == true ||
                    destination?.hasRoute<RecipeStepsRoute>() == true

            val recipeId: Long? = when {
                destination?.hasRoute<RecipeIngredientsRoute>() == true ->
                    currentEntry?.toRoute<RecipeIngredientsRoute>()?.recipeId

                destination?.hasRoute<RecipeStepsRoute>() == true ->
                    currentEntry?.toRoute<RecipeStepsRoute>()?.recipeId

                else -> null
            }

            val items: List<BottomNavItem> = if (isRecipeDetail && recipeId != null) {
                listOf(
                    BottomNavItem("Ingredients", R.drawable.grocery_24px, destination.hasRoute<RecipeIngredientsRoute>(),
                        onClick = {
                            navController.navigate(RecipeIngredientsRoute(recipeId)) {
                                popUpTo(RecipeListRoute) {
                                    saveState = true
                                }
                                restoreState = true
                                launchSingleTop = true
                            }
                        },
                    ),
                    BottomNavItem("Steps", R.drawable.footprint_24px, destination.hasRoute<RecipeStepsRoute>(),
                        onClick = {
                            navController.navigate(RecipeStepsRoute(recipeId)) {
                                popUpTo(RecipeListRoute) {
                                    saveState = true
                                }
                                restoreState = true
                                launchSingleTop = true
                            }
                        },
                    ),
                )
            } else {
                listOf(
                    BottomNavItem("Recipes", R.drawable.chef_hat_24px, destination?.hasRoute<RecipeListRoute>() == true,
                        onClick = {
                            navController.navigate(RecipeListRoute) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    ),
                    BottomNavItem("Ingredients", R.drawable.shopping_cart_24px, destination?.hasRoute<IngredientListRoute>() == true,
                        onClick = {
                            navController.navigate(IngredientListRoute) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    ),
                )
            }

            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = item.isSelected,
                        onClick = item.onClick,
                        icon = {
                            Icon(
                                painterResource(item.iconRes),
                                contentDescription = item.label,
                            )
                        },
                        label = { Text(item.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = RecipeListRoute,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable<RecipeListRoute> {
                RecipeListScreen(
                    onNavigateToDetail = { id -> navController.navigate(RecipeIngredientsRoute(id)) },
                    onNavigateToEditor = { id -> navController.navigate(RecipeEditorRoute(id)) },
                )
            }
            composable<RecipeEditorRoute> {
                RecipeEditorScreen(
                    onNavigateUp = { navController.navigateUp() }
                )
            }
            composable<IngredientListRoute> {
                IngredientListScreen()
            }
            composable<RecipeIngredientsRoute> {
                RecipeIngredientsScreen(
                    onNavigateUp = {
                        navController.popBackStack(RecipeListRoute, inclusive = false)
                    },
                    onNavigateToSteps = { id ->
                        navController.navigate(RecipeStepsRoute(id)) {
                            popUpTo(RecipeListRoute) {
                                saveState = true
                            }
                            restoreState = true
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable<RecipeStepsRoute> {
                RecipeStepsScreen(
                    onNavigateUp = {
                        navController.popBackStack(RecipeListRoute, inclusive = false)
                    },
                    onNavigateToIngredients = { id ->
                        navController.navigate(RecipeIngredientsRoute(id)) {
                            popUpTo(RecipeListRoute) {
                                saveState = true
                            }
                            restoreState = true
                            launchSingleTop = true
                        }
                    },
                )
            }
        }
    }
}