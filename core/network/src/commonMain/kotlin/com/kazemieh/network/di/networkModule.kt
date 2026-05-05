package com.kazemieh.network.di

import com.kazemieh.network.AuthApi
import com.kazemieh.network.AuthApiImpl
import com.kazemieh.network.HttpClientFactory
import com.kazemieh.network.ProfileApi
import com.kazemieh.network.ProfileApiImpl
import com.kazemieh.network.TokenProvider
import org.koin.dsl.module

val networkModule = module {

    // HttpClient
    single {
        HttpClientFactory.create(get())
    }

    single<AuthApi> {
        AuthApiImpl(get())
    }

    single<ProfileApi> {
        ProfileApiImpl(get())
    }
}