@file:OptIn(ExperimentalLayoutApi::class)

package app.kaaj.android.ui.admin

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.kaaj.android.R
import app.kaaj.android.data.AdminRepository
import app.kaaj.android.data.DocumentFiles
import app.kaaj.android.data.model.AdminEmployer
import app.kaaj.android.data.model.Decision
import app.kaaj.android.data.model.VerificationStatus
import app.kaaj.android.data.net.ApiException
import app.kaaj.android.domain.Format
import app.kaaj.android.ui.common.ErrorState
import app.kaaj.android.ui.common.FormField
import app.kaaj.android.ui.common.InfoRow
import app.kaaj.android.ui.common.KaajTopBar
import app.kaaj.android.ui.common.KaajViewModel
import app.kaaj.android.ui.common.LoadingBox
import app.kaaj.android.ui.common.LocalAppContainer
import app.kaaj.android.ui.common.MessageEffect
import app.kaaj.android.ui.common.SectionCard
import app.kaaj.android.ui.common.VerificationPill
import app.kaaj.android.ui.common.isBangla
import app.kaaj.android.ui.common.label
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/** A document ready to show: a picture of it (page 1 for a PDF) and the bytes for "Open in another app". */
class DocumentPreview(val image: Bitmap?, val mimeType: String, val bytes: ByteArray, val name: String)

