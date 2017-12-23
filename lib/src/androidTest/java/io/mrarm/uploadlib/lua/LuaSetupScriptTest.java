package io.mrarm.uploadlib.lua;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.InputStreamReader;

import io.mrarm.uploadlib.FileUploadProviderManager;
import io.mrarm.uploadlib.ui.login.SimpleLoginActivity;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class LuaSetupScriptTest {

    LuaFileUploadProvider testProvider1;

    @Rule
    public ActivityTestRule<SimpleLoginActivity> activityRule
            = new ActivityTestRule(SimpleLoginActivity.class, true, false);

    @Before
    public void loadScript() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();

        LuaFileUploaderScript script = new LuaFileUploaderScript();
        script.load(new InputStreamReader(appContext.getAssets().open("test.lua")), "test.lua");

        testProvider1 = new LuaFileUploadProvider(script);
        FileUploadProviderManager.add(testProvider1);
    }

    @Test
    public void testSetup() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        Intent intent = new Intent(appContext, SimpleLoginActivity.class);
        intent.putExtra("provider", testProvider1.getUUID().toString());
        activityRule.launchActivity(intent);
        testProvider1.waitForLogInFlow();
    }
}
