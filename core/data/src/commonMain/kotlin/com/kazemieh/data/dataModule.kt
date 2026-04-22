package com.kazemieh.data

import com.kazemieh.domain.AuthRepository
import org.koin.dsl.module

val dataModule = module {
    single<AuthRepository> {
        AuthRepositoryImpl(get())
    }


    single<RemoteDataSource> {
        RemoteDataSourceImpl(get())
    }
}