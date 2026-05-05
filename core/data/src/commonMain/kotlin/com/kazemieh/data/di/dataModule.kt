package com.kazemieh.data.di

import com.kazemieh.data.auth.datasource.AuthDataSource
import com.kazemieh.data.auth.datasource.AuthDataSourceImpl
import com.kazemieh.data.local.TokenManager
import com.kazemieh.data.auth.repository.AuthRepositoryImpl
import com.kazemieh.data.profile.repository.ProfileRepositoryImpl
import com.kazemieh.data.profile.source.ProfileDataSource
import com.kazemieh.data.profile.source.ProfileDataSourceImpl
import com.kazemieh.data.local.ProfileLocalDataSource
import com.kazemieh.domain.repository.AuthRepository
import com.kazemieh.domain.repository.ProfileRepository
import com.kazemieh.network.TokenProvider
import org.koin.dsl.module

val dataModule = module {
    single<AuthRepository> {
        AuthRepositoryImpl(get(), get(), get())
    }


    single<AuthDataSource> {
        AuthDataSourceImpl(get())
    }


    single<ProfileRepository> {
        ProfileRepositoryImpl(
            profileDataSource = get(),
            profileLocalDataSource = get()
        )
    }

    single<ProfileDataSource> {
        ProfileDataSourceImpl(get())
    }

    single { ProfileLocalDataSource(get()) }

    single<TokenProvider> { TokenManager(settings = get()) }

}