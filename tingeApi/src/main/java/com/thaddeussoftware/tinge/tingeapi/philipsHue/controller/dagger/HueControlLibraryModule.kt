package com.thaddeussoftware.tinge.tingeapi.philipsHue.dagger

import com.thaddeussoftware.tinge.tingeapi.philipsHue.finder.retrofitInterfaces.CredentialsObtainerRetrofitInterface
import com.thaddeussoftware.tinge.tingeapi.philipsHue.controller.retrofitInterfaces.LightsRetrofitInterface
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by thaddeusreason on 14/01/2018.
 */
@Module
class HueControlLibraryModule constructor(private val baseUrl:String) {

    @Provides @BaseBridgeUrlScope
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    @Provides @BaseBridgeUrlScope
    fun provideLightsService(retrofit: Retrofit): LightsRetrofitInterface {
        return retrofit.create(LightsRetrofitInterface::class.java)
    }

    @Provides @BaseBridgeUrlScope
    fun provideConfigurationService(retrofit: Retrofit): CredentialsObtainerRetrofitInterface {
        return retrofit.create(CredentialsObtainerRetrofitInterface::class.java)
    }
}