package com.kazemieh.admin.clinic

import com.kazemieh.domain.clinic.AddSlotUseCase
import com.kazemieh.domain.clinic.CompleteAppointmentUseCase
import com.kazemieh.domain.clinic.ConfirmAppointmentUseCase
import com.kazemieh.domain.clinic.CreateTherapistUseCase
import com.kazemieh.domain.clinic.DeleteTherapistUseCase
import com.kazemieh.domain.clinic.GenerateSlotsUseCase
import com.kazemieh.domain.clinic.GetAdminAppointmentsUseCase
import com.kazemieh.domain.clinic.GetAdminSlotsUseCase
import com.kazemieh.domain.clinic.GetAdminTherapistsUseCase
import com.kazemieh.domain.clinic.UpdateTherapistUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val adminClinicModule = module {
    factory { GetAdminTherapistsUseCase(get()) }
    factory { CreateTherapistUseCase(get()) }
    factory { UpdateTherapistUseCase(get()) }
    factory { DeleteTherapistUseCase(get()) }
    factory { AddSlotUseCase(get()) }
    factory { GenerateSlotsUseCase(get()) }
    factory { GetAdminSlotsUseCase(get()) }
    factory { GetAdminAppointmentsUseCase(get()) }
    factory { ConfirmAppointmentUseCase(get()) }
    factory { CompleteAppointmentUseCase(get()) }

    viewModel {
        AdminClinicViewModel(
            getAdminTherapistsUseCase = get(),
            createTherapistUseCase = get(),
            deleteTherapistUseCase = get(),
            addSlotUseCase = get(),
            getAdminSlotsUseCase = get(),
            getAdminAppointmentsUseCase = get(),
            confirmAppointmentUseCase = get(),
            completeAppointmentUseCase = get()
        )
    }
}
