package com.kazemieh.admin.academy

import com.kazemieh.domain.academy.AddCourseLessonUseCase
import com.kazemieh.domain.academy.AddCourseSectionUseCase
import com.kazemieh.domain.academy.CreateCourseUseCase
import com.kazemieh.domain.academy.DeleteCourseUseCase
import com.kazemieh.domain.academy.GetAdminCourseDetailUseCase
import com.kazemieh.domain.academy.GetAdminCoursesUseCase
import com.kazemieh.domain.academy.GetAdminWaitlistUseCase
import com.kazemieh.domain.academy.NotifyNextInWaitlistUseCase
import com.kazemieh.domain.academy.UpdateCourseUseCase
import com.kazemieh.domain.academy.UpsertCourseQuizUseCase
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

    viewModel {
        AdminAcademyViewModel(
            getAdminCoursesUseCase = get(),
            getAdminCourseDetailUseCase = get(),
            createCourseUseCase = get(),
            deleteCourseUseCase = get(),
            addCourseSectionUseCase = get(),
            addCourseLessonUseCase = get(),
            upsertCourseQuizUseCase = get(),
            getAdminWaitlistUseCase = get(),
            notifyNextInWaitlistUseCase = get()
        )
    }
}
