package com.kazemieh.data.di

import com.kazemieh.network.common.*
import com.kazemieh.common.*
import com.kazemieh.domain.admin.AdminRepository
import com.kazemieh.domain.auth.AuthRepository
import com.kazemieh.domain.cart.CartRepository
import com.kazemieh.domain.catalog.CatalogRepository
import com.kazemieh.domain.favorite.FavoriteRepository
import com.kazemieh.domain.order.OrderRepository
import com.kazemieh.domain.payment.PaymentRepository
import com.kazemieh.domain.address.AddressRepository
import com.kazemieh.domain.profile.ProfileRepository
import com.kazemieh.domain.settings.SettingsRepository
import com.kazemieh.network.common.TokenProvider
import com.kazemieh.data.admin.repository.AdminRepositoryImpl
import com.kazemieh.data.admin.source.AdminDataSource
import com.kazemieh.data.admin.source.AdminDataSourceImpl
import com.kazemieh.data.auth.source.AuthDataSource
import com.kazemieh.data.auth.source.AuthDataSourceImpl
import com.kazemieh.data.auth.repository.AuthRepositoryImpl
import com.kazemieh.data.cart.repository.CartRepositoryImpl
import com.kazemieh.data.cart.source.CartDataSource
import com.kazemieh.data.cart.source.CartDataSourceImpl
import com.kazemieh.data.catalog.repository.CatalogRepositoryImpl
import com.kazemieh.data.catalog.source.CatalogDataSource
import com.kazemieh.data.catalog.source.CatalogDataSourceImpl
import com.kazemieh.data.favorite.repository.FavoriteRepositoryImpl
import com.kazemieh.data.favorite.source.FavoriteDataSource
import com.kazemieh.data.favorite.source.FavoriteDataSourceImpl
import com.kazemieh.data.local.ProfileLocalDataSource
import com.kazemieh.data.local.TokenManager
import com.kazemieh.data.order.repository.OrderRepositoryImpl
import com.kazemieh.data.order.source.OrderDataSource
import com.kazemieh.data.order.source.OrderDataSourceImpl
import com.kazemieh.data.payment.source.PaymentDataSource
import com.kazemieh.data.payment.source.PaymentDataSourceImpl
import com.kazemieh.data.payment.repository.PaymentRepositoryImpl
import com.kazemieh.data.profile.repository.AddressRepositoryImpl
import com.kazemieh.data.profile.repository.ProfileRepositoryImpl
import com.kazemieh.data.profile.source.AddressDataSource
import com.kazemieh.data.profile.source.AddressDataSourceImpl
import com.kazemieh.data.profile.source.ProfileDataSource
import com.kazemieh.data.profile.source.ProfileDataSourceImpl
import com.kazemieh.data.settings.SettingsRepositoryImpl
import org.koin.dsl.module


val dataModule = module {

    single<SettingsRepository> { SettingsRepositoryImpl(get()) }

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

    single<AddressRepository> {
        AddressRepositoryImpl(get())
    }

    single<AddressDataSource> {
        AddressDataSourceImpl(get())
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

    single<FavoriteRepository> {
        FavoriteRepositoryImpl(get())
    }

    single<FavoriteDataSource> {
        FavoriteDataSourceImpl(get())
    }

    single<CartRepository> {
        CartRepositoryImpl(get())
    }

    single<CartDataSource> {
        CartDataSourceImpl(get())
    }

    single<OrderRepository> {
        OrderRepositoryImpl(get())
    }

    single<OrderDataSource> {
        OrderDataSourceImpl(get())
    }

    single<PaymentRepository> {
        PaymentRepositoryImpl(get())
    }

    single<PaymentDataSource> {
        PaymentDataSourceImpl(get())
    }

    single { ProfileLocalDataSource(get()) }

    single { TokenManager(settings = get()) }
    single<TokenProvider> { get<TokenManager>() }

}
