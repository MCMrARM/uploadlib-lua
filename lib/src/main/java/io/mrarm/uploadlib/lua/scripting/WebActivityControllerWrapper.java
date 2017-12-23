package io.mrarm.uploadlib.lua.scripting;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

import io.mrarm.uploadlib.ui.web.WebActivityController;
import io.mrarm.uploadlib.ui.web.WebBrowserController;
import io.mrarm.uploadlib.ui.web.WebViewCookieHandle;

public class WebActivityControllerWrapper extends LuaTable {

    private final WebActivityController controller;
    private WebViewCookieHandle cookiesHandle;

    public WebActivityControllerWrapper(WebActivityController controller) {
        this.controller = controller;

        set("setLoadingState", new setLoadingState());
        set("createWebBrowser", new createWebBrowser());
        set("setWebState", new setWebState());
    }

    public WebActivityController getWrapped() {
        return controller;
    }

    public WebViewCookieHandle obtainCookiesHandle() throws InterruptedException {
        if (cookiesHandle == null)
            cookiesHandle = WebViewCookieHandle.obtainHandle();
        return cookiesHandle;
    }

    public void releaseCookiesHandle() {
        if (cookiesHandle != null) {
            cookiesHandle.release();
            cookiesHandle = null;
        }
    }

    final class setLoadingState extends OneArgFunction {
        public LuaValue call(LuaValue self) {
            controller.setLoadingState();
            return NONE;
        }
    }

    final class createWebBrowser extends TwoArgFunction {
        public LuaValue call(LuaValue self, LuaValue v) {
            if (v.istable()) {
                LuaTable table = v.checktable();
                try {
                    WebBrowserWrapper browser = WebBrowserWrapper.create(
                            WebActivityControllerWrapper.this, table);
                    return browser;
                } catch (InterruptedException e) {
                    throw new LuaInterruptedException(e);
                }
            } else {
                return new WebBrowserWrapper(WebActivityControllerWrapper.this,
                        new WebBrowserController(controller));
            }
        }
    }

    final class setWebState extends TwoArgFunction {
        public LuaValue call(LuaValue self, LuaValue v) {
            if (v.istable()) {
                LuaTable table = v.checktable();
                try {
                    WebBrowserWrapper browser;
                    if (table instanceof WebBrowserWrapper) {
                        browser = (WebBrowserWrapper) table;
                    } else {
                        browser = WebBrowserWrapper.create(
                                WebActivityControllerWrapper.this, table);
                    }
                    controller.setWebState(browser.getWrapped());
                    return browser;
                } catch (InterruptedException e) {
                    throw new LuaInterruptedException(e);
                }
            }
            throw new LuaError("Invalid argument given to setWebState (must be a table)");
        }
    }

}
