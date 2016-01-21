package com.divarc.music365;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

import com.divarc.music365.entity.Datachannel;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class DataLoader extends AsyncTaskLoader<Datachannel> {

    private static String MESSAGE_ID;
    Context context;
    SharedPreferences sharedPreferences;
    private String url;
    Handler handler;

    public DataLoader(Context context, Handler handler, String url) {
        super(context);
        this.context = context;
        this.handler = handler;
        this.url = url;
    }


    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        //sharedPreferences = context.getSharedPreferences(ProgrammLoader.MESSAGE_ID, Context.MODE_PRIVATE);
        forceLoad();
    }


    @Override
    public Datachannel loadInBackground() {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = null;

        String xmlData = null;
        try {
            response = client.newCall(request).execute();
            xmlData = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Serializer serializer = new Persister();
        Reader reader = new StringReader(xmlData);
        Datachannel datachannel = null;

        try {
            datachannel = serializer.read(Datachannel.class, reader, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datachannel;
    }
}


