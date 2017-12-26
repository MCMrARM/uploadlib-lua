package io.mrarm.uploadlib.lua;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.Reader;

import io.mrarm.uploadlib.lua.scripting.HttpLib;
import io.mrarm.uploadlib.lua.scripting.JsonLib;
import io.mrarm.uploadlib.lua.scripting.UploaderScriptLib;

public class LuaFileUploaderScript {

    private final Globals globals;

    private LuaFileUploaderScriptInfo info;

    public LuaFileUploaderScript() {
        globals = JsePlatform.standardGlobals();
        globals.load(new UploaderScriptLib(this));
        globals.load(new JsonLib());
        globals.load(new HttpLib());
    }

    public void loadFromFile(String filePath) {
        globals.loadfile(filePath).invoke();
        onLoaded();
    }

    public void load(String text) {
        globals.load(text).invoke();
        onLoaded();
    }

    public void load(Reader reader, String name) {
        globals.load(reader, name).invoke();
        onLoaded();
    }

    private void onLoaded() {
        if (info == null)
            throw new LuaError("Script did not call `uploader.register`");
        System.out.println("Uploader loaded: " + info.name + " (" + info.uuid + ")");
    }

    public LuaValue getGlobal(String name) {
        return globals.get(name);
    }

    public LuaFileUploaderScriptInfo getInfo() {
        return info;
    }

    /**
     * Internal use only. Sets the script info for this object.
     * @param info the script info
     */
    public void onInfoRegistered(LuaFileUploaderScriptInfo info) {
        this.info = info;
    }

}
