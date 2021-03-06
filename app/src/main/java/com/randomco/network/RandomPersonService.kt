package com.randomco.network

import android.support.annotation.IntRange
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import io.reactivex.Single
import mini.dagger.AppScope
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

data class HttpException(val response: Response) : IOException()

interface RandomPersonService {
    fun fetchPersons(@IntRange(from = 1) count: Int): Single<NetworkPersonList>
}

class RandomPersonServiceImpl @Inject constructor(private val client: OkHttpClient,
                                                  private val moshi: Moshi) : RandomPersonService {

    override fun fetchPersons(count: Int): Single<NetworkPersonList> {
        //This could be done with Retrofit or something similar, but for
        //a single request seems looks ok
        return Single.create { emitter ->
            val request = Request.Builder()
                .url(HttpUrl.Builder()
                    .scheme("https")
                    .host("api.randomuser.me")
                    .addQueryParameter("results", "$count")
                    .build())
                .build()
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val persons = moshi
                        .adapter(NetworkPersonList::class.java)
                        .fromJson(response.body()!!.source())!!
                    emitter.onSuccess(persons)
                } else {
                    emitter.onError(HttpException(response))
                }
            } catch (ex: Exception) {
                emitter.onError(ex)
            }
        }
    }
}


@Module
class NetworkModule {

    @Provides @AppScope
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .build()
    }

    @Provides @AppScope
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides @AppScope
    fun provideRandomService(impl: RandomPersonServiceImpl): RandomPersonService = impl
}