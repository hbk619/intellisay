package com.hbk619.intellisay.sdk

import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.python.sdk.PythonSdkType

class SDKUtils: SDKHelper {
    override fun createPythonSdk(sdkHome: VirtualFile): Sdk? {
        return SdkConfigurationUtil.setupSdk(
            emptyArray(),
            sdkHome,
            PythonSdkType.getInstance(),
            false,
            null,
            sdkHome.path
        )
    }
}