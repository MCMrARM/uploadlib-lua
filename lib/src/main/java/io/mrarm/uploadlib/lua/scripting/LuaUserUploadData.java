package io.mrarm.uploadlib.lua.scripting;

import android.support.annotation.Nullable;

import org.luaj.vm2.LuaUserdata;
import org.luaj.vm2.LuaValue;

import java.io.IOException;

import io.mrarm.uploadlib.UploadData;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

public class LuaUserUploadData extends LuaUserdata {

    public LuaUserUploadData(UploadData uploadData) {
        super(uploadData);
    }

    public UploadData getUploadData() {
        return (UploadData) userdata();
    }

    @Override
    public LuaValue get(LuaValue key) {
        if (key.isstring()) {
            String k = key.checkjstring();
            switch (k) {
                case "size":
                    return LuaValue.valueOf(getUploadData().size());
                case "filename":
                    String filename = getUploadData().getName();
                    return filename != null ? LuaValue.valueOf(filename) : LuaValue.NIL;
                case "mimetype":
                    String mimetype = getUploadData().getName();
                    return mimetype != null ? LuaValue.valueOf(mimetype) : LuaValue.NIL;
            }
        }
        return super.get(key);
    }

    public static RequestBody createRequestBody(MediaType contentType, UploadData file) {
        return new RequestBody() {
            @Nullable
            @Override
            public MediaType contentType() {
                return contentType;
            }

            @Override
            public long contentLength() {
                return file.size();
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                Source source = null;
                try {
                    source = Okio.source(file.open());
                    sink.writeAll(source);
                } finally {
                    Util.closeQuietly(source);
                }
            }
        };
    }

    public static RequestBody createRequestBody(MediaType contentType, LuaUserUploadData file) {
        return createRequestBody(contentType, file.getUploadData());
    }

    public static RequestBody createRequestBody(LuaUserUploadData file) {
        String mimetype = file.getUploadData().getMimeType();
        return createRequestBody(mimetype != null ? MediaType.parse(mimetype) : null, file);
    }

}
