package com.kazemieh.data.di

import com.kazemieh.data.admin.repository.AdminRepositoryImpl
import com.kazemieh.data.admin.source.AdminDataSource
import com.kazemieh.data.admin.source.AdminDataSourceImpl
import com.kazemieh.data.auth.datasource.AuthDataSource
import com.kazemieh.data.auth.datasource.AuthDataSourceImpl
import com.kazemieh.data.auth.repository.AuthRepositoryImpl
import com.kazemieh.data.catalog.repository.CatalogRepositoryImpl
import com.kazemieh.data.catalog.source.CatalogDataSource
import com.kazemieh.data.catalog.source.CatalogDataSourceImpl
import com.kazemieh.data.local.ProfileLocalDataSource
import com.kazemieh.data.local.TokenManager
import com.kazemieh.data.profile.repository.ProfileRepositoryImpl
import com.kazemieh.data.profile.source.ProfileDataSource
import com.kazemieh.data.profile.source.ProfileDataSourceImpl
import com.kazemieh.domain.repository.AdminRepository
import com.kazemieh.domain.repository.AuthRepository
import com.kazemieh.domain.repository.CatalogRepository
import com.kazemieh.domain.repository.ProfileRepository
import com.kazemieh.network.TokenProvider
import org.koin.dsl.module

val dataModule = module {

    single<AuthRepository> {
        AuthRepositoryImpl(
            authDataSource = get(),
            tokenManager = get(),
            profileLocalDataSource = get()
        )
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

    single<AdminRepository> {
        AdminRepositoryImpl(get())
    }

    single<AdminDataSource> {
        AdminDataSourceImpl(get())
    }

    single<CatalogRepository> {
        CatalogRepositoryImpl(get())
    }

    single<CatalogDataSource> {
        CatalogDataSourceImpl(get())
    }

    single { ProfileLocalDataSource(get()) }

    single { TokenManager(settings = get()) }
    single<TokenProvider> { get<TokenManager>() }

}
