package de.yanneckreiss.mlkittutorial.ui.camera

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.camera.core.AspectRatio
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Scaffold
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItemDefaults.contentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.modifier.modifierLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Button
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import de.yanneckreiss.cameraxtutorial.R

@Composable
fun CameraScreen() {
    CameraContent()
}

@Composable
private fun CameraContent() {

    val context: Context = LocalContext.current
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val cameraController: LifecycleCameraController = remember { LifecycleCameraController(context) }
    var detectedText: String by remember { mutableStateOf("No text detected yet..") }
    // Text to speech related variables
    var textToSpeechInitialized by remember { mutableStateOf(false) }
    var textToSpeech: TextToSpeech? by remember { mutableStateOf(null) }


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


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        //topBar = { TopAppBar() },
    ) { paddingValues: PaddingValues ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(White)
                        .padding(5.dp),
                    contentAlignment = Alignment.TopEnd // 버튼 오른쪽 상단에 배치??
                ){
                IconButton(
                    onClick = {
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_question),
                        contentDescription = "question",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
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
                        }.also {
                        }
                    }
                )

            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
                    .padding(16.dp)
            ) {
                Text(
                    text = detectedText,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                val contentColor = LocalContentColor.current
                // Button for Text-to-Speech
                IconButton(
                    onClick = {
                        if (textToSpeechInitialized) {
                            textToSpeech?.speak(detectedText, TextToSpeech.QUEUE_FLUSH, null, null)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_camera),
                        contentDescription = "TTS",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
//                ZoomSeekBar { zoomLevel ->
//                    // 여기에서 zoomLevel 값을 사용하여 카메라 줌 조정
//                }
            }
        }
    }

    // Initialize Text-to-Speech when the composable is first composed
    LaunchedEffect(Unit) {
        initializeTextToSpeech()
    }

    // Dispose of Text-to-Speech when the composable is removed
    DisposableEffect(Unit) {
        onDispose {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        }
    }

}

private fun startTextRecognition(
    context: Context,
    cameraController: LifecycleCameraController,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    onDetectedTextUpdated: (String) -> Unit
) {

    cameraController.imageAnalysisTargetSize = CameraController.OutputSize(AspectRatio.RATIO_16_9)
    cameraController.setImageAnalysisAnalyzer(
        ContextCompat.getMainExecutor(context),
        TextRecognitionAnalyzer(onDetectedTextUpdated = onDetectedTextUpdated)
    )

    cameraController.bindToLifecycle(lifecycleOwner)
    previewView.controller = cameraController
}

//@Composable
//fun ZoomSeekBar(onZoomChange: (Int) -> Unit) {
//    var zoomValue by remember { mutableStateOf(0f) }
//
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        modifier = Modifier,
//    ) {
//        Text(text = "Zoom Level: ${zoomValue.toInt()}")
//        Slider(
//            value = zoomValue,
//            onValueChange = { newValue ->
//                zoomValue = newValue
//                onZoomChange(newValue.toInt())
//            },
//            valueRange = 0f..100f,
//            steps = 1,
//            modifier = Modifier.padding(horizontal = 16.dp)
//        )
//    }
//}