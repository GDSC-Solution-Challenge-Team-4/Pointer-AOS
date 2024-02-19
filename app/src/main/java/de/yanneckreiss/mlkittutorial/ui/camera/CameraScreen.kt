package de.yanneckreiss.mlkittutorial.ui.camera

import android.Manifest
import android.content.Context
import android.graphics.Color
import android.speech.tts.TextToSpeech
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.camera.core.AspectRatio
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Scaffold
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.Color.Companion.Yellow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.modifier.modifierLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Button
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import de.yanneckreiss.cameraxtutorial.R
import androidx.lifecycle.viewmodel.compose.viewModel
import de.yanneckreiss.mlkittutorial.translate.TranslateViewModel
import de.yanneckreiss.mlkittutorial.ui.DialogViewModel
import kotlinx.coroutines.delay

@Composable
fun CameraScreen() {
    CameraContent()
}

@Composable
private fun CameraContent(
    viewModel: TranslateViewModel = viewModel(),
    dialogViewModel: DialogViewModel = viewModel()
) {
    val state by viewModel.state
    val context: Context = LocalContext.current
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val cameraController: LifecycleCameraController =
        remember { LifecycleCameraController(context) }
    var detectedText: String by remember { mutableStateOf("No text detected yet..") }
    var showedText : String by remember { mutableStateOf("No text detected yet..") }
    // Text to speech related variables
    var textToSpeechInitialized by remember { mutableStateOf(false) }
    var textToSpeech: TextToSpeech? by remember { mutableStateOf(null) }

    var zoomValue by remember { mutableStateOf(1f) }


    fun onTextUpdated(updatedText: String) {
        detectedText = updatedText
    }

    fun initializeTextToSpeech() {
        if (!textToSpeechInitialized) {
            textToSpeech = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeechInitialized = true
                }
            }
        }
    }

    var showMessage by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = if (showMessage) 1f else 0f,
        animationSpec = if (showMessage) {
            tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        } else {
            keyframes {
                durationMillis = 1000
                1.0f at 0 // fade out 완료
                0.0f at 1 using FastOutSlowInEasing // fade out 시작

            }
        }, label = ""
    )


    LaunchedEffect(Unit) {
        showMessage = true
        delay(2000)
        showMessage = false
    }

    if (showMessage) {
        Box(
            modifier = Modifier
                .alpha(alpha)
                .graphicsLayer(alpha = alpha)
                .fillMaxSize()
                .padding(top = 50.dp),
            contentAlignment = Alignment.TopCenter // 세로 정렬을 맨 위로 설정
        ) {
            Box(
                modifier = Modifier
                    .background(
                        androidx.compose.ui.graphics.Color.Gray,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(top = 5.dp, start = 10.dp, end = 10.dp, bottom = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Text(
                    text = "Text Screen",
                    color = White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        //topBar = { TopAppBar() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(White),
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize(),
                factory = { context ->
                    PreviewView(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setBackgroundColor(Color.BLACK)
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FILL_START
                    }.also { previewView ->
                        startTextRecognition(
                            context = context,
                            cameraController = cameraController,
                            lifecycleOwner = lifecycleOwner,
                            previewView = previewView,
                            onDetectedTextUpdated = ::onTextUpdated
                        )
                    }
                }
            )

        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Yellow, shape = RoundedCornerShape(8.dp))
                .padding(10.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = detectedText,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
    if (dialogViewModel.isShortDialogShown) {
        AlertDialog(onDismissRequest = {
            dialogViewModel.onDismissShortDialog()
        }, confirmButton = {
           Button(onClick = {
               dialogViewModel.onDismissShortDialog()
               dialogViewModel.fullDialogOn()
           }) {
               Text(text = "확대")
           }
        }, dismissButton = {
            Button(onClick = {
                dialogViewModel.onDismissShortDialog()
            }) {
                Text(text = "나가기")
            }
        }, title = {
            Text(text = "감지된 문자")
        }, text = {
            Text(
                text = showedText,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }

        )

    }
    if (dialogViewModel.isFullDialogShown) {
        AlertDialog(onDismissRequest = {
            dialogViewModel.onDismissFullDialog()
        }, confirmButton = {
            Button(onClick = {
                dialogViewModel.onDismissFullDialog()
                dialogViewModel.shortDialogOn()
            }) {
                Text(text = "원래대로")
            }
        }, dismissButton = {
            Button(onClick = {
                dialogViewModel.onDismissFullDialog()
            }) {
                Text(text = "나가기")
            }
        }, title = {
            Text(text = "감지된 문자")
        }, text = {
            Text(
                text = showedText,
                Modifier.verticalScroll(rememberScrollState())
            )
        }

        )
    }
}


// Initialize Text-to-Speech when the composable is first composed
//    LaunchedEffect(Unit) {
//        initializeTextToSpeech()
//    }
//
//    // Dispose of Text-to-Speech when the composable is removed
//    DisposableEffect(Unit) {
//        onDispose {
//            textToSpeech?.stop()
//            textToSpeech?.shutdown()
//        }
//    }

//}

@Preview
@Composable
private fun Preview_CameraScreen() {
    CameraContent()
}

private fun startTextRecognition(
    context: Context,
    cameraController: LifecycleCameraController,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    onDetectedTextUpdated: (String) -> Unit
) {

    cameraController.imageAnalysisTargetSize = CameraController.OutputSize(AspectRatio.RATIO_4_3)
    cameraController.setImageAnalysisAnalyzer(
        ContextCompat.getMainExecutor(context),
        TextRecognitionAnalyzer(onDetectedTextUpdated = onDetectedTextUpdated)
    )

    cameraController.bindToLifecycle(lifecycleOwner)
    previewView.controller = cameraController
}

@Composable
fun CameraWithZoomSlider(
    zoomValue: Float,
    onZoomChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            elevation = 4.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Slider(
                    value = zoomValue,
                    onValueChange = { newValue ->
                        onZoomChanged(newValue)
                    },
                    valueRange = 1f..5f,
                    steps = 50,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .weight(1f)
                )
            }
        }
    }
}