package com.peanutwolf.googleappmonitor;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.peanutwolf.googleappmonitor.Database.ShakeDBContentProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by vigursky on 05.05.2016.
 */
public class ExportDataTestActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mTextURL;
    private Button mButtonSend;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_data_test);

        mTextURL = (TextView) findViewById(R.id.txt_test_url);
        mButtonSend = (Button) findViewById(R.id.btn_send_test_data);

        mButtonSend.setOnClickListener(this);

        sharedPref = getPreferences(Context.MODE_PRIVATE);

        String savedURL = sharedPref.getString(getString(R.string.saved_test_url), "");

        if(!savedURL.equals("")){
            mTextURL.setText(savedURL);
        }
    }

    @Override
    public void onClick(View v) {
        String url = mTextURL.getText().toString();
        if(url.isEmpty()){
            return;
        }else{
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.saved_test_url), url);
            editor.commit();
        }
        mButtonSend.setText("Loading...");
        SendTestDataTask task = new SendTestDataTask();
        task.execute(url);
    }

    class SendTestDataTask extends AsyncTask<String, Void, Boolean>{

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                JSONObject jsonObject = exportShakeData();
                return post(params[0], jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            mButtonSend.setText("Send to Server");
        }
    }

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();


    private JSONObject exportShakeData() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        ContentResolver mContentResolver =  getContentResolver();
        Cursor cursor = mContentResolver.query(ShakeDBContentProvider.CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            JSONObject jsonRecord;
            JSONArray jsonArray = new JSONArray();
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                jsonRecord = new JSONObject();
                for(int i = 0; i < cursor.getColumnCount(); i++){
                    jsonRecord.put(cursor.getColumnName(i), cursor.getString(i));
                }
                jsonArray.put(jsonRecord);
                cursor.moveToNext();
            }
            jsonObject.put("Data", jsonArray);
            jsonObject.put("Version", "1.0");
            cursor.close();
        }

        return jsonObject;
    }

    boolean post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        return response.isSuccessful();
    }
}
