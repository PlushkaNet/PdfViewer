package app.grapheneos.pdfviewer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.grapheneos.pdfviewer.recent.HomeScreen
import app.grapheneos.pdfviewer.ui.PdfViewerTheme
import app.grapheneos.pdfviewer.viewModel.PdfViewModel

class PdfViewer : ComponentActivity() {

    companion object {
        private const val TAG = "PdfViewer"
        const val PDF_MIME = "application/pdf"
    }

    val viewModel: PdfViewModel by viewModels()

    @VisibleForTesting
    internal var webView: WebView? = null

    @VisibleForTesting
    fun onJumpToPageInDocument(selectedPage: Int) {
        jumpToPage(viewModel, webView, selectedPage)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)

        val isViewIntent = intent.action == Intent.ACTION_VIEW
        var initialMimeError = false
        if (savedInstanceState == null && isViewIntent) {
            val type = intent.type
            if (type != null && type != PDF_MIME) {
                initialMimeError = true
            } else {
                if (type == null) {
                    Log.w(TAG, "MIME type is null, but we'll try to load it anyway")
                }
                viewModel.setUri(intent.data)
                viewModel.resetDocumentState()
            }
        }

        setContent {
            PdfViewerTheme {
                var showHome by rememberSaveable { mutableStateOf(!isViewIntent) }
                if (showHome) {
                    val recentFiles by (application as App)
                        .recentFilesRepository.files
                        .collectAsStateWithLifecycle()
                    HomeScreen(
                        recentFiles = recentFiles,
                        onOpenFile = { uri, lastPage ->
                            showHome = false
                            viewModel.setUri(uri)
                            viewModel.resetDocumentState()
                            if (lastPage > 1) {
                                viewModel.setPage(lastPage)
                            }
                            viewModel.setToolbarVisible(true)
                        },
                        onRemoveFile = { uri ->
                            (application as App).recentFilesRepository.remove(uri.toString())
                        }
                    )
                } else {
                    PdfViewerScreen(
                        viewModel = viewModel,
                        initialMimeError = initialMimeError,
                        onRequestRecreate = { recreate() },
                        onWebViewCreated = { webView = it },
                        onWebViewDestroyed = { webView = null },
                        onRequestBack = {
                            showHome = true
                            viewModel.setUri(null)
                            viewModel.resetDocumentState()
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.maybeCloseInputStream()
    }
}
