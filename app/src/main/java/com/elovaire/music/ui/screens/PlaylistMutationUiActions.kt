package elovaire.music.droidbeauty.app.ui.screens

import elovaire.music.droidbeauty.app.data.settings.PlaylistMutationResult
import kotlinx.coroutines.Deferred

internal typealias PlaylistMutationRequest = Deferred<PlaylistMutationResult>
internal typealias PlaylistCreateAction = (String) -> PlaylistMutationRequest
internal typealias PlaylistAddAction = (Long) -> PlaylistMutationRequest
