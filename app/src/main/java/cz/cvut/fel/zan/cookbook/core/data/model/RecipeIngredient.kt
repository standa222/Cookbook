package cz.cvut.fel.zan.cookbook.core.data.model

data class RecipeIngredient(
    val id: Long = 0,
    val ingredientId: Long = 0,
    val name: String,
    val quantity: String
)
