package com.kazemieh.blog

import com.kazemieh.domain.blog.usecase.GetBlogDetailUseCase
import com.kazemieh.domain.blog.usecase.GetBlogsUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val blogModule = module {
    factory { GetBlogsUseCase(get()) }
    factory { GetBlogDetailUseCase(get()) }

    viewModel { BlogListViewModel(get()) }
    viewModel { BlogDetailViewModel(get()) }
}
