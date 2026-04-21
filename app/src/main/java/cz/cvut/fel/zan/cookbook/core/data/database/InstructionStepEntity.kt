package cz.cvut.fel.zan.cookbook.core.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import cz.cvut.fel.zan.cookbook.core.data.model.InstructionStep

@Entity(
    tableName = "instruction_steps",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("recipeId")],
)
data class InstructionStepEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recipeId: Long,
    val stepNumber: Int,
    val description: String,
    val prepTime: Int,
    val timerMinutes: Int?
)

fun InstructionStepEntity.toInstructionStep() = InstructionStep(
    id = id,
    stepNumber = stepNumber,
    description = description,
    prepTime = prepTime,
    timerMinutes = timerMinutes
)

fun InstructionStep.toEntity(recipeId: Long) = InstructionStepEntity(
    id = id,
    recipeId = recipeId,
    stepNumber = stepNumber,
    description = description,
    prepTime = prepTime,
    timerMinutes = timerMinutes
)
