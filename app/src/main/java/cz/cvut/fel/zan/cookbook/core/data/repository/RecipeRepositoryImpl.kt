package cz.cvut.fel.zan.cookbook.core.data.repository

import cz.cvut.fel.zan.cookbook.core.data.database.RecipeDao
import cz.cvut.fel.zan.cookbook.core.data.model.Recipe
import kotlinx.coroutines.flow.Flow

class RecipeRepositoryImpl(
    private val recipeDao: RecipeDao,
) : RecipeRepository {
    override fun getRecipes(): Flow<List<Recipe>> {
        TODO("Not yet implemented")
    }

    override fun getRecipeById(id: Long): Flow<Recipe?> {
        TODO("Not yet implemented")
    }

    override suspend fun addRecipe(recipe: Recipe): Long {
        TODO("Not yet implemented")
    }

    override suspend fun updateRecipe(recipe: Recipe) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteRecipe(recipe: Recipe) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteRecipesByIds(ids: Set<Long>) {
        TODO("Not yet implemented")
    }

    override fun searchRecipes(query: String): Flow<List<Recipe>> {
        TODO("Not yet implemented")
    }

    override suspend fun toggleFavourite(id: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun setFavourite(ids: Set<Long>) {
        TODO("Not yet implemented")
    }
}