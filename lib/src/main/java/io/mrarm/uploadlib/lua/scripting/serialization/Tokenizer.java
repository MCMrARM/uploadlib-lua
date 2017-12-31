package io.mrarm.uploadlib.lua.scripting.serialization;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;

public class Tokenizer {

    private Reader reader;
    private int lc = -1;
    private int offset = 0;

    public Tokenizer(Reader reader) {
        this.reader = reader;
    }

    public int getCurrentOffset() {
        return offset;
    }

    private int read() throws IOException {
        offset++;
        int c;
        if (lc != -1) {
            c = lc;
            lc = -1;
        } else {
            c = reader.read();
        }
        return c;
    }

    private int peek() throws IOException {
        if (lc == -1)
            lc = reader.read();
        return lc;
    }

    public Token readToken(boolean ignoreWhitespace) throws IOException, ParseException {
        int c = peek();
        if (c < 0)
            return null;
        if (c == ' ' || c == '\n' || c == '\r' || c == '\t') {
            while (c == ' ' || c == '\n' || c == '\r' || c == '\t') {
                read();
                c = peek();
            }
            if (!ignoreWhitespace)
                return Token.WHITESPACE;
        }
        if (c == '\"')
            return new Token(Token.TYPE_STRING, readString());
        if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_') {
            String identifier = readIdentifier();
            for (int i = Token.KEYWORDS.length - 1; i >= 0; --i)
                if (Token.KEYWORDS[i].equals(identifier))
                    return new Token(Token.TYPE_KEYWORD, identifier);
            return new Token(Token.TYPE_IDENTIFIER, identifier);
        }
        if ((c >= '0' && c <= '9') || c == '-')
            return new Token(Token.TYPE_NUMBER, readNumber());
        if (c == '{' || c == '}' || c == '[' || c == ']' || c == '=' || c == ',') {
            read();
            if (c == '{')
                return Token.START_TABLE;
            if (c == '}')
                return Token.END_TABLE;
            if (c == '[')
                return Token.OPEN_SQ_BRACKET;
            if (c == ']')
                return Token.CLOSE_SQ_BRACKET;
            if (c == '=')
                return Token.OP_ASSIGN;
            if (c == ',')
                return Token.DELIM;
        }
        throw new ParseException("Invalid character", offset);
    }

    private String readString() throws IOException, ParseException {
        StringBuilder b = new StringBuilder();
        if (read() != '\"')
            throw new ParseException("Invalid string", offset);
        while (true) {
            int c = read();
            if (c < 0)
                throw new ParseException("Unexpected end of file", offset);
            if (c == '\"')
                return b.toString();
            if (c == '\\') {
                c = read();
                if (c == '\\') {
                    b.append('\\');
                } else if (c == '"') {
                    b.append('"');
                } else if (c == 'n') {
                    b.append('\n');
                } else if (c == 'r') {
                    b.append('\r');
                } else if (c == 't') {
                    b.append('\t');
                } else if (c == 'x') {
                    char[] ch = new char[] { (char) read(), (char) read() };
                    int code = Integer.parseInt(new String(ch), 16);
                    b.append((char) code);
                }
                continue;
            }
            b.append((char) c);
        }
    }

    private String readIdentifier() throws IOException, ParseException {
        StringBuilder b = new StringBuilder();
        while (true) {
            int c = peek();
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') ||
                    c == '_') {
                b.append((char) read());
            } else {
                return b.toString();
            }
        }
    }

    public String readNumber() throws IOException, ParseException {
        StringBuilder b = new StringBuilder();
        boolean hasE = false;
        boolean hasComma = false;
        while (true) {
            int c = peek();
            if (b.length() == 0 && c == '-') {
                b.append(c);
                read();
                continue;
            }
            if ((c >= '0' && c <= '9') || c == '.' || c == 'e' || c == 'E') {
                if (c == '.') {
                    if (hasComma || hasE)
                        throw new ParseException("Invalid number", offset);
                    hasComma = true;
                }
                if (c == 'e' || c == 'E') {
                    if (hasE)
                        throw new ParseException("Invalid number", offset);
                    hasE = true;
                }
                b.append((char) read());
            } else {
                return b.toString();
            }
        }
    }


}
