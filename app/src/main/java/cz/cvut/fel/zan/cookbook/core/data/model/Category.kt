package cz.cvut.fel.zan.cookbook.core.data.model

enum class Category {
    SWEET, SAVORY, BAKING, SOUP, SALAD, DRINK, OTHER
}

fun Category.label(): String = when (this) {
    Category.SWEET -> "Sweet"
    Category.SAVORY -> "Savory"
    Category.BAKING -> "Baking"
    Category.SOUP -> "Soup"
    Category.SALAD -> "Salad"
    Category.DRINK -> "Drink"
    Category.OTHER -> "Other"
}