package com.kazemieh.blog

import com.kazemieh.domain.blog.usecase.*
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val blogModule = module {
    factory { GetBlogsUseCase(get()) }
    factory { GetFeaturedBlogsUseCase(get()) }
    factory { GetBlogDetailUseCase(get()) }
    factory { GetRelatedBlogsUseCase(get()) }
    factory { GetBlogCategoriesUseCase(get()) }

    viewModel { BlogListViewModel(get(), get(), get()) }
    viewModel { BlogDetailViewModel(get(), get()) }
}
