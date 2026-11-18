package app.kaaj.android.ui.common

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import app.kaaj.android.data.net.ApiException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Base for the app's ViewModels. Screen state lives in Compose `mutableStateOf` properties on each
 * ViewModel; one-off snackbar messages go through [messages].
 */
abstract class KaajViewModel : ViewModel() {
    private val channel = Channel<UiMessage>(Channel.BUFFERED)
    val messages: Flow<UiMessage> = channel.receiveAsFlow()

    protected fun say(@StringRes id: Int) {
        channel.trySend(UiMessage.Res(id))
    }

    protected fun say(error: ApiException) {
        channel.trySend(UiMessage.Error(error))
    }
}
