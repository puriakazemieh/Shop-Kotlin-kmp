package com.kazemieh.network.di

import com.kazemieh.network.AuthApi
import com.kazemieh.network.AuthApiImpl
import com.kazemieh.network.HttpClientFactory
import org.koin.dsl.module

val networkModule = module {

    // HttpClient
    single {
        HttpClientFactory.create()
    }

    single<AuthApi> {
        AuthApiImpl(get())
    }

}