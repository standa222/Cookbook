package cz.cvut.fel.zan.cookbook.core.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Transaction
    @Query("SELECT * FROM recipes")
    fun getFullRecipes(): Flow<List<FullRecipe>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    fun getFullRecipeById(id: Long): Flow<FullRecipe?>

    @Transaction
    @Query("SELECT * FROM recipes WHERE title LIKE '%' || :query || '%'")
    fun searchRecipes(query: String): Flow<List<FullRecipe>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(steps: List<InstructionStepEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeIngredients(links: List<RecipeIngredientEntity>)

    @Delete
    suspend fun deleteRecipeEntity(recipe: RecipeEntity)

    @Query("DELETE FROM recipes WHERE id IN (:ids)")
    suspend fun deleteRecipesByIds(ids: List<Long>)

    @Query("DELETE FROM instruction_steps WHERE recipeId = :recipeId")
    suspend fun deleteStepsByRecipeId(recipeId: Long)

    @Query("DELETE FROM recipe_ingredient WHERE recipeId = :recipeId")
    suspend fun deleteLinksByRecipeId(recipeId: Long)

    @Query("UPDATE recipes SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFavourite(id: Long)

    @Query("UPDATE recipes SET isFavorite = 1 WHERE id IN (:ids)")
    suspend fun setFavourite(ids: List<Long>)
}