package io.mrarm.uploadlib.lua.scripting;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;

import java.io.IOException;

import okhttp3.Response;

public class HttpResponseWrapper extends LuaTable {

    private final Response response;

    public HttpResponseWrapper(Response response) {
        this.response = response;

        set("code", new code());
        set("stringBody", new stringBody());
        set("bytesBody", new bytesBody());
    }


    final class code extends TwoArgFunction {
        public LuaValue call(LuaValue self, LuaValue v) {
            return LuaValue.valueOf(response.code());
        }
    }

    final class stringBody extends TwoArgFunction {
        public LuaValue call(LuaValue self, LuaValue v) {
            try {
                return LuaValue.valueOf(response.body().string());
            } catch (IOException e) {
                throw new LuaError("IO Error: " + e.getMessage());
            }
        }
    }

    final class bytesBody extends TwoArgFunction {
        public LuaValue call(LuaValue self, LuaValue v) {
            try {
                return LuaValue.valueOf(response.body().bytes());
            } catch (IOException e) {
                throw new LuaError("IO Error: " + e.getMessage());
            }
        }
    }

}
