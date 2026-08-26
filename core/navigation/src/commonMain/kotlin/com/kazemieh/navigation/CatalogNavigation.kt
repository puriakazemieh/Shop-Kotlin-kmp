package com.kazemieh.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.kazemieh.common.Screen
import com.kazemieh.blog.BlogListScreen
import com.kazemieh.blog.BlogDetailScreen
import com.kazemieh.details.DetailsScreen
import com.kazemieh.catalog.CategorySearchScreen
import com.kazemieh.catalog.bundle.BundleListScreen
import com.kazemieh.catalog.bundle.BundleDetailScreen
import com.kazemieh.catalog.assistant.ShoppingAssistantScreen
import com.kazemieh.comparison.ComparisonScreen

fun NavGraphBuilder.catalogNavGraph(navController: NavController) {
        composable<Screen.BundleList> {
            BundleListScreen(
                navigateBack = { navController.navigateBack() },
                navigateToBundle = { slug -> navController.navigate(Screen.BundleDetail(slug)) }
            )
        }

        composable<Screen.BundleDetail> {
            val args = it.toRoute<Screen.BundleDetail>()
            BundleDetailScreen(
                slug = args.slug,
                navigateBack = { navController.navigateBack() },
                navigateToProduct = { productSlug -> navController.navigate(Screen.ProductDetail(productSlug)) }
            )
        }

        composable<Screen.Comparison> {
            ComparisonScreen(
                navigateBack = { navController.navigateBack() },
                navigateToDetail = { slug -> navController.navigate(Screen.ProductDetail(slug)) }
            )
        }

        composable<Screen.ShoppingAssistant> {
            ShoppingAssistantScreen(
                onBackClick = { navController.navigateBack() },
                navigateToDetails = { slug -> navController.navigate(Screen.ProductDetail(slug)) }
            )
        }

        composable<Screen.BlogList> {
            BlogListScreen(
                navigateToDetail = { slug: String ->
                    navController.navigate(Screen.BlogDetail(slug))
                },
                navigateBack = { navController.navigateBack() }
            )
        }

        composable<Screen.BlogDetail> {
            val args = it.toRoute<Screen.BlogDetail>()
            BlogDetailScreen(
                slug = args.slug,
                navigateBack = { navController.navigateBack() },
                navigateToDetail = { newSlug: String ->
                    navController.navigate(Screen.BlogDetail(newSlug))
                }
            )
        }

        composable<Screen.ProductDetail> {
            val args = it.toRoute<Screen.ProductDetail>()
            DetailsScreen(
                slug = args.slug,
                navigateBack = { navController.navigateBack() },
                navigateToCart = {
                    navController.navigate(Screen.HomeGraph(showCart = true)) {
                        popUpTo<Screen.HomeGraph> { inclusive = true }
                    }
                },
                navigateToAuth = {
                    navController.navigate(Screen.AuthGraph)
                },
                navigateToDetails = { slug ->
                    navController.navigate(Screen.ProductDetail(slug = slug))
                }
            )
        }

        composable<Screen.CategorySearch> {
            val args = it.toRoute<Screen.CategorySearch>()
            CategorySearchScreen(
                categoryId = args.id,
                categoryName = args.name,
                navigateToDetails = { slug ->
                    navController.navigate(Screen.ProductDetail(slug = slug))
                },
                navigateBack = { navController.navigateBack() },
                navigateToAuth = {
                    navController.navigate(Screen.AuthGraph)
                }
            )
        }

}
