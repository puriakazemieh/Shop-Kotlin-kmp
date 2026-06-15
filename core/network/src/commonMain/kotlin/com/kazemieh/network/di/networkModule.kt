package com.kazemieh.network.di

import com.kazemieh.network.address.AddressApi
import com.kazemieh.network.address.AddressApiImpl
import com.kazemieh.network.admin.AdminApi
import com.kazemieh.network.admin.AdminApiImpl
import com.kazemieh.network.auth.AuthApi
import com.kazemieh.network.auth.AuthApiImpl
import com.kazemieh.network.cart.CartApi
import com.kazemieh.network.cart.CartApiImpl
import com.kazemieh.network.catalog.CatalogApi
import com.kazemieh.network.catalog.CatalogApiImpl
import com.kazemieh.network.catalog.InteractionApi
import com.kazemieh.network.catalog.InteractionApiImpl
import com.kazemieh.network.common.HttpClientFactory
import com.kazemieh.network.favorite.FavoriteApi
import com.kazemieh.network.favorite.FavoriteApiImpl
import com.kazemieh.network.order.OrderApi
import com.kazemieh.network.order.OrderApiImpl
import com.kazemieh.network.payment.PaymentApi
import com.kazemieh.network.payment.PaymentApiImpl
import com.kazemieh.network.profile.ProfileApi
import com.kazemieh.network.profile.ProfileApiImpl
import com.kazemieh.network.wallet.WalletApi
import com.kazemieh.network.wallet.WalletApiImpl
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

    single<AdminApi> {
        AdminApiImpl(get())
    }

    single<CatalogApi> {
        CatalogApiImpl(get())
    }

    single<InteractionApi> {
        InteractionApiImpl(get())
    }

    single<FavoriteApi> {
        FavoriteApiImpl(get())
    }

    single<CartApi> {
        CartApiImpl(get())
    }

    single<OrderApi> {
        OrderApiImpl(get())
    }

    single<AddressApi> {
        AddressApiImpl(get())
    }

    single<PaymentApi> {
        PaymentApiImpl(get())
    }

    single<WalletApi> {
        WalletApiImpl(get())
    }
}
