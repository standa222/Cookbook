package cz.cvut.fel.zan.cookbook.core.data.model

data class RecipeIngredient(
    val id: Long,
    val ingredientId: Long,
    val name: String,
    val quantity: String
)
