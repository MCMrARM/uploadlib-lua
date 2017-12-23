package io.mrarm.uploadlib.lua.scripting;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

import java.util.UUID;

import io.mrarm.uploadlib.lua.LuaFileUploaderScript;
import io.mrarm.uploadlib.lua.LuaFileUploaderScriptInfo;

public class UploaderScriptLib extends TwoArgFunction {

    private final LuaFileUploaderScript script;

    public UploaderScriptLib(LuaFileUploaderScript script) {
        this.script = script;
    }

    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaTable coroutine = new LuaTable();
        coroutine.set("register", new register());
        env.set("uploader", coroutine);
        return coroutine;
    }

    final class register extends OneArgFunction {
        public LuaValue call(LuaValue i) {
            if (script.getInfo() != null)
                throw new LuaError("Script already registered");
            LuaTable t = i.checktable();
            LuaFileUploaderScriptInfo info = new LuaFileUploaderScriptInfo();
            info.name = t.get("name").checkjstring();
            if (t.get("uuid").isstring())
                info.uuid = UUID.fromString(t.get("uuid").checkjstring());
            else
                info.uuid = UUID.nameUUIDFromBytes(info.name.getBytes());
            if (t.get("loginSupported").isboolean())
                info.loginSupported = t.get("loginSupported").checkboolean();
            if (t.get("loginRequired").isboolean())
                info.loginRequired = t.get("loginRequired").checkboolean();
            script.onInfoRegistered(info);
            return NONE;
        }
    }

}
