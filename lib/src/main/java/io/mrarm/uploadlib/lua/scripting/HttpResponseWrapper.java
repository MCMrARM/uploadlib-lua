package io.mrarm.uploadlib.lua.scripting;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;

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
        set("jsonBody", new jsonBody());
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

    final class jsonBody extends TwoArgFunction {
        public LuaValue call(LuaValue self, LuaValue v) {
            try {
                String resp = response.body().string();
                JsonParser parser = new JsonFactory()
                        .createParser(resp);
                return JsonLib.parse(parser);
            } catch (IOException e) {
                throw new LuaError("IO Error: " + e.getMessage());
            }
        }
    }

}
