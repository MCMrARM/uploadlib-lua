package io.mrarm.uploadlib.lua.scripting;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaNumber;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class JsonLib extends TwoArgFunction {

    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaTable table = new LuaTable();
        table.set("encode", new encode());
        table.set("decode", new decode());
        table.set("emptyObject", new emptyObject());
        env.set("json", table);
        return table;
    }


    final class encode extends OneArgFunction {
        public LuaValue call(LuaValue i) {
            try {
                StringWriter writer = new StringWriter();
                JsonGenerator generator = new JsonFactory()
                        .createGenerator(writer);
                serialize(generator, i.checktable());
                generator.close();
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
                JsonParser parser = new JsonFactory()
                        .createParser(i.checkjstring());
                return parse(parser);
            } catch (IOException e) {
                throw new LuaError("IO Error: " + e.getMessage());
            }
        }
    }

    final class emptyObject extends OneArgFunction {
        public LuaValue call(LuaValue i) {
            LuaTable obj = new LuaTable();
            LuaTable metatable = new LuaTable();
            metatable.set("jsonObject", LuaValue.TRUE);
            obj.setmetatable(metatable);
            return obj;
        }
    }

    public static void serialize(JsonGenerator generator, LuaTable table) throws IOException {
        boolean isArray = isTableArray(table);
        if (isArray)
            generator.writeStartArray();
        else
            generator.writeStartObject();

        LuaValue k = LuaValue.NIL;
        while (true) {
            Varargs n = table.next(k);
            if ((k = n.arg1()).isnil())
                break;
            if (!isArray)
                generator.writeFieldName(k.tojstring());
            LuaValue v = n.arg(2);
            if (v.istable())
                serialize(generator, v.checktable());
            else if (v.isinttype())
                generator.writeNumber(v.checkint());
            else if (v.type() == TNUMBER)
                generator.writeNumber(v.checkdouble());
            else if (v.isstring())
                generator.writeString(v.checkjstring());
            else if (v.isboolean())
                generator.writeBoolean(v.checkboolean());
            else
                throw new JsonGenerationException("Invalid entry in table", generator);
        }

        if (isArray)
            generator.writeEndArray();
        else
            generator.writeEndObject();
    }

    private static boolean isTableArray(LuaTable table) {
        if (table.getmetatable() != null && table.getmetatable().get("jsonObject").isboolean() &&
                table.getmetatable().get("jsonObject").toboolean())
            return false;
        LuaValue k = LuaValue.NIL;
        while (true) {
            Varargs n = table.next(k);
            if ((k = n.arg1()).isnil())
                break;
            if (!k.isinttype())
                return false;
        }
        return true;
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
                if (obj.type == ParsedObject.TYPE_OBJECT && obj.table.length() == 0) {
                    LuaTable metatable = new LuaTable();
                    metatable.set("jsonObject", LuaValue.TRUE);
                    obj.table.setmetatable(metatable);
                }
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
