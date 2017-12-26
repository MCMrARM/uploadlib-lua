package io.mrarm.uploadlib.lua.scripting;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaUserdata;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

import java.io.IOException;
import java.io.StringWriter;

import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpLib extends TwoArgFunction {

    private static final MediaType MEDIA_TYPE_JSON =
            MediaType.parse("application/json; charset=utf-8");

    private static final ConnectionPool connectionPool = new ConnectionPool();

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectionPool(connectionPool)
            .cache(null)
            .build();

    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaTable table = new LuaTable();
        table.set("request", new request(null));
        table.set("get", new request("GET"));
        table.set("head", new request("HEAD"));
        table.set("post", new request("POST"));
        table.set("delete", new request("DELETE"));
        table.set("put", new request("PUT"));
        table.set("patch", new request("PATCH"));

        table.set("body", new body());
        table.set("jsonBody", new jsonBody());
        env.set("http", table);
        return table;
    }

    private void processHeaders(Request.Builder requestBuilder, LuaTable headers) {
        LuaValue k = LuaValue.NIL;
        while (true) {
            Varargs n = headers.next(k);
            if ((k = n.arg1()).isnil())
                break;
            String headerName = k.checkjstring();
            requestBuilder.removeHeader(headerName);
            LuaValue v = n.arg(2);
            if (v.isstring()) {
                requestBuilder.addHeader(headerName, v.checkjstring());
            } else if (v.istable()) {
                LuaValue k2 = LuaValue.NIL;
                while (true) {
                    Varargs n2 = v.next(k2);
                    if ((k2 = n2.arg1()).isnil())
                        break;
                    requestBuilder.addHeader(headerName, n2.arg(2).checkjstring());
                }
            }
        }
    }

    private Request makeRequest(String method, LuaTable t) {
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(t.get("url").checkjstring());
        RequestBody requestBody = null;
        if (t.get("body").isuserdata(RequestBody.class))
            requestBody = (RequestBody) t.get("body").checkuserdata();
        requestBuilder.method(method, requestBody);
        if (t.get("headers").istable())
            processHeaders(requestBuilder, t.get("headers").checktable());
        return requestBuilder.build();
    }

    final class request extends OneArgFunction {
        String method;
        request(String method) {
            this.method = method;
        }
        public LuaValue call(LuaValue i) {
            LuaTable t = i.checktable();
            String method = this.method;
            if (method == null)
                method = t.get("method").checkjstring();
            Request request = makeRequest(method, t);
            try {
                Response response = client.newCall(request).execute();
                return new HttpResponseWrapper(response);
            } catch (IOException e) {
                throw new LuaError("IO Error: " + e.getMessage());
            }
        }
    }

    final class body extends TwoArgFunction {
        public LuaValue call(LuaValue mediaType, LuaValue data) {
            if (data.isnil() && mediaType instanceof LuaUserUploadData) // Single argument version
                return new LuaUserdata(LuaUserUploadData.createRequestBody((LuaUserUploadData) mediaType));

            MediaType mt = MediaType.parse(mediaType.checkjstring());
            RequestBody requestBody;
            if (data.isstring())
                requestBody = RequestBody.create(mt, data.checkjstring());
            else if (data instanceof LuaUserUploadData)
                requestBody = LuaUserUploadData.createRequestBody((LuaUserUploadData) data);
            else
                throw new LuaError("Invalid data type");
            return new LuaUserdata(requestBody);
        }
    }

    final class jsonBody extends OneArgFunction {
        public LuaValue call(LuaValue data) {
            try {
                StringWriter writer = new StringWriter();
                JsonGenerator generator = new JsonFactory()
                        .createGenerator(writer);
                JsonLib.serialize(generator, data.checktable());
                generator.close();
                writer.close();
                return new LuaUserdata(RequestBody.create(MEDIA_TYPE_JSON, writer.toString()));
            } catch (IOException e) {
                throw new LuaError("IO Error: " + e.getMessage());
            }
        }
    }

}
