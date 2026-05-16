package cz.cvut.fel.zan.cookbook.core.data.remote

import cz.cvut.fel.zan.cookbook.core.data.model.Recipe

class RecipeRemoteDataSource(
    private val apiService: RecipeApiService
) {
    suspend fun getRecipes(): List<Recipe> {
        val response = apiService.getRecipes()
        return response.flatMap { it.meals }.map { meal ->
            meal.toRecipe()
        }
    }
}