package cz.cvut.fel.zan.cookbook.core.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kotlin.jvm.Throws

@Dao
interface IngredientDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(ingredients: List<IngredientEntity>): List<Long>

    @Query("SELECT * FROM ingredients WHERE name IN (:names)")
    suspend fun getByNames(names: List<String>): List<IngredientEntity>

    @Transaction
    @Query("SELECT * FROM ingredients WHERE name LIKE '%' || :query || '%'")
    fun searchByName(query: String): Flow<List<IngredientEntity>>
}