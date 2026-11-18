@file:OptIn(ExperimentalLayoutApi::class)

package app.kaaj.android.ui.common

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.kaaj.android.R
import app.kaaj.android.data.DocumentFiles
import app.kaaj.android.data.PickedFile
import kotlinx.coroutines.launch

/**
 * Three ways to give a document: take a photo, choose a photo, or choose a PDF.
 * [onPicked] gets a file that already passed the type and size checks; [onProblem] gets a message.
 */
@Composable
fun DocumentPicker(
    enabled: Boolean,
    onPicked: (PickedFile) -> Unit,
    onProblem: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // The camera writes into this file; it must survive the camera app replacing our screen.
    var cameraTarget by rememberSaveable { mutableStateOf<String?>(null) }

    fun handle(uri: Uri?) {
        if (uri == null) return
        scope.launch {
            when (val result = DocumentFiles.read(context, uri)) {
                is DocumentFiles.Result.Ok -> onPicked(result.file)
                DocumentFiles.Result.WrongType -> onProblem(context.getString(R.string.upload_wrong_type))
                DocumentFiles.Result.TooLarge -> onProblem(context.getString(R.string.upload_too_large, DocumentFiles.MAX_MB.toString()))
                DocumentFiles.Result.Unreadable -> onProblem(context.getString(R.string.upload_read_failed))
            }
        }
    }

    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        val target = cameraTarget
        if (saved && target != null) handle(Uri.parse(target))
    }
    val photo = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { handle(it) }
    val pdf = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { handle(it) }

    FlowRow(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(enabled = enabled, onClick = {
            val uri = DocumentFiles.newCameraFile(context)
            cameraTarget = uri.toString()
            camera.launch(uri)
        }) {
            Icon(painterResource(R.drawable.ic_photo_camera), contentDescription = null)
            Text(stringResource(R.string.upload_camera))
        }
        OutlinedButton(enabled = enabled, onClick = {
            photo.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }) {
            Icon(painterResource(R.drawable.ic_image), contentDescription = null)
            Text(stringResource(R.string.upload_photo))
        }
        OutlinedButton(enabled = enabled, onClick = { pdf.launch(arrayOf("application/pdf")) }) {
            Icon(painterResource(R.drawable.ic_picture_as_pdf), contentDescription = null)
            Text(stringResource(R.string.upload_pdf))
        }
    }
}
