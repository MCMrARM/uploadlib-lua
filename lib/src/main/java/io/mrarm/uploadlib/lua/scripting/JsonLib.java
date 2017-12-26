package io.mrarm.uploadlib.lua.scripting;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonLib extends TwoArgFunction {

    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaTable table = new LuaTable();
        table.set("decode", new decode());
        env.set("json", table);
        return table;
    }


    final class decode extends OneArgFunction {
        public LuaValue call(LuaValue i) {
            try {
                JsonParser parser = new JsonFactory()
                        .createParser(i.checkjstring());
                return parse(parser);
            } catch (IOException e) {
                throw new LuaError("IO Error: " + e.getMessage());
            }
        }
    }


    public static LuaTable parse(JsonParser parser) throws IOException {
        List<ParsedObject> stack = new ArrayList<>(128);
        while (true) {
            JsonToken token = parser.nextToken();
            String fieldName = null;
            if (token != JsonToken.END_OBJECT && stack.size() > 0 &&
                    stack.get(stack.size() - 1).type == ParsedObject.TYPE_OBJECT) {
                if (token != JsonToken.FIELD_NAME)
                    throw new JsonParseException(parser, "Missing field name");
                fieldName = parser.getValueAsString();
                token = parser.nextToken();
            }
            if (token.isStructStart()) {
                int type = (token == JsonToken.START_OBJECT ? ParsedObject.TYPE_OBJECT :
                        ParsedObject.TYPE_ARRAY);
                ParsedObject obj = new ParsedObject(type);
                if (stack.size() > 0) {
                    ParsedObject last = stack.get(stack.size() - 1);
                    if (fieldName != null)
                        last.table.set(fieldName, obj.table);
                    else
                        last.table.insert(last.table.rawlen() + 1, obj.table);
                }
                stack.add(obj);
            } else if (token.isStructEnd()) {
                ParsedObject obj = stack.remove(stack.size() - 1);
                if (stack.size() == 0)
                    return obj.table;
            } else {
                LuaValue val;
                if (token == JsonToken.VALUE_TRUE)
                    val = LuaValue.TRUE;
                else if (token == JsonToken.VALUE_FALSE)
                    val = LuaValue.FALSE;
                else if (token == JsonToken.VALUE_NULL)
                    val = LuaValue.NIL;
                else if (token == JsonToken.VALUE_NUMBER_INT)
                    val = LuaInteger.valueOf(parser.getValueAsLong());
                else if (token == JsonToken.VALUE_NUMBER_FLOAT)
                    val = LuaValue.valueOf(parser.getValueAsDouble());
                else if (token == JsonToken.VALUE_STRING)
                    val = LuaValue.valueOf(parser.getValueAsString());
                else
                    throw new JsonParseException(parser, "Unknown token");
                if (stack.size() == 0)
                    throw new JsonParseException(parser, "Invalid value token");
                ParsedObject last = stack.get(stack.size() - 1);
                if (fieldName != null)
                    last.table.set(fieldName, val);
                else
                    last.table.insert(last.table.rawlen() + 1, val);
            }
        }
    }

    private static class ParsedObject {
        private static int TYPE_ARRAY = 0;
        private static int TYPE_OBJECT = 1;

        LuaTable table;
        int type;

        public ParsedObject(int type) {
            this.type = type;
            this.table = new LuaTable();
        }
    }

}
