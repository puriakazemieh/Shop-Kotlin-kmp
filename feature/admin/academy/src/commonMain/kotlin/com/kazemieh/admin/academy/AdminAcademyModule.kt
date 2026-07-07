package com.kazemieh.admin.academy

import com.kazemieh.domain.academy.AddCourseLessonUseCase
import com.kazemieh.domain.academy.AddCourseSectionUseCase
import com.kazemieh.domain.academy.AddLessonFileByLinkUseCase
import com.kazemieh.domain.academy.AddLessonFileUseCase
import com.kazemieh.domain.academy.UploadCourseMediaUseCase
import com.kazemieh.domain.academy.CreateCourseUseCase
import com.kazemieh.domain.academy.DeleteCourseUseCase
import com.kazemieh.domain.academy.DeleteLessonFileUseCase
import com.kazemieh.domain.academy.GetAdminCourseDetailUseCase
import com.kazemieh.domain.academy.GetAdminCoursesUseCase
import com.kazemieh.domain.academy.GetAdminWaitlistUseCase
import com.kazemieh.domain.academy.ListProjectSubmissionsUseCase
import com.kazemieh.domain.academy.NotifyNextInWaitlistUseCase
import com.kazemieh.domain.academy.ReviewProjectSubmissionUseCase
import com.kazemieh.domain.academy.UpdateCourseUseCase
import com.kazemieh.domain.academy.UpsertCourseQuizUseCase
import com.kazemieh.domain.academy.UpsertLessonQuizUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val adminAcademyModule = module {
    factory { GetAdminCoursesUseCase(get()) }
    factory { GetAdminCourseDetailUseCase(get()) }
    factory { CreateCourseUseCase(get()) }
    factory { UpdateCourseUseCase(get()) }
    factory { DeleteCourseUseCase(get()) }
    factory { AddCourseSectionUseCase(get()) }
    factory { AddCourseLessonUseCase(get()) }
    factory { UpsertCourseQuizUseCase(get()) }
    factory { GetAdminWaitlistUseCase(get()) }
    factory { NotifyNextInWaitlistUseCase(get()) }
    factory { AddLessonFileByLinkUseCase(get()) }
    factory { AddLessonFileUseCase(get()) }
    factory { UploadCourseMediaUseCase(get()) }
    factory { DeleteLessonFileUseCase(get()) }
    factory { UpsertLessonQuizUseCase(get()) }
    factory { ListProjectSubmissionsUseCase(get()) }
    factory { ReviewProjectSubmissionUseCase(get()) }

    viewModel {
        AdminAcademyViewModel(
            getAdminCoursesUseCase = get(),
            getAdminCourseDetailUseCase = get(),
            createCourseUseCase = get(),
            updateCourseUseCase = get(),
            deleteCourseUseCase = get(),
            addCourseSectionUseCase = get(),
            addCourseLessonUseCase = get(),
            upsertCourseQuizUseCase = get(),
            getAdminWaitlistUseCase = get(),
            notifyNextInWaitlistUseCase = get(),
            addLessonFileByLinkUseCase = get(),
            addLessonFileUseCase = get(),
            uploadCourseMediaUseCase = get(),
            deleteLessonFileUseCase = get(),
            upsertLessonQuizUseCase = get(),
            listProjectSubmissionsUseCase = get(),
            reviewProjectSubmissionUseCase = get()
        )
    }
}
