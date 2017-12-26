package io.mrarm.uploadlib.lua;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collection;
import java.util.UUID;

import io.mrarm.uploadlib.FileUploadRequest;
import io.mrarm.uploadlib.FileUploadUserContext;
import io.mrarm.uploadlib.UploadData;
import io.mrarm.uploadlib.lua.scripting.LuaInterruptedException;
import io.mrarm.uploadlib.lua.scripting.LuaUserUploadData;
import io.mrarm.uploadlib.lua.scripting.WebActivityControllerWrapper;
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
    public FileUploadRequest upload(@Nullable Context ctx,
                                    @Nullable FileUploadUserContext userContext,
                                    @NonNull UploadData data) {
        script.getGlobal("uploader").get("upload").invoke(new LuaUserUploadData(data));
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
        WebActivityControllerWrapper wrapper = new WebActivityControllerWrapper(controller);
        try {
            script.getGlobal("uploader").get("login").invoke(wrapper);
        } catch (LuaInterruptedException e) {
            throw (InterruptedException) e.getCause();
        } finally {
            wrapper.releaseCookiesHandle();
        }
    }

}
