package com.kahomesl.allergenradar

import android.Manifest
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DebugNetworkConfigurationTest {
    @Test
    fun appDeclaresInternetPermission() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        assertEquals(PackageManager.PERMISSION_GRANTED, context.checkSelfPermission(Manifest.permission.INTERNET))
    }

    @Test
    fun debugBuildAllowsCleartextForEmulatorLoopbackApi() {
        val applicationInfo = InstrumentationRegistry.getInstrumentation().targetContext.applicationInfo

        assertTrue(applicationInfo.flags and ApplicationInfo.FLAG_USES_CLEARTEXT_TRAFFIC != 0)
    }
}
