package cz.cvut.fel.zan.cookbook.core.data.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import cz.cvut.fel.zan.cookbook.core.data.model.Category
import cz.cvut.fel.zan.cookbook.core.data.model.Recipe

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val category: Category,
//    val imageUrl: String,
    val prepTime: Int, // in minutes
    val isFavorite: Boolean = false,
)

data class FullRecipe(
    @Embedded
    val recipe: RecipeEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId"
    )
    val steps: List<InstructionStepEntity>,

    @Relation(
        entity = IngredientEntity::class,
        parentColumn = "id", // RecipeEntity.id
        entityColumn = "id", // IngredientEntity.id
        associateBy = Junction(
            value = RecipeIngredient::class,
            parentColumn = "recipeId",
            entityColumn = "ingredientId"
        )
    )
    val ingredients: List<IngredientWithQuantity>
)

fun FullRecipe.toRecipe() = Recipe(
    id = recipe.id,
    title = recipe.title,
    description = recipe.description,
    category = recipe.category,
    prepTime = recipe.prepTime,
    ingredients = ingredients.map { it.toIngredient() },
    instructionSteps = steps.sortedBy { it.stepNumber }.map { it.toInstructionStep() },
    isFavorite = recipe.isFavorite
)