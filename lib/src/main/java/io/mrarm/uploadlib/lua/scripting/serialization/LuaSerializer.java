package io.mrarm.uploadlib.lua.scripting.serialization;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.io.IOException;
import java.io.Writer;

public class LuaSerializer {

    public static void serialize(Writer writer, LuaValue value,
                                 int indent, String indentSeq) throws IOException {
        if (value.isnil()) {
            writer.append("nil");
        } else if (value.istable()) {
            LuaTable table = value.checktable();
            writer.append('{');
            appendNewline(writer, indentSeq);

            LuaValue k = LuaValue.NIL;
            int arrayIndex = 1;
            boolean first = true;
            while (true) {
                Varargs n = table.next(k);
                if ((k = n.arg1()).isnil())
                    break;
                if (first) {
                    first = false;
                } else {
                    writer.append(',');
                    appendNewline(writer, indentSeq);
                }
                appendIndent(writer, indent + 1, indentSeq);
                if (k.isinttype() && arrayIndex == k.checkint()) {
                    arrayIndex++;
                } else if (k.type() == LuaValue.TSTRING && isIdentifier(k.checkjstring())) {
                    writer.append(k.checkjstring());
                    writer.append(indentSeq != null ? " = " : "=");
                } else {
                    writer.append('[');
                    serialize(writer, k, indent + 1, indentSeq);
                    writer.append(']');
                    writer.append(indentSeq != null ? " = " : "=");
                }
                LuaValue v = n.arg(2);
                serialize(writer, v, indent + 1, indentSeq);
            }
            if (!first)
                appendNewline(writer, indentSeq);

            appendIndent(writer, indent, indentSeq);
            writer.append('}');
        } else if (value.isinttype()) {
            writer.append(String.valueOf(value.checkint()));
        } else if (value.type() == LuaValue.TNUMBER) {
            writer.append(String.valueOf(value.checkdouble()));
        } else if (value.isstring()) {
            appendLuaString(writer, value.checkjstring());
        } else if (value.isboolean()) {
            if (value.checkboolean())
                writer.append("true");
            else
                writer.append("false");
        }
    }

    private static void appendIndent(Writer writer, int indent, String seq) throws IOException {
        if (seq == null)
            return;
        while (indent-- > 0)
            writer.append(seq);
    }

    private static void appendNewline(Writer writer, String indentSeq) throws IOException {
        if (indentSeq != null)
            writer.append('\n');
    }

    private static void appendLuaString(Writer writer, String str) throws IOException {
        writer.append('\"');
        char[] chars = str.toCharArray();
        int firstNonescapedChar = -1;
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c < 32 || c == '\\' || c == '\"') {
                if (firstNonescapedChar != -1) {
                    writer.write(chars, firstNonescapedChar, i - firstNonescapedChar);
                    firstNonescapedChar = -1;
                }
                if (c == '\n') {
                    writer.append("\\n");
                } else if (c == '\r') {
                    writer.append("\\r");
                } else if (c == '\t') {
                    writer.append("\\t");
                } else if (c == '\\' || c == '\"') {
                    writer.append('\\');
                    firstNonescapedChar = i;
                } else {
                    writer.append('\\');
                    writer.append('x');
                    String s = Integer.toHexString(c);
                    if (s.length() == 1)
                        writer.append('0');
                    writer.append(s);
                }
            } else {
                if (firstNonescapedChar == -1)
                    firstNonescapedChar = i;
            }
        }
        if (firstNonescapedChar != -1)
            writer.write(chars, firstNonescapedChar, chars.length - firstNonescapedChar);
        writer.append('\"');
    }

    private static boolean isIdentifier(String text) {
        for (int i = Token.KEYWORDS.length - 1; i >= 0; --i)
            if (Token.KEYWORDS[i].equals(text))
                return false;
        for (int i = text.length() - 1; i >= 0; --i) {
            char c = text.charAt(i);
            if (c >= '0' && c <= '9') {
                if (i == 0)
                    return false;
                continue;
            }
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_')
                continue;
            return false;
        }
        return true;
    }

}