/** One employer's details and document, with Approve / Reject (wireframe A05). */
class AdminEmployerViewModel(
    private val admin: AdminRepository,
    private val appContext: Context,
    private val employerId: String,
) : KaajViewModel() {
    var employer by mutableStateOf<AdminEmployer?>(null)
        private set
    var error by mutableStateOf<ApiException?>(null)
        private set
    var preview by mutableStateOf<DocumentPreview?>(null)
        private set
    var previewFailed by mutableStateOf(false)
        private set
    var reason by mutableStateOf("")
    var reasonMissing by mutableStateOf(false)
        private set
    var deciding by mutableStateOf(false)
        private set

    fun load() {
        viewModelScope.launch {
            try {
                val loaded = admin.employer(employerId)
                employer = loaded
                error = null
                loaded.documentUrl?.let { loadDocument(it, loaded.documentName ?: "document") }
            } catch (e: ApiException) {
                error = e
            }
        }
    }

    private suspend fun loadDocument(url: String, name: String) {
        try {
            val file = admin.download(url)
            val image = withContext(Dispatchers.IO) { render(file.mimeType, file.bytes) }
            preview = DocumentPreview(image, file.mimeType, file.bytes, name)
            previewFailed = image == null
        } catch (e: ApiException) {
            previewFailed = true
        }
    }

    /** Images are decoded directly; for a PDF the first page is drawn with Android's PdfRenderer. */
    private fun render(mimeType: String, bytes: ByteArray): Bitmap? = try {
        if (mimeType.startsWith("application/pdf")) {
            val file = File(appContext.cacheDir, "preview.pdf").apply { writeBytes(bytes) }
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                PdfRenderer(descriptor).use { renderer ->
                    renderer.openPage(0).use { page ->
                        val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
                        bitmap.eraseColor(AndroidColor.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        bitmap
                    }
                }
            }
        } else {
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    } catch (e: Exception) {
        null
    }

    fun decide(decision: Decision) {
        if (decision == Decision.REJECT && reason.isBlank()) {
            reasonMissing = true
            return
        }
        deciding = true
        viewModelScope.launch {
            try {
                employer = admin.decide(employerId, decision, reason)
                reason = ""
                reasonMissing = false
                say(if (decision == Decision.APPROVE) R.string.admin_approved else R.string.admin_rejected)
            } catch (e: ApiException) {
                say(e)
            } finally {
                deciding = false
            }
        }
    }

    fun typeReason(text: String) {
        reason = text
        if (text.isNotBlank()) reasonMissing = false
    }
}

@Composable
fun AdminEmployerScreen(employerId: String, snackbar: SnackbarHostState, onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val vm: AdminEmployerViewModel = viewModel { AdminEmployerViewModel(container.admin, context.applicationContext, employerId) }
    MessageEffect(vm.messages, snackbar)
    LaunchedEffect(Unit) { if (vm.employer == null) vm.load() }

    Scaffold(topBar = { KaajTopBar(title = stringResource(R.string.admin_title), onBack = onBack) }) { padding ->
        val employer = vm.employer
        val error = vm.error
        when {
            error != null -> ErrorState(error, onRetry = vm::load, modifier = Modifier.padding(padding))
            employer == null -> LoadingBox(Modifier.padding(padding))
            else -> Column(
                Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                DetailsCard(employer)
                DocumentCard(employer, vm.preview, vm.previewFailed)
                // Only a business under review can be approved or rejected (VerificationStatus.canMoveTo).
                if (employer.status == VerificationStatus.PENDING) DecisionCard(vm)
            }
        }
    }
}

@Composable
private fun DetailsCard(employer: AdminEmployer) {
    val bangla = isBangla()
    SectionCard {
        Text(employer.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        VerificationPill(employer.status)
        InfoRow(stringResource(R.string.profile_kind), employer.kind.label())
        InfoRow(stringResource(R.string.admin_owner), employer.ownerEmail.orEmpty())
        InfoRow(stringResource(R.string.profile_address), employer.address.orEmpty())
        if (employer.latitude != null && employer.longitude != null) {
            InfoRow(stringResource(R.string.job_location), Format.coordinate(employer.latitude, bangla) + ", " + Format.coordinate(employer.longitude, bangla))
        }
        InfoRow(stringResource(R.string.profile_contact_person), employer.contactPerson.orEmpty())
        InfoRow(stringResource(R.string.profile_contact_phone), Format.localDigits(employer.contactPhone.orEmpty(), bangla))
        employer.contactEmail?.let { InfoRow(stringResource(R.string.profile_contact_email), it) }
        employer.submittedAt?.let { InfoRow(stringResource(R.string.verification_submitted_at), Format.date(it, bangla)) }
        employer.verifiedAt?.let { InfoRow(stringResource(R.string.verification_verified_at), Format.date(it, bangla)) }
        employer.verificationNote?.takeIf { it.isNotBlank() }?.let { InfoRow(stringResource(R.string.verification_note), it) }
    }
}

@Composable
private fun DocumentCard(employer: AdminEmployer, preview: DocumentPreview?, failed: Boolean) {
    val context = LocalContext.current
    SectionCard(title = stringResource(R.string.verification_document)) {
        if (employer.documentUrl == null) {
            Text(stringResource(R.string.admin_no_document), color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Text(listOfNotNull(employer.documentType?.label(), employer.documentName).joinToString(" · "))
            val image = preview?.image
            when {
                image != null -> Image(
                    bitmap = image.asImageBitmap(),
                    contentDescription = employer.documentName,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth().heightIn(max = 520.dp),
                )
                failed -> Text(stringResource(R.string.admin_document_failed), color = MaterialTheme.colorScheme.error)
                else -> LinearProgressIndicator(Modifier.fillMaxWidth())
            }
            if (preview != null) {
                OutlinedButton(onClick = { openElsewhere(context, preview) }) { Text(stringResource(R.string.admin_open_document)) }
            }
        }
    }
}

private fun openElsewhere(context: Context, preview: DocumentPreview) {
    val extension = if (preview.mimeType.startsWith("application/pdf")) ".pdf" else ".jpg"
    val uri = DocumentFiles.shareableCopy(context, preview.name.substringBeforeLast('.') + extension, preview.bytes)
    val intent = Intent(Intent.ACTION_VIEW)
        .setDataAndType(uri, preview.mimeType.substringBefore(';'))
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // No app on this phone can open it; the preview above is still there.
    }
}

@Composable
private fun DecisionCard(vm: AdminEmployerViewModel) {
    val quickReasons = listOf(
        stringResource(R.string.admin_quick_reason_1),
        stringResource(R.string.admin_quick_reason_2),
        stringResource(R.string.admin_quick_reason_3),
        stringResource(R.string.admin_quick_reason_4),
    )
    SectionCard(title = stringResource(R.string.admin_decision)) {
        FormField(
            value = vm.reason,
            onValueChange = vm::typeReason,
            label = stringResource(R.string.admin_reason_label),
            supporting = stringResource(R.string.admin_note),
            error = if (vm.reasonMissing) stringResource(R.string.admin_reason_required) else null,
            singleLine = false,
            minLines = 2,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            quickReasons.forEach { text -> AssistChip(onClick = { vm.typeReason(text) }, label = { Text(text) }) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { vm.decide(Decision.APPROVE) },
                enabled = !vm.deciding,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.weight(1f),
            ) { Text(stringResource(R.string.admin_approve)) }
            Button(
                onClick = { vm.decide(Decision.REJECT) },
                enabled = !vm.deciding,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.weight(1f),
            ) { Text(stringResource(R.string.admin_reject)) }
        }
    }
}
