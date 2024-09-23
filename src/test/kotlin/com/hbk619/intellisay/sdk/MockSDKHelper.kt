package com.hbk619.intellisay.sdk

import com.hbk619.intellisay.mock.Call
import com.hbk619.intellisay.mock.Mock
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.impl.JavaSdkImpl
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil
import com.intellij.openapi.vfs.VirtualFile

class MockSDKHelper: SDKHelper, Mock() {
    var sdk: Sdk? = null

    override fun createPythonSdk(sdkHome: VirtualFile): Sdk? {
        addCall(Call(listOf( sdkHome)))
        sdk = SdkConfigurationUtil.setupSdk(
            emptyArray(),
            sdkHome,
            JavaSdkImpl.getInstance(),
            false,
            null,
            sdkHome.path
        )
        return sdk
    }

    override fun reset() {
        super.reset()
        if (sdk != null) {
            WriteAction.run<Throwable> {
                ProjectJdkTable.getInstance().removeJdk(sdk!!)
            }
        }
    }
}