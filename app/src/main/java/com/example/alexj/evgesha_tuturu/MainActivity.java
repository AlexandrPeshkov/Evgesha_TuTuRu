package com.example.alexj.evgesha_tuturu;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.IOException;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private int mStreamID;
    private SoundPool mSoundPool;
    private AssetManager mAssetManager;
    private WebView mButton;
    private TextView counterTextView;
    private int mCounter=0;
    private  int mSound;
    private  int mWoody;
    private  int mLife;
    private  SharedPreferences myPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myPreferences
                = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        mCounter = myPreferences.getInt("Counter",0);
        // найдем View-элементы
        mButton = findViewById(R.id.faceButton);

        counterTextView = (TextView) findViewById(R.id.textCounter);

        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
           // Для устройств до Android 5
           createOldSoundPool();
      } else {
          // Для новых устройств
          createNewSoundPool();
        }
        mAssetManager = getAssets();
        mSound = loadSound("sound.ogg");
        mWoody = loadSound("woody.ogg");
        mLife = loadSound("life.ogg");
        updateCounter();
        changeImage("face.png");

        mButton.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                int eventAction = event.getAction();
                if (eventAction == MotionEvent.ACTION_UP) {
                    // Отпускаем палец
                    if (mStreamID > 0)
                        mSoundPool.stop(mStreamID);
                    changeImage("face.png");
                }
                if (eventAction == MotionEvent.ACTION_DOWN) {

                    mCounter++;
                    updateCounter();
                    // Нажимаем на кнопку
                    Random random = new Random();
                    int value = random.nextInt(100);
                    if(value > 15) {
                        changeImage("sound_face.png");
                        mStreamID = playSound(mSound);
                    }
                    else {
                        if (value > 5) {
                            changeImage("sound_face.png");
                            mStreamID = playSound(mWoody);
                            setAnimation(R.anim.bounce, true);
                        }
                        else {
                           changeImage("anim_dance.gif");
                            mStreamID = playSound(mLife);
                            //setAnimation();
                        }
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    mSoundPool.stop(mStreamID);
                }
                return true;
            }
        });

    }



    private void setAnimation(int animation, boolean isInterpolated){
        Animation myAnim = AnimationUtils.loadAnimation(this, animation);
        // Use bounce interpolator with amplitude 0.2 and frequency 20
        if(isInterpolated) {
            MyBounceInterpolator interpolator = new MyBounceInterpolator(0.2, 20);
            myAnim.setInterpolator(interpolator);
        }
        mButton.startAnimation(myAnim);
    }

    private  void changeImage(String data){
        //mButton.setBackgroundResource(backgroundImage);

        mButton.loadDataWithBaseURL("file:///android_asset/", "<img src='"+data+"' />", "text/html", "utf-8", null);
    }

    private void updateCounter(){

        myPreferences.edit();
        SharedPreferences.Editor myEditor = myPreferences.edit();
        myEditor.putInt("Counter", mCounter);
        myEditor.commit();
        if(counterTextView!=null){
            counterTextView.setText(String.valueOf(mCounter));
        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void createNewSoundPool() {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        mSoundPool = new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .build();
    }

    @SuppressWarnings("deprecation")
    private void createOldSoundPool() {
        mSoundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
    }

    private int playSound(int sound) {
        if (sound > 0) {
            mStreamID = mSoundPool.play(sound, 1, 1, 1, 0, 1);
        }
        return mStreamID;
    }

    private int loadSound(String fileName) {
        AssetFileDescriptor afd;
        try {
            afd = mAssetManager.openFd(fileName);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Не могу загрузить файл " + fileName,
                    Toast.LENGTH_SHORT).show();
            return -1;
        }
        return mSoundPool.load(afd, 1);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // Для устройств до Android 5
            createOldSoundPool();
        } else {
            // Для новых устройств
            createNewSoundPool();
        }

        mAssetManager = getAssets();

        // получим идентификаторы
        mSound = loadSound("sound.ogg");
        mWoody = loadSound("woody.ogg");
        mLife =  loadSound("life.ogg");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSoundPool.release();
        mSoundPool = null;
    }
}

class MyBounceInterpolator implements android.view.animation.Interpolator {
    private double mAmplitude = 0.9;
    private double mFrequency = 100;

    MyBounceInterpolator(double amplitude, double frequency) {
        mAmplitude = amplitude;
        mFrequency = frequency;
    }

    public float getInterpolation(float time) {
        return (float) (-1 * Math.pow(Math.E, -time/ mAmplitude) *
                Math.cos(mFrequency * time) + 1);
    }
}