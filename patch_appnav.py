import re

path = 'core/navigation/src/commonMain/kotlin/com/kazemieh/navigation/AppNavigation.kt'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Add imports
content = content.replace('import org.koin.compose.viewmodel.koinViewModel', 'import org.koin.compose.viewmodel.koinViewModel\nimport androidx.navigation.compose.currentBackStackEntryAsState\nimport androidx.compose.runtime.collectAsState\nimport androidx.compose.runtime.getValue\nimport org.koin.compose.koinInject\nimport com.kazemieh.config.capabilities.EffectiveFeatureStore\nimport androidx.compose.foundation.layout.Box\nimport androidx.compose.foundation.layout.fillMaxSize\nimport androidx.compose.foundation.layout.Column\nimport androidx.compose.material3.Text\nimport androidx.compose.material3.Button\nimport androidx.compose.ui.Alignment')

# 2. Add FeatureUnavailableScreen Composable at the end
unavailable_screen = """

@Composable
fun FeatureUnavailableScreen(featureId: String, onGoHome: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("قابلیت '${featureId}' در حال حاضر غیرفعال است.")
            Button(onClick = onGoHome) {
                Text("بازگشت به خانه")
            }
        }
    }
}
"""
if 'fun FeatureUnavailableScreen' not in content:
    content += unavailable_screen

# 3. Modify AppNavHost to include currentBackStackEntryAsState and LaunchedEffect
old_app_nav_host = """@Composable
fun AppNavHost(
    startDestination: Any = getInitialDestination() ?: Screen.HomeGraph(),
    routeGuard: FeatureRouteGuard? = null,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    BindBrowserHistory(navController)

    DisposableEffect(navController, routeGuard) {
        val listener = NavController.OnDestinationChangedListener { controller, destination, _ ->
            val route = destination.route ?: return@OnDestinationChangedListener
            if (routeGuard?.checkRoute(route) is RouteGuardDecision.Blocked) {
                controller.navigate(Screen.HomeGraph()) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose { navController.removeOnDestinationChangedListener(listener) }
    }"""

new_app_nav_host = """@Composable
fun AppNavHost(
    startDestination: Any = getInitialDestination() ?: Screen.HomeGraph(),
    routeGuard: FeatureRouteGuard? = null,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val store = koinInject<EffectiveFeatureStore>()
    val features by store.features.collectAsState()
    val currentEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route

    BindBrowserHistory(navController)

    LaunchedEffect(features, currentRoute) {
        if (currentRoute != null) {
            val decision = routeGuard?.checkRoute(currentRoute)
            if (decision is RouteGuardDecision.Blocked) {
                navController.navigate(Screen.FeatureUnavailable(decision.featureId)) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    DisposableEffect(navController, routeGuard) {
        val listener = NavController.OnDestinationChangedListener { controller, destination, _ ->
            val route = destination.route ?: return@OnDestinationChangedListener
            val decision = routeGuard?.checkRoute(route)
            if (decision is RouteGuardDecision.Blocked) {
                controller.navigate(Screen.FeatureUnavailable(decision.featureId)) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose { navController.removeOnDestinationChangedListener(listener) }
    }"""
content = content.replace(old_app_nav_host, new_app_nav_host)

# 4. Add composable for FeatureUnavailable
if 'composable<Screen.FeatureUnavailable>' not in content:
    content = content.replace('composable<Screen.HomeGraph> { backStackEntry ->\n            val screen = backStackEntry.toRoute<Screen.HomeGraph>()\n            MainGraphScreen(\n                showCart = screen.showCart,', 'composable<Screen.FeatureUnavailable> {\n            val featureId = it.toRoute<Screen.FeatureUnavailable>().featureId\n            FeatureUnavailableScreen(featureId) {\n                navController.navigate(Screen.HomeGraph()) {\n                    popUpTo(0) { inclusive = true }\n                    launchSingleTop = true\n                }\n            }\n        }\n\n        composable<Screen.HomeGraph> { backStackEntry ->\n            val screen = backStackEntry.toRoute<Screen.HomeGraph>()\n            MainGraphScreen(\n                showCart = screen.showCart,')

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
print('Done!')
