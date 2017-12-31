package io.mrarm.uploadlib.lua.scripting.serialization;

import org.luaj.vm2.LuaNumber;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class LuaDeserializer {

    public static LuaValue deserialize(Tokenizer tokenizer) throws IOException, ParseException {
        List<LuaTable> stack = new ArrayList<>(128);
        Token token;
        while ((token = tokenizer.readToken(true)) != null) {
            int type = token.getType();
            LuaValue key = null;
            if (type == Token.TYPE_DELIM) {
                token = tokenizer.readToken(true);
                type = token.getType();
            }
            if (type == Token.TYPE_IDENTIFIER) {
                if (stack.size() == 0)
                    throw new ParseException("Unexpected identifier", tokenizer.getCurrentOffset());
                key = LuaString.valueOf(token.getText());
            } else if (type == Token.TYPE_OPEN_SQ_BRACKET) {
                key = deserialize(tokenizer); // do a recursive call, as it's very hard to implement it otherwise
                token = tokenizer.readToken(true);
                if (token == null)
                    throw new ParseException("Unexpected end of file", tokenizer.getCurrentOffset());
                if (token.getType() != Token.TYPE_CLOSE_SQ_BRACKET)
                    throw new ParseException("Unexpected token", tokenizer.getCurrentOffset());
            }
            if (key != null) {
                token = tokenizer.readToken(true);
                if (token == null)
                    throw new ParseException("Unexpected end of file", tokenizer.getCurrentOffset());
                if (token.getType() != Token.TYPE_OP_ASSIGN)
                    throw new ParseException("Unexpected token " + token.getType(), tokenizer.getCurrentOffset());
                token = tokenizer.readToken(true);
                if (token == null)
                    throw new ParseException("Unexpected end of file", tokenizer.getCurrentOffset());
            }
            type = token.getType();
            LuaValue value = null;
            LuaTable valueAddTo = stack.size() > 0 ? stack.get(stack.size() - 1) : null;
            if (type == Token.TYPE_START_TABLE) {
                value = new LuaTable();
                stack.add((LuaTable) value);
            } else if (type == Token.TYPE_END_TABLE) {
                if (stack.size() == 0)
                    throw new ParseException("Unexpected end table", tokenizer.getCurrentOffset());
                if (stack.size() == 1)
                    return stack.remove(0);
                stack.remove(stack.size() - 1);
            } else if (type == Token.TYPE_STRING) {
                value = LuaString.valueOf(token.getText());
            } else if (type == Token.TYPE_NUMBER) {
                try {
                    double num = Double.parseDouble(token.getText());
                    value = LuaNumber.valueOf(num);
                } catch (NumberFormatException e) {
                    throw new ParseException("Invalid number", tokenizer.getCurrentOffset());
                }
            } else if (type == Token.TYPE_KEYWORD) {
                if (token.getText().equals("true"))
                    value = LuaValue.TRUE;
                else if (token.getText().equals("false"))
                    value = LuaValue.FALSE;
                else if (token.getText().equals("nil"))
                    value = LuaValue.NIL;
                else
                    throw new ParseException("Unexpected keyword", tokenizer.getCurrentOffset());
            } else {
                throw new ParseException("Unexpected token " + type, tokenizer.getCurrentOffset());
            }

            if (value == null)
                continue;
            if (valueAddTo == null) {
                if (type == Token.TYPE_START_TABLE)
                    continue;
                return value;
            }
            if (key != null)
                valueAddTo.set(key, value);
            else
                valueAddTo.insert(valueAddTo.rawlen() + 1, value);
        }
        return null;
    }

    public static LuaValue deserialize(Reader reader) throws IOException, ParseException {
        Tokenizer tokenizer = new Tokenizer(reader);
        return deserialize(tokenizer);
    }

}
