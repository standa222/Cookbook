package cz.cvut.fel.zan.cookbook.core.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.http.ContentType.Application.Json
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private val IDS = listOf("52772", "52988", "53151", "52905")

class RecipeApiService {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(Logging) {
            level = LogLevel.INFO
        }
    }

    suspend fun getRecipes(): List<RecipeCollectionDto> {
        return IDS.map { id ->
            client.get("$BASE_URL/lookup.php?i=$id").body()
        }
    }

    companion object {
        private const val BASE_URL = "https://www.themealdb.com/api/json/v1/1"
    }
}