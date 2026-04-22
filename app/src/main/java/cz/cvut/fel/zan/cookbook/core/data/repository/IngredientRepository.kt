package cz.cvut.fel.zan.cookbook.core.data.repository

import cz.cvut.fel.zan.cookbook.core.data.model.Ingredient
import cz.cvut.fel.zan.cookbook.core.data.model.IngredientUsageCount
import kotlinx.coroutines.flow.Flow

interface IngredientRepository {
    fun searchIngredients(query: String): Flow<List<Ingredient>>

    fun searchIngredientsWithRecipeCount(query: String): Flow<List<IngredientUsageCount>>

    suspend fun deleteIngredientIfUnused(id: Long): Boolean
}
