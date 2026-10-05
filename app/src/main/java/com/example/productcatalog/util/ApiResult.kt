package com.example.productcatalog.util

import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException

/** Like runCatching, but never swallows coroutine cancellation. */
suspend inline fun <T> safeApiCall(crossinline block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(e)
}

fun Throwable.toUserMessage(): String = when (this) {
    is UnknownHostException -> "No internet connection. Please check your network and try again."
    is SocketTimeoutException -> "The request timed out. Please try again."
    is HttpException -> "Server error (${code()}). Please try again later."
    is IOException -> "Network problem. Please check your connection."
    else -> "Something went wrong. Please try again."
}

fun Double.asPrice(): String = "₹" + String.format(java.util.Locale.ENGLISH, "%.2f", this)
