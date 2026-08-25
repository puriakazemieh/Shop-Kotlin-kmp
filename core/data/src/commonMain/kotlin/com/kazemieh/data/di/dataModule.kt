package com.kazemieh.data.di

import com.kazemieh.network.common.*
import com.kazemieh.common.*
import com.kazemieh.domain.admin.AdminRepository
import com.kazemieh.domain.auth.AuthRepository
import com.kazemieh.domain.cart.CartRepository
import com.kazemieh.domain.catalog.CatalogRepository
import com.kazemieh.domain.catalog.InteractionRepository
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
import com.kazemieh.data.catalog.repository.InteractionRepositoryImpl
import com.kazemieh.data.catalog.source.CatalogDataSource
import com.kazemieh.data.catalog.source.CatalogDataSourceImpl
import com.kazemieh.data.favorite.repository.FavoriteRepositoryImpl
import com.kazemieh.data.favorite.source.FavoriteDataSource
import com.kazemieh.data.favorite.source.FavoriteDataSourceImpl
import com.kazemieh.data.local.ProfileLocalDataSource
import com.kazemieh.data.local.TokenManager
import com.kazemieh.data.local.createTokenSettings
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
import com.kazemieh.data.wallet.repository.WalletRepositoryImpl
import com.kazemieh.domain.wallet.WalletRepository
import com.kazemieh.domain.story.StoryRepository
import com.kazemieh.domain.blog.BlogRepository
import com.kazemieh.data.story.repository.StoryRepositoryImpl
import com.kazemieh.data.story.source.StoryDataSource
import com.kazemieh.data.story.source.StoryDataSourceImpl
import com.kazemieh.data.blog.repository.BlogRepositoryImpl
import com.kazemieh.data.blog.source.BlogDataSource
import com.kazemieh.data.blog.source.BlogDataSourceImpl
import com.kazemieh.domain.recentlyviewed.RecentlyViewedRepository
import com.kazemieh.data.recentlyviewed.RecentlyViewedRepositoryImpl
import com.kazemieh.data.recentlyviewed.source.RecentlyViewedDataSource
import com.kazemieh.data.recentlyviewed.source.RecentlyViewedDataSourceImpl
import com.kazemieh.domain.support.SupportRepository
import com.kazemieh.data.support.repository.SupportRepositoryImpl
import com.kazemieh.domain.academy.AcademyRepository
import com.kazemieh.data.academy.repository.AcademyRepositoryImpl
import com.kazemieh.domain.academy.AdminAcademyRepository
import com.kazemieh.data.academy.repository.AdminAcademyRepositoryImpl
import com.kazemieh.domain.psychtest.PsychTestRepository
import com.kazemieh.data.psychtest.repository.PsychTestRepositoryImpl
import com.kazemieh.domain.psychtest.AdminPsychTestRepository
import com.kazemieh.data.psychtest.repository.AdminPsychTestRepositoryImpl
import com.kazemieh.domain.clinic.ClinicRepository
import com.kazemieh.data.clinic.repository.ClinicRepositoryImpl
import com.kazemieh.domain.courserequest.CourseRequestRepository
import com.kazemieh.data.courserequest.repository.CourseRequestRepositoryImpl
import com.kazemieh.domain.clinic.AdminClinicRepository
import com.kazemieh.data.clinic.repository.AdminClinicRepositoryImpl
import com.kazemieh.domain.bundle.BundleRepository
import com.kazemieh.data.bundle.repository.BundleRepositoryImpl
import com.kazemieh.domain.bundle.AdminBundleRepository
import com.kazemieh.data.bundle.repository.AdminBundleRepositoryImpl
import org.koin.dsl.module


val dataModule = module {

    single<WalletRepository> { WalletRepositoryImpl(get(), get()) }

    single<StoryRepository> { StoryRepositoryImpl(get(), get()) }

    single<StoryDataSource> { StoryDataSourceImpl(get()) }

    single<BlogRepository> { BlogRepositoryImpl(get()) }
    single<BlogDataSource> { BlogDataSourceImpl(get()) }

    single<SettingsRepository> { SettingsRepositoryImpl(get()) }

    single<RecentlyViewedDataSource> { RecentlyViewedDataSourceImpl(get()) }
    single<RecentlyViewedRepository> { RecentlyViewedRepositoryImpl(get()) }

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
        CatalogRepositoryImpl(get(), get())
    }

    single<InteractionRepository> {
        InteractionRepositoryImpl(get())
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

    single<com.kazemieh.domain.order.ReturnRequestRepository> {
        com.kazemieh.data.order.repository.ReturnRequestRepositoryImpl(get())
    }

    single<com.kazemieh.domain.referral.ReferralRepository> {
        com.kazemieh.data.referral.ReferralRepositoryImpl(get())
    }

    single<com.kazemieh.domain.order.RecurringOrderRepository> {
        com.kazemieh.data.order.repository.RecurringOrderRepositoryImpl(get())
    }

    single<com.kazemieh.domain.membership.MembershipRepository> {
        com.kazemieh.data.membership.MembershipRepositoryImpl(get())
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

    single<SupportRepository> {
        SupportRepositoryImpl(get())
    }

    single<AcademyRepository> {
        AcademyRepositoryImpl(get())
    }

    single<ClinicRepository> {
        ClinicRepositoryImpl(get())
    }

    single<CourseRequestRepository> {
        CourseRequestRepositoryImpl(get())
    }

    single<AdminAcademyRepository> {
        AdminAcademyRepositoryImpl(get())
    }

    single<AdminClinicRepository> {
        AdminClinicRepositoryImpl(get())
    }

    single<PsychTestRepository> {
        PsychTestRepositoryImpl(get())
    }

    single<AdminPsychTestRepository> {
        AdminPsychTestRepositoryImpl(get())
    }

    single<BundleRepository> {
        BundleRepositoryImpl(get())
    }

    single<AdminBundleRepository> {
        AdminBundleRepositoryImpl(get())
    }

    single { TokenManager(settings = createTokenSettings()) }
    single<TokenProvider> { get<TokenManager>() }

}
