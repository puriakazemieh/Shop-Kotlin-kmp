package com.kazemieh.admin.blog

import com.kazemieh.domain.blog.usecase.GetBlogCategoriesUseCase
import com.kazemieh.domain.blog.usecase.admin.*
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val adminBlogModule = module {
    factory { GetAdminBlogsUseCase(get()) }
    factory { GetAdminBlogDetailUseCase(get()) }
    factory { CreateBlogUseCase(get()) }
    factory { UpdateBlogUseCase(get()) }
    factory { DeleteBlogUseCase(get()) }
    
    factory { CreateBlogCategoryUseCase(get()) }
    factory { UpdateBlogCategoryUseCase(get()) }
    factory { DeleteBlogCategoryUseCase(get()) }
    // GetBlogCategoriesUseCase is likely already in blogModule, but for Admin we might need it too if not shared
    // Checking if it's already defined in blogModule...

    viewModel { AdminBlogListViewModel(get(), get(), get(), get()) }
    viewModel { ManageBlogViewModel(get(), get(), get(), get()) }
}
