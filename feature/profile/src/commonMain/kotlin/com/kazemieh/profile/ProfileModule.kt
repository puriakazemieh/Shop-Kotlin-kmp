package com.kazemieh.profile

import com.kazemieh.domain.profile.GetProfileUseCase
import com.kazemieh.domain.profile.ObserveProfileUseCase
import com.kazemieh.domain.profile.UpdateProfileUseCase
import com.kazemieh.domain.address.AddAddressUseCase
import com.kazemieh.domain.address.DeleteAddressUseCase
import com.kazemieh.domain.address.GetAddressesUseCase
import com.kazemieh.domain.address.SetDefaultAddressUseCase
import com.kazemieh.domain.address.UpdateAddressUseCase
import com.kazemieh.domain.profile.ValidateProfileUseCase
import com.kazemieh.domain.favorite.GetFavoritesUseCase
import com.kazemieh.domain.favorite.ToggleFavoriteUseCase
import com.kazemieh.domain.wallet.*
import com.kazemieh.domain.referral.GetMyReferralInfoUseCase
import com.kazemieh.domain.membership.GetMembershipStatusUseCase
import com.kazemieh.domain.membership.SubscribeMembershipUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val profileModule = module {

    // ViewModel
    viewModel {
        ProfileViewModel(
            getProfileUseCase = get(),
            updateProfileUseCase = get(),
            observeProfileUseCase = get(),
            validateProfileUseCase = get(),
            getAddressesUseCase = get(),
            addAddressUseCase = get(),
            updateAddressUseCase = get(),
            deleteAddressUseCase = get(),
            setDefaultAddressUseCase = get(),
            getWalletBalanceUseCase = get(),
            getFavoritesUseCase = get(),
            toggleFavoriteUseCase = get(),
            observeFavoriteIdsUseCase = get(),
            getMyOrdersUseCase = get(),
            signOutUseCase = get()
        )
    }

    viewModel {
        com.kazemieh.profile.club.CustomerClubViewModel(
            getMyOrdersUseCase = get()
        )
    }

    viewModel {
        WalletViewModel(
            getWalletBalanceUseCase = get(),
            getWalletTransactionsUseCase = get(),
            topUpWalletUseCase = get(),
            withdrawWalletUseCase = get()
        )
    }

    viewModel { ReferralViewModel(getMyReferralInfoUseCase = get()) }
    factory { GetMyReferralInfoUseCase(get()) }

    viewModel { MembershipViewModel(getMembershipStatusUseCase = get(), subscribeMembershipUseCase = get()) }
    factory { GetMembershipStatusUseCase(get()) }
    factory { SubscribeMembershipUseCase(get()) }

    // UseCases
    factory { GetProfileUseCase(get()) }
    factory { UpdateProfileUseCase(get()) }
    factory { ObserveProfileUseCase(get()) }

    factory { GetAddressesUseCase(get()) }
    factory { AddAddressUseCase(get()) }
    factory { UpdateAddressUseCase(get()) }
    factory { DeleteAddressUseCase(get()) }
    factory { SetDefaultAddressUseCase(get()) }

    factory { GetFavoritesUseCase(get()) }
    factory { ToggleFavoriteUseCase(get()) }

    // Validation
    factory { ValidateProfileUseCase() }

    // Wallet
    factory { GetWalletBalanceUseCase(get()) }
    factory { GetWalletTransactionsUseCase(get()) }
    factory { TopUpWalletUseCase(get()) }
    factory { WithdrawWalletUseCase(get()) }
    factory { AdminSearchWalletUsersUseCase(get()) }
    factory { AdminAdjustWalletUseCase(get()) }
    factory { AdminListWithdrawalsUseCase(get()) }
    factory { AdminProcessWithdrawalUseCase(get()) }
}
