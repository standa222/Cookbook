package cz.cvut.fel.zan.cookbook.core.data.database

import androidx.room.TypeConverter
import cz.cvut.fel.zan.cookbook.core.data.model.Category

class Converters {
    @TypeConverter
    fun fromCategory(value: Category) = value.name

    @TypeConverter
    fun toCategory(value: String) = Category.valueOf(value)
}