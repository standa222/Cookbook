package cz.cvut.fel.zan.cookbook.core.data.remote

import cz.cvut.fel.zan.cookbook.core.data.model.Category
import cz.cvut.fel.zan.cookbook.core.data.model.InstructionStep
import cz.cvut.fel.zan.cookbook.core.data.model.Recipe
import cz.cvut.fel.zan.cookbook.core.data.model.RecipeIngredient

private fun String?.toCategoryLoose(): Category {
    val raw = this?.trim().orEmpty()
    if (raw.isBlank()) return Category.DEFAULT

    return when (raw.lowercase()) {
        "dessert" -> Category.SWEET
        "beef", "chicken", "pork", "lamb", "goat", "seafood", "fish" -> Category.SAVORY
        else -> {
            val normalizedEnumName = raw
                .replace(" ", "")
                .replace("-", "")
                .uppercase()

            Category.entries.firstOrNull { it.name == normalizedEnumName } ?: Category.DEFAULT
        }
    }
}

private data class MealIngredientMeasure(
    val name: String,
    val measure: String,
)

private fun MealResponseDto.extractIngredients(): List<MealIngredientMeasure> {
    return (1..20).mapNotNull { index ->
        val ingredient = this::class.java
            .getDeclaredField("strIngredient$index")
            .apply { isAccessible = true }
            .get(this) as? String

        val measure = this::class.java
            .getDeclaredField("strMeasure$index")
            .apply { isAccessible = true }
            .get(this) as? String

        val name = ingredient?.trim().orEmpty()
        if (name.isBlank()) return@mapNotNull null

        MealIngredientMeasure(
            name = name,
            measure = measure?.trim().orEmpty(),
        )
    }
}

fun MealResponseDto.toRecipe(): Recipe {
    val ingredientMeasures = extractIngredients()

    val ingredients = ingredientMeasures.map { item ->
        RecipeIngredient(
            name = item.name,
            quantity = item.measure,
        )
    }

    val instructionLines = strInstructions
        .split("\n")
        .map { it.trim() }
        .filter { it.isNotBlank() }

    val steps = (instructionLines.ifEmpty { listOf(strInstructions.trim()) })
        .mapIndexed { index, line ->
            InstructionStep(
                stepNumber = index + 1,
                description = line,
                prepTime = 0,
                timerMinutes = null,
            )
        }

    val prepTimeMinutes = steps.sumOf { it.prepTime }

    return Recipe(
        id = idMeal.toLong(),
        title = strMeal.trim(),
        description = strMeal.trim(),
        category = strCategory.toCategoryLoose(),
        prepTime = prepTimeMinutes,
        ingredients = ingredients,
        instructionSteps = steps,
        isFavorite = false,
    )
}