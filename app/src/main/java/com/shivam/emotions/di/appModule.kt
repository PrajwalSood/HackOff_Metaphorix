package com.shivam.emotions.di

import com.shivam.emotions.util.PermissionsUtil
import com.shivam.emotions.util.BitmapUtils
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { PermissionsUtil(androidContext()) }
    single { BitmapUtils(androidContext()) }
}