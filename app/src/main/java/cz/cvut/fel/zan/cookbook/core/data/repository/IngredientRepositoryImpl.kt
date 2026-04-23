package cz.cvut.fel.zan.cookbook.core.data.repository

import cz.cvut.fel.zan.cookbook.core.data.database.IngredientDao
import cz.cvut.fel.zan.cookbook.core.data.database.IngredientUsageCountEntity
import cz.cvut.fel.zan.cookbook.core.data.database.toIngredient
import cz.cvut.fel.zan.cookbook.core.data.database.toIngredientUsageCount
import cz.cvut.fel.zan.cookbook.core.data.model.Ingredient
import cz.cvut.fel.zan.cookbook.core.data.model.IngredientUsageCount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class IngredientRepositoryImpl(
    private val ingredientDao: IngredientDao,
) : IngredientRepository {

    override fun searchIngredients(query: String): Flow<List<Ingredient>> {
        return ingredientDao.searchByName(query).map { list ->
            list.map { it.toIngredient() }
        }
    }

    override fun searchIngredientsWithRecipeCount(query: String): Flow<List<IngredientUsageCount>> {
        return ingredientDao.searchWithRecipeCount(query).map { list: List<IngredientUsageCountEntity> ->
            list.map { it.toIngredientUsageCount() }
        }
    }

    override suspend fun deleteIngredientIfUnused(id: Long): Boolean {
        val count = ingredientDao.getRecipeUsageCount(id)
        if (count > 0) {
            return false
        } else {
            val deleted = ingredientDao.deleteById(id)
            return deleted > 0
        }
    }
}
