package com.kahomesl.allergenradar

import android.content.pm.ApplicationInfo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DebugNetworkConfigurationTest {
    @Test
    fun debugBuildAllowsCleartextForEmulatorLoopbackApi() {
        val applicationInfo = InstrumentationRegistry.getInstrumentation().targetContext.applicationInfo

        assertTrue(applicationInfo.flags and ApplicationInfo.FLAG_USES_CLEARTEXT_TRAFFIC != 0)
    }
}
