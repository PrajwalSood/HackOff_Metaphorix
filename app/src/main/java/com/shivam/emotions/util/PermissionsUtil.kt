package com.shivam.emotions.util

import android.Manifest
import android.content.Context
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import timber.log.Timber


class PermissionsUtil(private val context: Context) {

    fun cameraPermission(callback: PermissionsUtilCallback) {
        Dexter.withContext(context)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                   Timber.d("Camera Permission Granted")
                    callback.onPermissionRequest(granted = true)
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    Timber.d("Camera Permission Denied")
                    callback.onPermissionRequest(granted = false)
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    Timber.d("Camera Permission Rationale Shown")
                    token?.continuePermissionRequest()
                }
            })
            .check()

    }

    fun storagePermission(callback: PermissionsUtilCallback) {
        Dexter.withContext(context)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(p0: MultiplePermissionsReport) {
                    if (p0.areAllPermissionsGranted()) {
                        Timber.d("Storage Permission Granted")
                        callback.onPermissionRequest(granted = true)
                    } else {
                        Timber.d("Storage Permission Denied")
                        callback.onPermissionRequest(granted = false)
                    }

                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>,
                    p1: PermissionToken
                ) {
                    Timber.d("Storage Permission Rationale Shown")
                    p1.continuePermissionRequest()
                }
            })
            .check()

    }

}

interface PermissionsUtilCallback {
    fun onPermissionRequest(granted: Boolean)
}