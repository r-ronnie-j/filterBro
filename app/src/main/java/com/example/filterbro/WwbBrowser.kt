import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.viewinterop.AndroidView
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView

@Composable
fun ViewSite() {
    val uri by remember {
        mutableStateOf<String?>(null)
    }
    Column {
        WebsiteView(uri = uri)
    }
}

@Composable
fun WebsiteView(uri: String?) {
    var granted by remember {
        mutableStateOf(false)
    }
    var androidPermissionGranted by remember {
        mutableStateOf(false)
    }
    var called by remember {
        mutableStateOf(false)
    }
    var message by remember {
        mutableStateOf("")
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permission ->
            Log.d("called1", "checking the calling order")
            androidPermissionGranted = permission.all {
                it.value
            }
        }
    )
    if (called) {
        Snackbar {
            Text(text = "Android Permission called")
        }
    }
    val session by remember {
        mutableStateOf(GeckoSession())
    }
    BackHandler {
        session.goBack()
    }
    Column {

        AndroidView(factory = { context ->
            val view = GeckoView(context)
            fun loadUri(uri: String) {
                session.loadUri(uri)
            }
            session.contentDelegate = object : GeckoSession.ContentDelegate {

            }
            session.permissionDelegate = object : GeckoSession.PermissionDelegate {
                override fun onAndroidPermissionsRequest(
                    session: GeckoSession,
                    permissions: Array<out String>?,
                    callback: GeckoSession.PermissionDelegate.Callback
                ) {
                    permissions?.let {
                        permissionLauncher.launch(it as Array<String>)
                        Log.d("called2", "checking the order")
                        if (androidPermissionGranted) {
                            message = "Successfully called the android permission"
                            callback.grant()
                        } else callback.reject()
                    }
                    message = "failed called the android permission "
                    callback.reject()
                }

                override fun onMediaPermissionRequest(
                    session: GeckoSession,
                    uri: String,
                    video: Array<out GeckoSession.PermissionDelegate.MediaSource>?,
                    audio: Array<out GeckoSession.PermissionDelegate.MediaSource>?,
                    callback: GeckoSession.PermissionDelegate.MediaCallback
                ) {
                    called = true
                    message = "Media permission called"
                    Log.d("media", "Media permission called")
                    super.onMediaPermissionRequest(session, uri, video, audio, callback)
                }

                override fun onContentPermissionRequest(
                    session: GeckoSession,
                    perm: GeckoSession.PermissionDelegate.ContentPermission
                ): GeckoResult<Int>? {
                    called = true
                    message = "Successfully called content"
                    Log.d("content", "Content permission called")
                    return super.onContentPermissionRequest(session, perm)
                }
            }
            val sRuntime = GeckoRuntime.getDefault(context)
            sRuntime.webExtensionController
                .ensureBuiltIn("resource://android/assets/FirefoxFacebookBlocker/","facebook blocker")
                .accept(
                    {
                        Log.i("extension","Extension has been installed successfully")
                        Log.i("extension",it?.id.toString())
                    },
                    {
                        Log.i("extension","Error has occured while installing the extension")
                        Log.i("extension",it.toString())
                    }
                )
            session.open(sRuntime)
            view.setSession(session)
            loadUri(uri ?: "https://www.facebook.com/")
            view
        })
    }
}


@Composable
fun ControlButtons() {
    val singlePermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
        }
    )
}