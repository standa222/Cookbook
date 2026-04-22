package cz.cvut.fel.zan.cookbook.core.data.model

data class Ingredient(
    val id: Long,
    val name: String
)

data class IngredientUsageCount(
    val id: Long,
    val name: String,
    val recipeCount: Int,
)