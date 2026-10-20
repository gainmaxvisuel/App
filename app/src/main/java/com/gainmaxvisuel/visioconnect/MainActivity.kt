
package com.gainmaxvisuel.visioconnect

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var webView: WebView
    private var pendingRequest: PermissionRequest? = null
    private var pendingResources: Array<String> = emptyArray()

    companion object {
        private const val MEDIA_PERMISSION_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.mediaPlaybackRequiresUserGesture = false

        webView.webViewClient = WebViewClient()

        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                runOnUiThread {
                    val permissions = mutableListOf<String>()

                    if (request.resources.contains(
                            PermissionRequest.RESOURCE_VIDEO_CAPTURE
                        ) && checkSelfPermission(Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissions.add(Manifest.permission.CAMERA)
                    }

                    if (request.resources.contains(
                            PermissionRequest.RESOURCE_AUDIO_CAPTURE
                        ) && checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissions.add(Manifest.permission.RECORD_AUDIO)
                    }

                    if (permissions.isEmpty()) {
                        request.grant(request.resources)
                    } else {
                        pendingRequest = request
                        pendingResources = request.resources
                        requestPermissions(
                            permissions.toTypedArray(),
                            MEDIA_PERMISSION_REQUEST
                        )
                    }
                }
            }
        }

        setContentView(webView)
        webView.loadUrl("file:///android_asset/index.html")
    }

    @Deprecated("Deprecated in Android")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode != MEDIA_PERMISSION_REQUEST) return

        val request = pendingRequest ?: return
        pendingRequest = null

        val cameraGranted =
            checkSelfPermission(Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED

        val micGranted =
            checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED

        val allowed = pendingResources.filter { resource ->
            (resource == PermissionRequest.RESOURCE_VIDEO_CAPTURE &&
                cameraGranted) ||
            (resource == PermissionRequest.RESOURCE_AUDIO_CAPTURE &&
                micGranted)
        }.toTypedArray()

        if (allowed.isNotEmpty()) {
            request.grant(allowed)
        } else {
            request.deny()
            Toast.makeText(
                this,
                "Autorise la caméra ou le microphone dans les paramètres.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @Deprecated("Deprecated in Android")
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        webView.stopLoading()
        webView.destroy()
        super.onDestroy()
    }
}
