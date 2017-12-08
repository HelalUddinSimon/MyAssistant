package com.ss.simon.myassistant;

import android.Manifest;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import static android.R.attr.button;

public class MainActivity extends AppCompatActivity {

    //private String text = "This is paragraph 1\n\nThis is paragraph 2\n\n";
    private TextToSpeech tts;
    String txt="hello, i am simon. i am here to help you.";
    String[] Speech;
    String smsBodycontent;
    List<PackageInfo> packs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        packs = getPackageManager().getInstalledPackages(0); // for store all the packages that installed in the mobile
//fab button work
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");
                    }
                    speak(txt);

                } else {
                    Log.e("TTS", "Initilization Failed!");
                }
            }
        });

        findViewById(R.id.microphoneButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listen();
            }
        });


    }

    //speak method
    private void speak(String text){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }else{
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    // listing method
    private void listen(){
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");

        try {
            startActivityForResult(i, 100);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(MainActivity.this, "Your device doesn't support Speech Recognition", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100){
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> res = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String inSpeech = res.get(0);
                recognition(inSpeech);
            }
        }
    }

    //  recognition of text string
    private void recognition(String text) {
        Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
        text = text.toLowerCase();
        Log.e("Speech", "" + text);
        String searchTxt = text;
        // txt = "Searching "+text;
        // speak(txt);
        Speech = text.split(" ");

        if (text.contains("send") && text.contains("sms") || text.contains("mobile")) {
            String name = Speech[Speech.length - 1];
            String[] smsBody = text.split("send");
            smsBodycontent = smsBody[0];
            //  speak(smsBodycontent);
            sendSMSFunction(name, smsBodycontent);
        } else if (text.contains("open camera") || text.contains("camera")) {
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                intent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
            } else {
                intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
            }
            startActivity(intent);
        }

        else if(text.contains("dial")) {
            String phoneNumber="";
            try {
                for(int i = 1 ; i<Speech.length;i++){
                   String number = Speech[i];
                    phoneNumber = phoneNumber+number;
                }
                Toast.makeText(MainActivity.this,"phone ::"+  phoneNumber, Toast.LENGTH_SHORT).show();
                int REQUEST_PHONE_CALL = 1;
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
                //here the word 'tel' is important for making a call...
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PHONE_CALL);
                } else {
                    startActivity(intent);
                }
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getApplicationContext(), "Error in your phone call" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        else if(text == "1"){

        for (int i = 0; i < packs.size(); i++) {
            PackageInfo p = packs.get(i);

            String appName = p.applicationInfo.loadLabel(getPackageManager()).toString();
            Toast.makeText(MainActivity.this, "App name :: " + appName, Toast.LENGTH_LONG).show();
            if (text.contains(appName)) {
                Toast.makeText(MainActivity.this, "package " + p.applicationInfo.packageName, Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.setClassName("packageName", p.applicationInfo.packageName);
                startActivity(intent);
                break;
            }
        }
    }
      /*  Uri uri = Uri.parse("http://www.google.com/#q="+text);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);*/
    }
    //sending sms function
    private void sendSMSFunction(String name , String smsBody){

        String phoneNumber = getPhoneNumber(name , MainActivity.this);
        speak("sending sms to" +phoneNumber);
        SmsManager sms = SmsManager.getDefault();
        // if message length is too long messages are divided
        List<String> messages = sms.divideMessage(smsBody);
        for (String msg : messages) {

            PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT"), 0);
            PendingIntent deliveredIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED"), 0);
            sms.sendTextMessage(name, null, msg, sentIntent, deliveredIntent);

        }


    }

    // getting phone number from phone contact
    public String getPhoneNumber(String name, Context context) {
        String ret = null;
        String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" like'%" + "chondon" +"%'";
        String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor c = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection, selection, null, null);
        if (c.moveToFirst()) {
            ret = c.getString(0);
        }
        c.close();
        if(ret==null)
            ret = "Unsaved";
        return ret;
    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
