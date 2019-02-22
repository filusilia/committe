package com.hxht.mobile.committee

import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import org.json.JSONArray

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("com.hxht.mobile.committe", appContext.packageName)
        val s = "[{\"id\":32,\"name\":\"是是是，赶紧买\"},{\"id\":33,\"name\":\"关我吊事\"}]"
        val arr = JSONArray(s)
        print(arr)
    }
}
