package io.mrarm.uploadlib.lua;

import org.junit.Test;

import java.io.InputStreamReader;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void testScriptLoader() throws Exception {
        LuaFileUploaderScript script = new LuaFileUploaderScript();
        script.load(new InputStreamReader(ExampleUnitTest.class.getResourceAsStream("/test.lua")), "test.lua");
    }
}