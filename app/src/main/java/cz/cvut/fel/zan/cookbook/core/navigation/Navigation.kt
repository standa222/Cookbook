package cz.cvut.fel.zan.cookbook.core.navigation

import kotlinx.serialization.Serializable

@Serializable object RecipeListRoute

@Serializable object IngredientListRoute

@Serializable data class RecipeIngredientsRoute(val recipeId: Long)

@Serializable data class RecipeEditorRoute(val recipeId: Long? = null)

@Serializable data class RecipeStepsRoute(val recipeId: Long)