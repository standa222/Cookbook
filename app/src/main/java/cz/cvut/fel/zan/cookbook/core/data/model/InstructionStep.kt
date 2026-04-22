package cz.cvut.fel.zan.cookbook.core.data.model

data class InstructionStep(
    val id: Long = 0,
    val stepNumber: Int,
    val description: String,
    val prepTime: Int, // in minutes
    val timerMinutes: Int? = null, // optional timer for this step
)
