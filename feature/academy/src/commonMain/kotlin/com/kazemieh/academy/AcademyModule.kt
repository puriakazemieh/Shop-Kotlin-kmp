package com.kazemieh.academy

import com.kazemieh.academy.cert.CertificateVerifyViewModel
import com.kazemieh.academy.cert.CertificatesViewModel
import com.kazemieh.academy.detail.CourseDetailViewModel
import com.kazemieh.academy.learn.CourseLearnViewModel
import com.kazemieh.academy.lessonquiz.LessonQuizViewModel
import com.kazemieh.academy.list.CourseListViewModel
import com.kazemieh.academy.peerreview.PeerReviewViewModel
import com.kazemieh.academy.placement.PlacementQuizViewModel
import com.kazemieh.academy.project.ProjectSubmissionViewModel
import com.kazemieh.academy.courserequest.CourseRequestViewModel
import com.kazemieh.academy.quiz.CourseQuizViewModel
import com.kazemieh.domain.courserequest.CreateCourseRequestUseCase
import com.kazemieh.domain.courserequest.GetCourseRequestsUseCase
import com.kazemieh.domain.courserequest.ToggleCourseRequestLikeUseCase
import com.kazemieh.domain.academy.AddPeerCommentUseCase
import com.kazemieh.domain.academy.CreateLessonQuestionUseCase
import com.kazemieh.domain.academy.EnrollCourseUseCase
import com.kazemieh.domain.academy.GetCertificatesUseCase
import com.kazemieh.domain.academy.GetCourseDetailUseCase
import com.kazemieh.domain.academy.GetCoursesUseCase
import com.kazemieh.domain.academy.GetLessonQuestionsUseCase
import com.kazemieh.domain.academy.GetLessonQuizUseCase
import com.kazemieh.domain.academy.GetMyCoursesUseCase
import com.kazemieh.domain.academy.GetMyProjectUseCase
import com.kazemieh.domain.academy.GetPeerCommentsUseCase
import com.kazemieh.domain.academy.GetPeerSubmissionsUseCase
import com.kazemieh.domain.academy.GetPlacementQuizUseCase
import com.kazemieh.domain.academy.GetMyRefundRequestsUseCase
import com.kazemieh.domain.academy.GetQuizUseCase
import com.kazemieh.domain.academy.JoinWaitlistUseCase
import com.kazemieh.domain.academy.MarkCourseUpdateSeenUseCase
import com.kazemieh.domain.academy.RequestCourseRefundUseCase
import com.kazemieh.domain.academy.SubmitLessonQuizUseCase
import com.kazemieh.domain.academy.SubmitPlacementQuizUseCase
import com.kazemieh.domain.academy.SubmitProjectByLinkUseCase
import com.kazemieh.domain.academy.SubmitProjectUseCase
import com.kazemieh.domain.academy.SubmitQuizUseCase
import com.kazemieh.domain.academy.UpdateLessonProgressUseCase
import com.kazemieh.domain.academy.VerifyCertificateUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val academyModule = module {
    factory { GetCoursesUseCase(get()) }
    factory { GetCourseDetailUseCase(get()) }
    factory { GetMyCoursesUseCase(get()) }
    factory { EnrollCourseUseCase(get()) }
    factory { UpdateLessonProgressUseCase(get()) }
    factory { GetQuizUseCase(get()) }
    factory { SubmitQuizUseCase(get()) }
    factory { GetCertificatesUseCase(get()) }
    factory { JoinWaitlistUseCase(get()) }
    factory { GetLessonQuizUseCase(get()) }
    factory { SubmitLessonQuizUseCase(get()) }
    factory { SubmitProjectUseCase(get()) }
    factory { SubmitProjectByLinkUseCase(get()) }
    factory { GetMyProjectUseCase(get()) }
    factory { GetLessonQuestionsUseCase(get()) }
    factory { CreateLessonQuestionUseCase(get()) }
    factory { MarkCourseUpdateSeenUseCase(get()) }
    factory { GetPeerSubmissionsUseCase(get()) }
    factory { GetPeerCommentsUseCase(get()) }
    factory { AddPeerCommentUseCase(get()) }
    factory { VerifyCertificateUseCase(get()) }
    factory { GetPlacementQuizUseCase(get()) }
    factory { SubmitPlacementQuizUseCase(get()) }
    factory { RequestCourseRefundUseCase(get()) }
    factory { GetMyRefundRequestsUseCase(get()) }
    factory { GetCourseRequestsUseCase(get()) }
    factory { CreateCourseRequestUseCase(get()) }
    factory { ToggleCourseRequestLikeUseCase(get()) }

    viewModel { CourseListViewModel(get(), get()) }
    viewModel { CourseDetailViewModel(get(), get(), get(), get()) }
    viewModel { CourseLearnViewModel(get(), get(), get(), get(), get()) }
    viewModel { CourseQuizViewModel(get(), get()) }
    viewModel { CertificatesViewModel(get()) }
    viewModel { LessonQuizViewModel(get(), get()) }
    viewModel { ProjectSubmissionViewModel(get(), get()) }
    viewModel { PeerReviewViewModel(get(), get(), get()) }
    viewModel { CertificateVerifyViewModel(get()) }
    viewModel { PlacementQuizViewModel(get(), get()) }
    viewModel { CourseRequestViewModel(get(), get(), get()) }
}
