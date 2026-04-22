package cz.cvut.fel.zan.cookbook.core.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(ingredients: List<IngredientEntity>): List<Long>

    @Query("SELECT * FROM ingredients WHERE name IN (:names)")
    suspend fun getByNames(names: List<String>): List<IngredientEntity>

    @Transaction
    @Query("SELECT * FROM ingredients WHERE name LIKE '%' || :query || '%'")
    fun searchByName(query: String): Flow<List<IngredientEntity>>

    @Query(
        """
        SELECT i.id AS ingredientId,
               i.name AS name,
               COUNT(DISTINCT ri.recipeId) AS recipeCount
        FROM ingredients i
        LEFT JOIN recipe_ingredient ri ON ri.ingredientId = i.id
        WHERE i.name LIKE '%' || :query || '%'
        GROUP BY i.id
        ORDER BY i.name COLLATE NOCASE
        """
    )
    fun searchWithRecipeCount(query: String): Flow<List<IngredientUsageCountEntity>>
}