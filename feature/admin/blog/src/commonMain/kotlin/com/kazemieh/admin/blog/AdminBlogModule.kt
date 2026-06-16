package com.kazemieh.admin.blog

import com.kazemieh.domain.blog.usecase.admin.CreateBlogUseCase
import com.kazemieh.domain.blog.usecase.admin.DeleteBlogUseCase
import com.kazemieh.domain.blog.usecase.admin.GetAdminBlogsUseCase
import com.kazemieh.domain.blog.usecase.admin.UpdateBlogUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val adminBlogModule = module {
    factory { GetAdminBlogsUseCase(get()) }
    factory { CreateBlogUseCase(get()) }
    factory { UpdateBlogUseCase(get()) }
    factory { DeleteBlogUseCase(get()) }

    viewModel { AdminBlogListViewModel(get(), get()) }
}
