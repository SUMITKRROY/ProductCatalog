package com.example.productcatalog

import android.app.Application
import androidx.room.Room
import com.example.productcatalog.data.local.AppDatabase
import com.example.productcatalog.data.remote.DummyJsonApi
import com.example.productcatalog.data.repository.CartRepository
import com.example.productcatalog.data.repository.ProductRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Manual dependency container – small enough that Hilt would be overkill. */
class AppContainer(app: Application) {

    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    private val api: DummyJsonApi = Retrofit.Builder()
        .baseUrl("https://dummyjson.com/")
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(DummyJsonApi::class.java)

    private val db: AppDatabase = Room.databaseBuilder(app, AppDatabase::class.java, "catalog.db").build()

    val productRepository = ProductRepository(api)
    val cartRepository = CartRepository(db.cartDao())
}

class CatalogApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
