package cz.cvut.fel.zan.cookbook.core.data.repository

import cz.cvut.fel.zan.cookbook.core.data.database.IngredientDao
import cz.cvut.fel.zan.cookbook.core.data.database.IngredientEntity
import cz.cvut.fel.zan.cookbook.core.data.database.RecipeDao
import cz.cvut.fel.zan.cookbook.core.data.database.RecipeIngredientEntity
import cz.cvut.fel.zan.cookbook.core.data.database.toEntity
import cz.cvut.fel.zan.cookbook.core.data.database.toIngredient
import cz.cvut.fel.zan.cookbook.core.data.database.toRecipe
import cz.cvut.fel.zan.cookbook.core.data.model.Ingredient
import cz.cvut.fel.zan.cookbook.core.data.model.Recipe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RecipeRepositoryImpl(
    private val recipeDao: RecipeDao,
    private val ingredientDao: IngredientDao,
) : RecipeRepository {

    override fun getRecipes(): Flow<List<Recipe>> {
        return recipeDao.getFullRecipes().map { list ->
            list.map { it.toRecipe() }
        }
    }

    override fun getRecipeById(id: Long): Flow<Recipe?> {
        return recipeDao.getFullRecipeById(id).map { it?.toRecipe() }
    }

    override suspend fun addRecipe(recipe: Recipe): Long {
        val recipeId = recipeDao.insertRecipe(recipe.toEntity())

        recipeDao.deleteStepsByRecipeId(recipeId)
        recipeDao.deleteLinksByRecipeId(recipeId)

        val steps = recipe.instructionSteps.map { it.toEntity(recipeId) }
        recipeDao.insertSteps(steps)

        val names = recipe.ingredients.map { it.name }
        val existingIngredients = ingredientDao.getByNames(names)
        val existingNamesMap = existingIngredients.associateBy { it.name }

        val newIngredients = recipe.ingredients
            .filter { it.name !in existingNamesMap }
            .map { IngredientEntity(name = it.name) }

        if (newIngredients.isNotEmpty()) {
            ingredientDao.insertAll(newIngredients)
        }

        val finalIdMap = ingredientDao.getByNames(names).associateBy { it.name }

        val links = recipe.ingredients.map { input ->
            val ingredientId = finalIdMap[input.name]?.id
                ?: throw IllegalStateException("Failed to resolve ID for ingredient: ${input.name}")

            RecipeIngredientEntity(
                recipeId = recipeId,
                ingredientId = ingredientId,
                quantity = input.quantity
            )
        }

        recipeDao.insertRecipeIngredients(links)

        return recipeId
    }

    override suspend fun updateRecipe(recipe: Recipe) {
        addRecipe(recipe)
    }

    override suspend fun deleteRecipe(recipe: Recipe) {
        recipeDao.deleteRecipeEntity(recipe.toEntity())
    }

    override suspend fun deleteRecipesByIds(ids: Set<Long>) {
        recipeDao.deleteRecipesByIds(ids.toList())
    }

    override fun searchRecipes(query: String): Flow<List<Recipe>> {
        return recipeDao.searchRecipes(query).map { list ->
            list.map { it.toRecipe() }
        }
    }

    override suspend fun toggleFavourite(id: Long) {
        recipeDao.toggleFavourite(id)
    }

    override suspend fun setFavourite(ids: Set<Long>) {
        recipeDao.setFavourite(ids.toList())
    }

    override fun searchIngredients(query: String): Flow<List<Ingredient>> {
        return ingredientDao.searchByName(query).map { list ->
            list.map {it.toIngredient() }
        }
    }
}