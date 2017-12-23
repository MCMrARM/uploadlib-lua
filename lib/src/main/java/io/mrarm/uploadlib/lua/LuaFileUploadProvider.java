package io.mrarm.uploadlib.lua;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.io.InputStream;
import java.util.Collection;
import java.util.UUID;

import io.mrarm.uploadlib.FileUploadRequest;
import io.mrarm.uploadlib.FileUploadUserContext;
import io.mrarm.uploadlib.ui.login.SimpleLoginFileUploadProvider;
import io.mrarm.uploadlib.ui.web.WebActivityController;

public class LuaFileUploadProvider extends SimpleLoginFileUploadProvider {

    private LuaFileUploaderScript script;

    public LuaFileUploadProvider(LuaFileUploaderScript script) {
        this.script = script;
    }

    @NonNull
    @Override
    public UUID getUUID() {
        return script.getInfo().uuid;
    }

    @Override
    public FileUploadRequest upload(@Nullable Context context, @Nullable FileUploadUserContext fileUploadUserContext, @Nullable String s, @Nullable String s1, @NonNull InputStream inputStream) {
        return null;
    }

    @Override
    public boolean canLogIn() {
        return script.getInfo().loginSupported;
    }

    @Override
    public boolean isLogInRequired() {
        return script.getInfo().loginRequired;
    }

    @Override
    public Collection<FileUploadUserContext> getLoggedInUsers() {
        return null;
    }

    @Override
    public void handleLogInFlow(WebActivityController controller) throws InterruptedException {
        script.getGlobal("uploader_login").invoke(CoerceJavaToLua.coerce(controller));
    }

}
