package com.kazemieh.network.di

import com.kazemieh.network.ApiClient
import com.kazemieh.network.HttpClientFactory
import org.koin.dsl.module

val networkModule = module {

    // HttpClient
    single {
        HttpClientFactory.create()
    }

    // ApiClient
    single {
        ApiClient(
            httpClient = get(),
            baseUrl = "https://api.yourshop.com"
        )
    }

}