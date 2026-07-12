package com.kazemieh.comparison

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val comparisonModule = module {
    viewModel { ComparisonViewModel(get()) }
}
