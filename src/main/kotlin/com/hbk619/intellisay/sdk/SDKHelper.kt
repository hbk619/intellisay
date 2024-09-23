package com.hbk619.intellisay.sdk

import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.vfs.VirtualFile

interface SDKHelper {
    fun createPythonSdk(sdkHome: VirtualFile): Sdk?
}