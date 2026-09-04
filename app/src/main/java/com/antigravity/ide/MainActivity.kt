package com.antigravity.ide

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.antigravity.ide.ui.screens.IdeMainScreen
import com.antigravity.ide.ui.theme.AntigravityTheme
import com.antigravity.ide.ui.theme.DeepSpace
import com.antigravity.ide.ui.viewmodel.IdeViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: IdeViewModel by viewModels()

    // Storage Access Framework Directory Picker Launcher
    private val openDocumentTreeLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { treeUri: Uri? ->
        treeUri?.let { uri ->
            try {
                // Persist folder access permissions across reboots
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (_: SecurityException) {
                // Fallback if already granted or vendor-specific SAF
            }
            viewModel.openWorkspaceTree(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AntigravityTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DeepSpace
                ) {
                    IdeMainScreen(
                        viewModel = viewModel,
                        onOpenFolderRequested = {
                            openDocumentTreeLauncher.launch(null)
                        }
                    )
                }
            }
        }
    }
}
