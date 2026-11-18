package app.kaaj.android.ui.jobs

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import app.kaaj.android.R
import app.kaaj.android.data.JobRepository
import app.kaaj.android.data.model.AllowedAction
import app.kaaj.android.data.model.Job
import app.kaaj.android.data.model.JobAction
import app.kaaj.android.ui.common.label

/** The message after an action succeeds ("Post paused."). */
fun AllowedAction.doneMessage(): Int = when (this) {
    AllowedAction.SUBMIT -> R.string.job_done_submit
    AllowedAction.PAUSE -> R.string.job_done_pause
    AllowedAction.RESUME -> R.string.job_done_resume
    AllowedAction.CLOSE -> R.string.job_done_close
    AllowedAction.REPOST -> R.string.job_done_repost
    AllowedAction.DELETE -> R.string.job_done_delete
    AllowedAction.EDIT -> R.string.job_updated
}

/** Closing and deleting can't be undone, so they ask first. */
fun AllowedAction.needsConfirmation(): Boolean = this == AllowedAction.CLOSE || this == AllowedAction.DELETE

/**
 * Runs one of the server's job actions. EDIT is handled by the screen (it opens the form).
 * Returns the updated post, the new draft for REPOST, or null for DELETE.
 */
suspend fun JobRepository.perform(job: Job, action: AllowedAction): Job? = when (action) {
    AllowedAction.DELETE -> {
        deleteDraft(job.id)
        null
    }
    AllowedAction.SUBMIT -> act(job.id, JobAction.SUBMIT)
    AllowedAction.PAUSE -> act(job.id, JobAction.PAUSE)
    AllowedAction.RESUME -> act(job.id, JobAction.RESUME)
    AllowedAction.CLOSE -> act(job.id, JobAction.CLOSE)
    AllowedAction.REPOST -> act(job.id, JobAction.REPOST)
    AllowedAction.EDIT -> job
}

/** Asks "Close this post?" / "Delete this draft?" before doing it. */
@Composable
fun ConfirmActionDialog(action: AllowedAction, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.common_confirm)) },
        text = {
            Text(stringResource(if (action == AllowedAction.DELETE) R.string.job_confirm_delete else R.string.job_confirm_close))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.common_yes)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } },
    )
}

/** The "⋮" menu on a job card: every action the server allows for this post right now. */
@Composable
fun JobActionsMenu(job: Job, enabled: Boolean, onAction: (AllowedAction) -> Unit) {
    var open by remember { mutableStateOf(false) }
    var confirming by remember { mutableStateOf<AllowedAction?>(null) }
    Box {
        IconButton(onClick = { open = true }, enabled = enabled && job.allowedActions.isNotEmpty()) {
            Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.common_more))
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            job.allowedActions.forEach { action ->
                DropdownMenuItem(
                    text = { Text(action.label()) },
                    onClick = {
                        open = false
                        if (action.needsConfirmation()) confirming = action else onAction(action)
                    },
                )
            }
        }
    }
    confirming?.let { action ->
        ConfirmActionDialog(
            action = action,
            onConfirm = {
                confirming = null
                onAction(action)
            },
            onDismiss = { confirming = null },
        )
    }
}
