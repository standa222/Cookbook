package cz.cvut.fel.zan.cookbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cz.cvut.fel.zan.cookbook.core.navigation.IngredientListRoute
import cz.cvut.fel.zan.cookbook.core.navigation.RecipeListRoute
import cz.cvut.fel.zan.cookbook.core.ui.theme.CookbookTheme
import cz.cvut.fel.zan.cookbook.feature.recipelist.RecipeListScreen

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

    val bottomNavItems = listOf(
        Triple(RecipeListRoute, "Recipes", Icons.Default.Menu),
        Triple(IngredientListRoute, "Ingredients", Icons.Default.ShoppingCart),
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            val currentEntry by navController.currentBackStackEntryAsState()
            val currentRoute = currentEntry?.destination?.route

            NavigationBar {
                bottomNavItems.forEach { (route, label, icon) ->
                    val isSelected = currentRoute?.contains(route::class.simpleName ?: "") == true

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = RecipeListRoute,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable<RecipeListRoute> {
                RecipeListScreen(
//                    onNavigateToDetail = { id ->
//                        navController.navigate(RecipeDetailRoute(id))
//                    }
                )
            }
//            composable<IngredientListRoute> {
//                IngredientListScreen()
//            }
//
//            composable<RecipeDetailRoute> {
//                RecipeDetailScreen(
//                    onNavigateUp = { navController.navigateUp() }
//                )
//            }
        }
    }
}