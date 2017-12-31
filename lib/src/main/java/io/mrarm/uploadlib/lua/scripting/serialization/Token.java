package io.mrarm.uploadlib.lua.scripting.serialization;

public class Token {

    public static final int TYPE_WHITESPACE = 0;
    public static final int TYPE_STRING = 1;
    public static final int TYPE_NUMBER = 2;
    public static final int TYPE_IDENTIFIER = 3;
    public static final int TYPE_KEYWORD = 4;
    public static final int TYPE_START_TABLE = 5;
    public static final int TYPE_END_TABLE = 6;
    public static final int TYPE_OPEN_SQ_BRACKET = 7;
    public static final int TYPE_CLOSE_SQ_BRACKET = 8;
    public static final int TYPE_DELIM = 9;
    public static final int TYPE_OP_ASSIGN = 10;

    public static final Token WHITESPACE = new Token(TYPE_WHITESPACE, " ");
    public static final Token START_TABLE = new Token(TYPE_START_TABLE, "{");
    public static final Token END_TABLE = new Token(TYPE_END_TABLE, "}");
    public static final Token OPEN_SQ_BRACKET = new Token(TYPE_OPEN_SQ_BRACKET, "{");
    public static final Token CLOSE_SQ_BRACKET = new Token(TYPE_CLOSE_SQ_BRACKET, "}");
    public static final Token DELIM = new Token(TYPE_DELIM, ",");
    public static final Token OP_ASSIGN = new Token(TYPE_OP_ASSIGN, "=");

    public static String[] KEYWORDS = new String[] {"and", "break", "do", "else", "elseif", "end",
            "false", "for", "function", "if", "in", "local", "nil", "not", "or", "repeat", "return",
            "then", "true", "until", "while"};

    private int type;
    private String text;

    public Token(int type, String text) {
        this.type = type;
        this.text = text;
    }

    public int getType() {
        return type;
    }

    public String getText() {
        return text;
    }

}
