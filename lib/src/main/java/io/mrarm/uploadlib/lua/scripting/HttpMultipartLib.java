package io.mrarm.uploadlib.lua.scripting;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaUserdata;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class HttpMultipartLib extends TwoArgFunction {

    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaTable table = new LuaTable();
        table.set("body", new body());
        table.set("formData", new formData());

        table.set("MIXED", new LuaUserdata(MultipartBody.MIXED));
        table.set("ALTERNATIVE", new LuaUserdata(MultipartBody.ALTERNATIVE));
        table.set("DIGEST", new LuaUserdata(MultipartBody.DIGEST));
        table.set("PARALLEL", new LuaUserdata(MultipartBody.PARALLEL));
        table.set("FORM", new LuaUserdata(MultipartBody.FORM));
        env.get("http").set("multipart", table);
        return table;
    }

    final class body extends OneArgFunction {
        public LuaValue call(LuaValue data) {
            MultipartBody.Builder builder = new MultipartBody.Builder();
            if (data.get("type").isuserdata(MediaType.class))
                builder.setType((MediaType) data.get("type").touserdata());
            else if (data.get("type").isstring())
                builder.setType(MediaType.parse(data.get("type").checkjstring()));
            for (int i = 1; ; i++) {
                LuaValue val = data.get(i);
                if (val.isnil())
                    break;
                builder.addPart((MultipartBody.Part) val.checkuserdata(MultipartBody.Part.class));
            }
            return new LuaUserdata(builder.build());
        }
    }

    final class formData extends ThreeArgFunction {
        public LuaValue call(LuaValue name, LuaValue luaFilename, LuaValue body) {
            String filename;
            if (body.isnil()) {
                body = luaFilename;
                filename = null;
                if (body instanceof LuaUserUploadData)
                    filename = ((LuaUserUploadData) body).getUploadData().getName();
            } else {
                filename = luaFilename.isnil() ? null : luaFilename.checkjstring();
            }
            if (body instanceof LuaUserUploadData) {
                return new LuaUserdata(MultipartBody.Part.createFormData(
                        name.checkjstring(), filename,
                        LuaUserUploadData.createRequestBody((LuaUserUploadData) body)));
            } else if (body.isuserdata(RequestBody.class)) {
                return new LuaUserdata(MultipartBody.Part.createFormData(
                        name.checkjstring(), filename, (RequestBody) body.checkuserdata()));
            } else if (body.isstring() && filename == null) {
                return new LuaUserdata(MultipartBody.Part.createFormData(name.checkjstring(),
                        body.checkjstring()));
            }
            throw new LuaError("Bad body value");
        }
    }

}
