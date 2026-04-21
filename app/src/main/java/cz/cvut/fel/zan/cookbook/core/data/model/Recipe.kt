package cz.cvut.fel.zan.cookbook.core.data.model

data class Recipe(
    val id: Long,
    val title: String,
    val description: String,
    val category: Category,
    val imageUrl: String,
    val prepTime: Int, // in minutes
    val ingredients: List<Ingredient> = emptyList(),
    val instructions: List<InstructionStep> = emptyList(),
    val isFavorite: Boolean = false,
)