package com.kazemieh.academy

import com.kazemieh.academy.detail.CourseDetailViewModel
import com.kazemieh.academy.learn.CourseLearnViewModel
import com.kazemieh.academy.list.CourseListViewModel
import com.kazemieh.domain.academy.EnrollCourseUseCase
import com.kazemieh.domain.academy.GetCourseDetailUseCase
import com.kazemieh.domain.academy.GetCoursesUseCase
import com.kazemieh.domain.academy.GetMyCoursesUseCase
import com.kazemieh.domain.academy.UpdateLessonProgressUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val academyModule = module {
    factory { GetCoursesUseCase(get()) }
    factory { GetCourseDetailUseCase(get()) }
    factory { GetMyCoursesUseCase(get()) }
    factory { EnrollCourseUseCase(get()) }
    factory { UpdateLessonProgressUseCase(get()) }

    viewModel { CourseListViewModel(get(), get()) }
    viewModel { CourseDetailViewModel(get(), get()) }
    viewModel { CourseLearnViewModel(get(), get()) }
}
