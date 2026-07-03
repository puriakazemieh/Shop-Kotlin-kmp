package com.kazemieh.clinic

import com.kazemieh.clinic.appointments.MyAppointmentsViewModel
import com.kazemieh.clinic.detail.TherapistDetailViewModel
import com.kazemieh.clinic.list.TherapistListViewModel
import com.kazemieh.domain.clinic.BookAppointmentUseCase
import com.kazemieh.domain.clinic.CancelAppointmentUseCase
import com.kazemieh.domain.clinic.GetMyAppointmentsUseCase
import com.kazemieh.domain.clinic.GetTherapistDetailUseCase
import com.kazemieh.domain.clinic.GetTherapistsUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val clinicModule = module {
    factory { GetTherapistsUseCase(get()) }
    factory { GetTherapistDetailUseCase(get()) }
    factory { GetMyAppointmentsUseCase(get()) }
    factory { BookAppointmentUseCase(get()) }
    factory { CancelAppointmentUseCase(get()) }

    viewModel { TherapistListViewModel(get()) }
    viewModel { TherapistDetailViewModel(get(), get()) }
    viewModel { MyAppointmentsViewModel(get(), get()) }
}
