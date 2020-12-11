package com.shivam.emotions

import androidx.fragment.app.Fragment
import com.shivam.emotions.util.PermissionsUtil
import com.shivam.emotions.util.BitmapUtils
import org.koin.android.ext.android.inject

open class BaseFragment : Fragment() {
    val permissionUtil: PermissionsUtil by inject()
    val bitmapUtils: BitmapUtils by inject()
}