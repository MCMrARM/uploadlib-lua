package io.mrarm.uploadlib.lua.scripting;

public class LuaInterruptedException extends RuntimeException {

    public LuaInterruptedException(InterruptedException e) {
        super(e);
    }

}
