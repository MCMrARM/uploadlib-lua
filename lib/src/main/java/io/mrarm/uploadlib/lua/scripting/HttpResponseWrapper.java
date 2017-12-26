package io.mrarm.uploadlib.lua.scripting;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.Response;

public class HttpResponseWrapper extends LuaTable {

    private final Response response;

    public HttpResponseWrapper(Response response) {
        this.response = response;

        set("code", new code());
        set("stringBody", new stringBody());
        set("bytesBody", new bytesBody());
        set("jsonBody", new jsonBody());
        set("header", new header());
        set("headers", new headers());
        set("headerNames", new headerNames());
        set("allHeaders", new allHeaders());
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

    final class header extends ThreeArgFunction {
        public LuaValue call(LuaValue self, LuaValue header, LuaValue defaultValue) {
            String headerValue = response.header(header.tojstring());
            return headerValue != null ? LuaValue.valueOf(headerValue) : defaultValue;
        }
    }

    final class headers extends TwoArgFunction {
        public LuaValue call(LuaValue self, LuaValue header) {
            List<String> headers = response.headers(header.tojstring());
            LuaTable luaTable = new LuaTable(headers.size(), 0);
            for (int i = 0; i < headers.size(); i++)
                luaTable.rawset(i + 1, header.get(i));
            return luaTable;
        }
    }

    final class headerNames extends OneArgFunction {
        public LuaValue call(LuaValue self) {
            Set<String> headers = response.headers().names();
            LuaTable luaTable = new LuaTable(headers.size(), 0);
            int i = 1;
            for (String header : headers)
                luaTable.rawset(i++, header);
            return luaTable;
        }
    }

    final class allHeaders extends TwoArgFunction {
        public LuaValue call(LuaValue self, LuaValue luaCreateArrays) {
            boolean createArrays = luaCreateArrays.isboolean() && luaCreateArrays.toboolean();
            LuaTable luaTable = new LuaTable();
            for (int i = response.headers().size() - 1; i >= 0; i--) {
                LuaString key = LuaValue.valueOf(response.headers().name(i));
                LuaString val = LuaValue.valueOf(response.headers().value(i));
                LuaValue tabVal = luaTable.get(key);
                if (tabVal.isnil()) {
                    tabVal = createArrays ? new LuaTable() : val;
                    luaTable.set(key, tabVal);
                    if (!createArrays)
                        continue;
                } else if (!tabVal.istable()) {
                    LuaValue oldVal = tabVal;
                    tabVal = new LuaTable();
                    ((LuaTable) tabVal).insert(1, oldVal);
                    luaTable.set(key, tabVal);
                }
                ((LuaTable) tabVal).insert(tabVal.length() + 1, val);
            }
            return luaTable;
        }
    }

}
