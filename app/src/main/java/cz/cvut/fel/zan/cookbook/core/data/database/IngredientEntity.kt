package cz.cvut.fel.zan.cookbook.core.data.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import cz.cvut.fel.zan.cookbook.core.data.model.RecipeIngredient

@Entity(tableName = "ingredients")
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

@Entity(
    tableName = "recipe_ingredient",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = IngredientEntity::class,
            parentColumns = ["id"],
            childColumns = ["ingredientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("recipeId"), Index("ingredientId")],
)
data class RecipeIngredientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recipeId: Long,
    val ingredientId: Long,
    val quantity: String
)

data class IngredientWithQuantity(
    @Embedded
    val ingredient: IngredientEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "ingredientId"
    )
    val link: RecipeIngredientEntity
)

fun IngredientWithQuantity.toRecipeIngredient() = RecipeIngredient(
    id = link.id,
    ingredientId = ingredient.id,
    name = ingredient.name,
    quantity = link.quantity
)