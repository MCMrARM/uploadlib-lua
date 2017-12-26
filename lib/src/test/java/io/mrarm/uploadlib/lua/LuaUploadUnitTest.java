package io.mrarm.uploadlib.lua;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;

import io.mrarm.uploadlib.FileUploadData;
import io.mrarm.uploadlib.FileUploadProviderManager;

/**
 * Local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class LuaUploadUnitTest {

    LuaFileUploadProvider testProvider1;

    @Before
    public void loadScript() throws Exception {
        LuaFileUploaderScript script = new LuaFileUploaderScript();
        script.load(new InputStreamReader(LuaUploadUnitTest.class.getResourceAsStream("/test.lua")), "test.lua");

        testProvider1 = new LuaFileUploadProvider(script);
        FileUploadProviderManager.add(testProvider1);
    }

    @Test
    public void testUpload() throws Exception {
        File testFile = File.createTempFile("testupload", ".txt");
        testFile.deleteOnExit();
        FileWriter writer = new FileWriter(testFile);
        writer.write("This is a\ntest file.");
        writer.close();

        FileUploadData fileUpload = new FileUploadData(testFile, "text/plain");
        testProvider1.upload(null, null, fileUpload);
    }
}