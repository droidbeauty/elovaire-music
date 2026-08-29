package elovaire.music.droidbeauty.app.ui.screens

internal data class RootNavigationPolicyState(
    val browsingOriginRoute: String = HOME_ROUTE,
    val selectedBottomRoute: String = HOME_ROUTE,
    val pendingTopLevelRoute: String? = null,
)

internal sealed interface RootNavigationEvent {
    data class TopLevelRequested(val route: String) : RootNavigationEvent
    data class DestinationObserved(val route: String?) : RootNavigationEvent
}

internal object RootNavigationReducer {
    fun reduce(
        state: RootNavigationPolicyState,
        event: RootNavigationEvent,
    ): RootNavigationPolicyState {
        return when (event) {
            is RootNavigationEvent.TopLevelRequested -> state.copy(
                browsingOriginRoute = event.route,
                selectedBottomRoute = event.route,
                pendingTopLevelRoute = event.route,
            )
            is RootNavigationEvent.DestinationObserved -> {
                val committedRoute = committedTopLevelRoute(
                    currentRoute = event.route,
                    pendingRoute = state.pendingTopLevelRoute,
                )
                committedRoute?.let {
                    state.copy(
                        browsingOriginRoute = it,
                        selectedBottomRoute = it,
                        pendingTopLevelRoute = null,
                    )
                } ?: state
            }
        }
    }
}

internal fun resolveActiveBottomRoute(
    pendingTopLevelRoute: String?,
    routeOwner: String?,
    currentRoute: String?,
    browsingOriginRoute: String?,
    selectedBottomRoute: String,
): String {
    return pendingTopLevelRoute
        ?: routeOwner
        ?: topLevelOwnerRoute(currentRoute, browsingOriginRoute)
        ?: selectedBottomRoute
}

internal fun committedTopLevelRoute(
    currentRoute: String?,
    pendingRoute: String?,
): String? {
    if (currentRoute !in TopLevelRoutes) return null
    if (pendingRoute != null && pendingRoute != currentRoute) return null
    return currentRoute
}
