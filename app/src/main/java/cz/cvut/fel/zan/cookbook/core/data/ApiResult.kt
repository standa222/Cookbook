package cz.cvut.fel.zan.cookbook.core.data

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(
        val exception: Throwable,
        val message: String? = null
    ) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}