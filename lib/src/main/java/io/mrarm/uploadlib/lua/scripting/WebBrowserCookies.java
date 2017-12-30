package io.mrarm.uploadlib.lua.scripting;

import android.webkit.CookieManager;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

import io.mrarm.uploadlib.ui.web.WebViewCookieHandle;

public class WebBrowserCookies extends LuaTable {

    private WebViewCookieHandle cookiesHandle;

    public WebBrowserCookies(WebViewCookieHandle cookiesHandle) {
        this.cookiesHandle = cookiesHandle;

        set("getRawCookies", new getRawCookies());
        set("setRawCookie", new setRawCookie());
    }

    private void checkHandle() {
        if (!cookiesHandle.isObtained())
            throw new LuaError("Cookies handle must be obtained");
    }

    final class getRawCookies extends TwoArgFunction {
        public LuaValue call(LuaValue self, LuaValue url) {
            checkHandle();
            String cookies = CookieManager.getInstance().getCookie(url.checkjstring());
            return cookies != null ? LuaValue.valueOf(cookies) : NIL;
        }
    }

    final class setRawCookie extends ThreeArgFunction {
        public LuaValue call(LuaValue self, LuaValue url, LuaValue cookie) {
            checkHandle();
            CookieManager.getInstance().setCookie(url.checkjstring(), cookie.checkjstring());
            return NIL;
        }
    }

}
