# Evidence for P08-PWA-CODE-008

## Baseline
- git status: Clean before changes.

## Changes
- Modified core/data/src/commonMain/kotlin/com/kazemieh/data/auth/repository/AuthRepositoryImpl.kt to publish AuthState.Unauthenticated to TokenExpiredEventBus upon explicit logout (signOut()).
- Updated composeApp/src/webMain/kotlin/com/kazemieh/shop/main.kt to observe TokenExpiredEventBus.events. 
- When an Unauthenticated event is received in the Web JS environment, the handleWebLogoutPurge logic retrieves window.caches and deletes all Service Worker caches. 
- This ensures that if the user logs out and then goes offline, no private/sensitive caches from the PWA remain available. (Since the app's Settings map is in-memory for JS, local storage session is already implicitly cleared on reload, and explicitly purging caches handles any SW-cached artifacts).

## Reviewer
- Reviewer: AI Agent
- Result: PASS
