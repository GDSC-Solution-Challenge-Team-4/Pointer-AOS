package de.yanneckreiss.mlkittutorial

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.End
import androidx.compose.foundation.layout.Arrangement.Vertical
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.Color.Companion.Yellow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import de.yanneckreiss.cameraxtutorial.R
import de.yanneckreiss.mlkittutorial.ui.MainScreen
import de.yanneckreiss.mlkittutorial.ui.RecordAndConvertToText
import de.yanneckreiss.mlkittutorial.ui.camera.CameraScreen
import de.yanneckreiss.mlkittutorial.ui.money.ui.MoneyScreen
import de.yanneckreiss.mlkittutorial.ui.pointer.PointerScreen
import de.yanneckreiss.mlkittutorial.ui.theme.JetpackComposeMLKitTutorialTheme
import kotlinx.coroutines.delay


class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalFoundationApi::class, ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            val context: Context = LocalContext.current
            var textToSpeechInitialized by remember { mutableStateOf(false) }
            var textToSpeech: TextToSpeech? by remember { mutableStateOf(null) }

            var sttValue by remember { mutableStateOf("") }

            fun initializeTextToSpeech() {
                if (!textToSpeechInitialized) {
                    textToSpeech = TextToSpeech(context) { status ->
                        if (status == TextToSpeech.SUCCESS) {
                            textToSpeechInitialized = true
                        }
                    }
                }
            }

            val permissionState = rememberPermissionState(
                permission = Manifest.permission.RECORD_AUDIO
            )
            SideEffect {
                permissionState.launchPermissionRequest()
            }

            val speechRecognizerLauncher = rememberLauncherForActivityResult(
                contract = RecordAndConvertToText(),
                onResult = {
                    sttValue = it.toString()
                }
            )

            JetpackComposeMLKitTutorialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val tabItems = listOf(
                        TabItem(
                            title = "money",
                            selectedIcon = R.drawable.icon_money
                        ),
                        TabItem(
                            title = "ocr",
                            selectedIcon = R.drawable.icon_ocr
                        ),
                        TabItem(
                            title = "pointer",
                            selectedIcon = R.drawable.icon_pointer
                        )
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        var selectedTabIndex by remember {
                            mutableIntStateOf(1)
                        }

                        val pagerState = rememberPagerState {
                            tabItems.size
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {

                            IconButton(
                                onClick = {
                                    if (permissionState.status.isGranted) {
                                        speechRecognizerLauncher.launch(Unit)
                                    } else
                                        permissionState.launchPermissionRequest()
                                },
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(60.dp)
                                    .padding(10.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.icon_record), // 이미지 리소스 지정
                                    contentDescription = "Record", // 이미지 버튼에 대한 설명
                                    colorFilter = ColorFilter.tint(Gray)
                                )
                            }
                            if (sttValue.isNotBlank()) {
                                Text(
                                    text = sttValue,
                                    fontSize = 24.sp
                                )
                            }
//                            }
//                            if (sttValue.isNotBlank()) {
//                                when(sttValue){
//                                    "포인터" -> PointerScreen()
//                                    "돈" -> MoneyScreen()
//                                    "텍스트"-> MainScreen()
//                                }
//                            }
                            IconButton(
                                onClick = { /*TODO 여기 버튼 누르면 도움말 dialog!*/ },
                                modifier = Modifier
                                    .padding(5.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.icon_question), // 이미지 리소스 지정
                                    contentDescription = "Question", // 이미지 버튼에 대한 설명
                                    colorFilter = ColorFilter.tint(Gray)
                                )
                            }
                        }

                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) { index ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (index == pagerState.currentPage) {
                                    var string = ttsIndex(index)

//                                    when(sttValue){
//                                            "포인터" -> {
//                                                selectedTabIndex = 2
//                                            }
//                                            "돈" -> {
//                                                selectedTabIndex = 0
//                                            }
//                                            "텍스트"-> {
//                                                selectedTabIndex = 1
//                                            }
//                                    }
                                    LaunchedEffect(selectedTabIndex) {
                                        if (selectedTabIndex != pagerState.currentPage) {
                                            pagerState.scrollToPage(selectedTabIndex)
                                        }
                                    }
                                    when (index) {
                                        0 -> {
                                            initializeTextToSpeech()
                                            textToSpeech?.speak(
                                                string,
                                                TextToSpeech.QUEUE_FLUSH,
                                                null,
                                                null
                                            )
                                            MoneyScreen()
                                        }

                                        1 -> {
                                            initializeTextToSpeech()
                                            textToSpeech?.speak(
                                                string,
                                                TextToSpeech.QUEUE_FLUSH,
                                                null,
                                                null
                                            )
                                            MainScreen()
                                        }

                                        2 -> {
                                            initializeTextToSpeech()
                                            textToSpeech?.speak(
                                                string,
                                                TextToSpeech.QUEUE_FLUSH,
                                                null,
                                                null
                                            )
                                            PointerScreen()
                                        }
                                    }
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = {
                                    //                                    if (textToSpeechInitialized) {
                                    //                                        textToSpeech?.speak(detectedText, TextToSpeech.QUEUE_FLUSH, null, null)
                                    //                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 15.dp, bottom = 15.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.icon_camera2),
                                    contentDescription = "TTS",
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class TabItem(
    var title: String,
    //var unSelectedIcon: ImageVector,
    var selectedIcon: Int
)

fun ttsIndex(index: Int): String {
    var string: String = ""
    when (index) {
        0 -> string = "Currancy Screen"
        1 -> string = "Text Screen"
        2 -> string = "Pointer Screen"
    }

    return string
}