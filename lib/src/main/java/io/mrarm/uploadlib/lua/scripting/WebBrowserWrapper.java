package io.mrarm.uploadlib.lua.scripting;

import android.graphics.Bitmap;

import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;

import io.mrarm.uploadlib.ui.web.WebBrowserController;
import io.mrarm.uploadlib.ui.web.WebBrowserListener;

public class WebBrowserWrapper extends LuaTable implements WebBrowserListener {

    private static String[] LUA_CALLBACK_NAMES = new String[] { "onPageStarted", "onPageFinished",
            "onLoadResource" };

    private final WebBrowserController controller;

    public WebBrowserWrapper(WebBrowserController controller) {
        this.controller = controller;
    }

    public static WebBrowserController create(WebActivityControllerWrapper controller,
                                              LuaTable table) throws InterruptedException {
        WebBrowserController browser = new WebBrowserController(controller.getWrapped());
        WebBrowserWrapper wrapper = new WebBrowserWrapper(browser);
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
        return browser;
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
