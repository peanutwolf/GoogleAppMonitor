package com.peanutwolf.googleappmonitor;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.peanutwolf.googleappmonitor.Database.ShakeDBContentProvider;
import com.peanutwolf.googleappmonitor.Database.ShakeDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
    private Button mButtonSendHost;
    private Button mButtonSaveToDisk;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_data_test);

        mTextURL = (TextView) findViewById(R.id.txt_test_url);
        mButtonSendHost = (Button) findViewById(R.id.btn_send_test_data);
        mButtonSaveToDisk = (Button) findViewById(R.id.btn_save_test_data);

        mButtonSendHost.setOnClickListener(this);
        mButtonSaveToDisk.setOnClickListener(this);

        sharedPref = getPreferences(Context.MODE_PRIVATE);

        String savedURL = sharedPref.getString(getString(R.string.saved_test_url), "");

        if(!savedURL.equals("")){
            mTextURL.setText(savedURL);
        }
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.btn_save_test_data){
            AlertDialog alertDialog = new AlertDialog.Builder(ExportDataTestActivity.this).create();
            alertDialog.setTitle("File export");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            try {
                DateFormat df = new SimpleDateFormat("EEEddMMMHHmmss");
                String filename = df.format(Calendar.getInstance().getTime()) + ".csv";
                exportToFile(filename);
                alertDialog.setMessage("Exported to " + filename);
            } catch (IOException e) {
                e.printStackTrace();
                alertDialog.setMessage("File export failed");
            }finally {
                alertDialog.show();
            }

            return;
        }

        String url = mTextURL.getText().toString();
        if(url.isEmpty()){
            return;
        }else{
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.saved_test_url), url);
            editor.commit();
        }
        mButtonSendHost.setText("Loading...");
        SendTestDataTask task = new SendTestDataTask();
        task.execute(url);
    }

    private void exportToFile(String filename) throws IOException {

        SQLiteDatabase sqldb = new ShakeDatabase(this.getApplicationContext()).getReadableDatabase(); //My Database class
        Cursor c = null;

        try {
            c = sqldb.rawQuery("select * from shake", null);
            int rowcount;
            int colcount;
            BufferedWriter bw = getBufferedWriterStreamToDisk(filename);
            rowcount = c.getCount();
            colcount = c.getColumnCount();
            if (rowcount > 0) {
                c.moveToFirst();
                for (int i = 0; i < colcount; i++) {
                    if (i != colcount - 1) {
                        bw.write(c.getColumnName(i) + ",");
                    } else {
                        bw.write(c.getColumnName(i));
                    }
                }
                bw.newLine();
                for (int i = 0; i < rowcount; i++) {
                    c.moveToPosition(i);
                    for (int j = 0; j < colcount; j++) {
                        if (j != colcount - 1)
                            bw.write(c.getString(j) + ",");
                        else
                            bw.write(c.getString(j));
                    }
                    bw.newLine();
                }
                bw.flush();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            if (sqldb.isOpen()) {
                sqldb.close();
            }
        }

    }

    private BufferedWriter getBufferedWriterStreamToDisk(String filename){
        File dataDirectory = getExternalFilesDir(null).getParentFile();
        // the name of the file to export with
        File saveFile = new File(dataDirectory, filename);
        FileWriter fw = null;
        try {
            fw = new FileWriter(saveFile, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedWriter bw = new BufferedWriter(fw);

        return bw;
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
            mButtonSendHost.setText("Send to Server");
        }
    }

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();


    private JSONObject exportShakeData() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        ContentResolver mContentResolver =  getContentResolver();
        Cursor cursor = mContentResolver.query(ShakeDBContentProvider.CONTENT_SHAKES_URI, null, null, null, null);
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
        boolean isSuccessful = false;
        try{
            RequestBody body = RequestBody.create(JSON, json);
            Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
            Response response = client.newCall(request).execute();
            isSuccessful = response.isSuccessful();
        }catch (java.lang.IllegalArgumentException e){
            e.printStackTrace();
        }
        return isSuccessful;
    }
}
