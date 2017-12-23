package io.mrarm.uploadlib.lua.scripting;

import android.graphics.Bitmap;

import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

import io.mrarm.uploadlib.ui.web.WebBrowserController;
import io.mrarm.uploadlib.ui.web.WebBrowserListener;

public class WebBrowserWrapper extends LuaTable implements WebBrowserListener {

    private static String[] LUA_CALLBACK_NAMES = new String[] { "onPageStarted", "onPageFinished",
            "onLoadResource" };

    private final WebActivityControllerWrapper activityController;
    private final WebBrowserController controller;

    public WebBrowserWrapper(WebActivityControllerWrapper activityController,
                             WebBrowserController controller) {
        this.activityController = activityController;
        this.controller = controller;

        set("setUrl", new setUrl());
        set("loadUrl", new loadUrl());
        set("setCookiesEnabled", new setCookiesEnabled());
        set("setCookiesDisabled", new setCookiesDisabled());
        set("finish", new finish());
    }

    public static WebBrowserWrapper create(WebActivityControllerWrapper controller,
                                              LuaTable table) throws InterruptedException {
        WebBrowserController browser = new WebBrowserController(controller.getWrapped());
        WebBrowserWrapper wrapper = new WebBrowserWrapper(controller, browser);
        browser.setListener(wrapper);
        if (table.get("url").isstring()) {
            if (table.get("loadUrl").isboolean() && table.get("loadUrl").toboolean())
                browser.loadUrl(table.get("url").checkjstring());
            else
                browser.setUrl(table.get("url").checkjstring());
        }
        if (table.get("cookiesEnabled").isboolean() && table.get("cookiesEnabled").checkboolean())
            browser.setCookiesEnabled(controller.obtainCookiesHandle());
        else
            browser.setCookiesDisabled();
        for (String callback : LUA_CALLBACK_NAMES) {
            if (table.get(callback).isfunction())
                wrapper.set(callback, table.get(callback));
        }
        return wrapper;
    }

    public WebBrowserController getWrapped() {
        return controller;
    }

    final class setUrl extends TwoArgFunction {
        public LuaValue call(LuaValue self, LuaValue v) {
            controller.setUrl(v.checkjstring());
            return NONE;
        }
    }

    final class loadUrl extends TwoArgFunction {
        public LuaValue call(LuaValue self, LuaValue v) {
            try {
                controller.loadUrl(v.checkjstring());
            } catch (InterruptedException e) {
                throw new LuaInterruptedException(e);
            }
            return NONE;
        }
    }

    final class setCookiesEnabled extends OneArgFunction {
        public LuaValue call(LuaValue self) {
            try {
                controller.setCookiesEnabled(activityController.obtainCookiesHandle());
            } catch (InterruptedException e) {
                throw new LuaInterruptedException(e);
            }
            return NONE;
        }
    }

    final class setCookiesDisabled extends OneArgFunction {
        public LuaValue call(LuaValue self) {
            controller.setCookiesDisabled();
            return NONE;
        }
    }

    final class finish extends OneArgFunction {
        public LuaValue call(LuaValue self) {
            controller.finish();
            return NONE;
        }
    }

    @Override
    public void onPageStarted(WebBrowserController browser, String url, Bitmap favicon) {
        if (get("onPageStarted").isfunction())
            get("onPageStarted").invoke(this, LuaString.valueOf(url));
    }

    @Override
    public void onPageFinished(WebBrowserController browser, String url) {
        if (get("onPageFinished").isfunction())
            get("onPageFinished").invoke(this, LuaString.valueOf(url));
    }

    @Override
    public void onLoadResource(WebBrowserController browser, String url) {
        if (get("onLoadResource").isfunction())
            get("onLoadResource").invoke(this, LuaString.valueOf(url));
    }

}
