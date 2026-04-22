package cz.cvut.fel.zan.cookbook.core.data.repository

import cz.cvut.fel.zan.cookbook.core.data.model.Ingredient
import cz.cvut.fel.zan.cookbook.core.data.model.IngredientUsageCount
import cz.cvut.fel.zan.cookbook.core.data.model.Recipe
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun getRecipes(): Flow<List<Recipe>>
    fun getRecipeById(id: Long): Flow<Recipe?>

    suspend fun addRecipe(recipe: Recipe): Long
    suspend fun updateRecipe(recipe: Recipe)

    suspend fun deleteRecipe(recipe: Recipe)
    suspend fun deleteRecipesByIds(ids: Set<Long>)

    fun searchRecipes(query: String): Flow<List<Recipe>>
    suspend fun toggleFavourite(id: Long)
    suspend fun setFavourite(ids: Set<Long>)

    fun searchIngredients(query: String): Flow<List<Ingredient>>

    fun searchIngredientsWithRecipeCount(query: String): Flow<List<IngredientUsageCount>>
}