package com.kazemieh.clinic

import com.kazemieh.clinic.appointments.MyAppointmentsViewModel
import com.kazemieh.clinic.detail.TherapistDetailViewModel
import com.kazemieh.clinic.homework.HomeworkViewModel
import com.kazemieh.clinic.journal.JournalViewModel
import com.kazemieh.clinic.list.TherapistListViewModel
import com.kazemieh.clinic.match.TherapistMatchViewModel
import com.kazemieh.clinic.messaging.MessagingViewModel
import com.kazemieh.clinic.mood.MoodCheckInViewModel
import com.kazemieh.clinic.receipt.SessionReceiptViewModel
import com.kazemieh.domain.clinic.AddJournalEntryUseCase
import com.kazemieh.domain.clinic.BookAppointmentUseCase
import com.kazemieh.domain.clinic.CancelAppointmentUseCase
import com.kazemieh.domain.clinic.CompleteHomeworkUseCase
import com.kazemieh.domain.clinic.DeleteJournalEntryUseCase
import com.kazemieh.domain.clinic.GetClinicMessagesUseCase
import com.kazemieh.domain.clinic.GetMessagingStatusUseCase
import com.kazemieh.domain.clinic.GetMoodHistoryUseCase
import com.kazemieh.domain.clinic.GetMyAppointmentsUseCase
import com.kazemieh.domain.clinic.GetMyHomeworkUseCase
import com.kazemieh.domain.clinic.GetMyJournalUseCase
import com.kazemieh.domain.clinic.GetMySwitchRequestsUseCase
import com.kazemieh.domain.clinic.GetSessionReceiptUseCase
import com.kazemieh.domain.clinic.GetTherapistDetailUseCase
import com.kazemieh.domain.clinic.GetTherapistMatchQuestionsUseCase
import com.kazemieh.domain.clinic.GetTherapistsUseCase
import com.kazemieh.domain.clinic.RequestTherapistSwitchUseCase
import com.kazemieh.domain.clinic.SendClinicMessageUseCase
import com.kazemieh.domain.clinic.SubmitMoodCheckInUseCase
import com.kazemieh.domain.clinic.SubmitTherapistMatchUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val clinicModule = module {
    factory { GetTherapistsUseCase(get()) }
    factory { GetTherapistDetailUseCase(get()) }
    factory { GetMyAppointmentsUseCase(get()) }
    factory { BookAppointmentUseCase(get()) }
    factory { CancelAppointmentUseCase(get()) }
    factory { GetSessionReceiptUseCase(get()) }
    factory { SubmitMoodCheckInUseCase(get()) }
    factory { GetMoodHistoryUseCase(get()) }
    factory { RequestTherapistSwitchUseCase(get()) }
    factory { GetMySwitchRequestsUseCase(get()) }
    factory { GetClinicMessagesUseCase(get()) }
    factory { SendClinicMessageUseCase(get()) }
    factory { GetMessagingStatusUseCase(get()) }
    factory { GetMyHomeworkUseCase(get()) }
    factory { CompleteHomeworkUseCase(get()) }
    factory { GetMyJournalUseCase(get()) }
    factory { AddJournalEntryUseCase(get()) }
    factory { DeleteJournalEntryUseCase(get()) }
    factory { GetTherapistMatchQuestionsUseCase(get()) }
    factory { SubmitTherapistMatchUseCase(get()) }

    viewModel { TherapistListViewModel(get()) }
    viewModel { TherapistDetailViewModel(get(), get(), get()) }
    viewModel { MyAppointmentsViewModel(get(), get()) }
    viewModel { MoodCheckInViewModel(get(), get()) }
    viewModel { SessionReceiptViewModel(get()) }
    viewModel { MessagingViewModel(get(), get(), get()) }
    viewModel { HomeworkViewModel(get(), get()) }
    viewModel { JournalViewModel(get(), get(), get()) }
    viewModel { TherapistMatchViewModel(get(), get()) }
}
