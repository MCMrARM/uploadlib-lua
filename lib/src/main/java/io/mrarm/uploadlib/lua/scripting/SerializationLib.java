package io.mrarm.uploadlib.lua.scripting;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;

import io.mrarm.uploadlib.lua.scripting.serialization.LuaDeserializer;
import io.mrarm.uploadlib.lua.scripting.serialization.LuaSerializer;

public class SerializationLib extends TwoArgFunction {

    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaTable table = new LuaTable();
        table.set("encode", new encode(null));
        table.set("prettyPrint", new encode("  "));
        table.set("decode", new decode());
        env.set("serialization", table);
        return table;
    }


    final class encode extends OneArgFunction {
        private String indent;
        public encode(String indent) {
            this.indent = indent;
        }
        public LuaValue call(LuaValue i) {
            try {
                StringWriter writer = new StringWriter();
                LuaSerializer.serialize(writer, i, 0, indent);
                writer.close();
                return LuaValue.valueOf(writer.toString());
            } catch (IOException e) {
                throw new LuaError("IO Error: " + e.getMessage());
            }
        }
    }

    final class decode extends OneArgFunction {
        public LuaValue call(LuaValue i) {
            try {
                StringReader reader = new StringReader(i.checkjstring());
                return LuaDeserializer.deserialize(reader);
            } catch (IOException e) {
                throw new LuaError("IO Error: " + e.getMessage());
            } catch (ParseException e) {
                throw new RuntimeException("Parse error: " + e.getMessage(), e);
            }
        }
    }

}
