package com.example.racinggame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    GameSurface gameSurface;
    float accelermeterX;
    MediaPlayer mediaPlayer;
    MediaPlayer soundEffect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameSurface = new GameSurface(this);
        setContentView(gameSurface);

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelorometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(gameSurface, accelorometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

        mediaPlayer = MediaPlayer.create(this, R.raw.background);
        mediaPlayer.start();

        soundEffect = MediaPlayer.create(this, R.raw.hitsound);
    }

    @Override
    protected void onPause(){
        super.onPause();
        gameSurface.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        gameSurface.resume();
    }

    public class EnemyFish{

        float yPosition;
        float xPosition;

        public EnemyFish(float xPosition){
            this.yPosition = -250;
            this.xPosition = xPosition;
        }
        public float getxPosition(){
            return xPosition;
        }

        public float getyPosition() {
            return yPosition;
        }

        public void setyPosition(float yPosition) {
            this.yPosition = yPosition;
        }
    }

    public class GameSurface extends SurfaceView implements Runnable, SensorEventListener {

        Thread gameThread;
        SurfaceHolder holder;
        volatile boolean running = false;
        Bitmap fish;
        Bitmap ocean;
        Bitmap ocean2;
        Bitmap goldfish;
        ArrayList<EnemyFish> enemyFishes;
        ArrayList<Bitmap> enemyBitmaps;

        Rect playerRect;

        int backgroundPlace = 0;
        int backgroundPlace2;

        int displacement=0;
        int enemySpeed = 6;

        int timer = 25;
        boolean isTimerRunning = false;

        String timerFormat;
        Paint paintProperty;

        int screenWidth;
        int screenHeight;
        int centerWidth;
        int fishPlace;

        int score;

        CountDownTimer countDownTimer = new CountDownTimer(61000, 1000) {

            public void onTick(long millisUntilFinished) {
                timerFormat = String.valueOf((millisUntilFinished / 1000));
            }

            public void onFinish() {
                running = false;
                mediaPlayer.stop();
            }

        }.start();


        public GameSurface(Context context) {
            super(context);
            holder=getHolder();
            fish= BitmapFactory.decodeResource(getResources(),R.drawable.fish);
            ocean= BitmapFactory.decodeResource(getResources(), R.drawable.ocean);
            ocean2= BitmapFactory.decodeResource(getResources(), R.drawable.ocean);
            goldfish=BitmapFactory.decodeResource(getResources(), R.drawable.goldfish);

            Display screenDisplay = getWindowManager().getDefaultDisplay();
            Point sizeOfScreen = new Point();
            screenDisplay.getSize(sizeOfScreen);
            screenWidth=sizeOfScreen.x; //=1440
            screenHeight=sizeOfScreen.y;

            centerWidth = screenWidth/2 - fish.getWidth()/2;

            enemyFishes = new ArrayList<>();
            enemyBitmaps = new ArrayList<>();
            enemyFishes.add(new EnemyFish((float)(Math.random()*(screenWidth-goldfish.getWidth()))));

            for(int i = 0; i<enemyFishes.size(); i++){
                enemyBitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.goldfish));
            }

            SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this,accelerometerSensor,sensorManager.SENSOR_DELAY_NORMAL);

            paintProperty= new Paint();
            paintProperty.setTextSize(100);
            paintProperty.setColor(Color.WHITE);

            backgroundPlace2 = - ocean.getHeight();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if(enemySpeed == 6)
                enemySpeed = 12;
            else
                enemySpeed = 6;

            return super.onTouchEvent(event);
        }

        @Override
        public void run() {
            while (running){
                if (!holder.getSurface().isValid())
                    continue;

                Canvas canvas= holder.lockCanvas();

                if(enemyFishes != null){
                    if(enemyFishes.get(enemyFishes.size()-1).getyPosition() == 350 || enemyFishes.get(enemyFishes.size()-1).getyPosition() == 356){
                        enemyFishes.add(new EnemyFish((float)(Math.random()*(screenWidth-goldfish.getWidth()))));
                        enemyBitmaps.add(BitmapFactory.decodeResource(getResources(), R.drawable.goldfish));
                    }
                }

                //draws background
                canvas.drawBitmap(ocean, 0, backgroundPlace , null);
                canvas.drawBitmap(ocean2, 0, backgroundPlace2 , null);
                backgroundPlace2++;
                backgroundPlace++;


                if(isTimerRunning){
                    if(timer == 0){
                        fish= BitmapFactory.decodeResource(getResources(),R.drawable.fish);
                    }
                    else{
                        timer--;
                    }
                }

                //draws player fish
                if(centerWidth + displacement + -accelermeterX*5 > 0 && centerWidth + displacement + -accelermeterX*5 + fish.getWidth() < screenWidth){
                    displacement += -accelermeterX*5;
                }
                else{
                    if(fishPlace<centerWidth){
                        displacement = -centerWidth;
                    }
                    else{
                        displacement = centerWidth;
                    }
                }
                fishPlace = centerWidth + displacement;

                playerRect = new Rect(fishPlace, screenHeight-fish.getHeight()*2, fishPlace + fish.getWidth(), screenHeight - fish.getWidth());

                canvas.drawBitmap(fish, fishPlace ,screenHeight - fish.getHeight()*2,null);

                //draws enemy fish
                for(int i = 0; i< enemyFishes.size(); i++){

                    Rect enemyRect = new Rect((int)enemyFishes.get(i).getxPosition(), (int)enemyFishes.get(i).getyPosition(), (int)enemyFishes.get(i).getxPosition() + goldfish.getWidth(), (int)enemyFishes.get(i).getyPosition() + goldfish.getHeight());

                    if(enemyFishes.get(i).getyPosition()+goldfish.getHeight()>screenHeight){
                        enemyFishes.remove(i);
                        enemyBitmaps.remove(i);
                        i--;
                        score++;

                    }
                    else if (!enemyRect.intersect(playerRect)) {

                        enemyFishes.get(i).setyPosition(enemyFishes.get(i).getyPosition() + enemySpeed);

                        canvas.drawBitmap(enemyBitmaps.get(i), enemyFishes.get(i).getxPosition(), enemyFishes.get(i).yPosition, null);


                    }
                    else{
                        enemyFishes.remove(i);
                        enemyBitmaps.remove(i);
                        i--;
                        soundEffect.start();
                        fish= BitmapFactory.decodeResource(getResources(),R.drawable.damagedfish);
                        isTimerRunning = true;
                        timer = 25;
                    }
                }

                //updates time
                canvas.drawText(timerFormat,50,100,paintProperty);

                //updates score
                canvas.drawText(String.valueOf(score),1300,100,paintProperty);

                holder.unlockCanvasAndPost(canvas);
            }

            paintProperty.setTextSize(150);

            Canvas canvas = holder.lockCanvas();
            canvas.drawBitmap(ocean, 0, 0 , null);
            canvas.drawText("GAME OVER",350,900,paintProperty);
            canvas.drawText("Score: "+score,450,1100,paintProperty);
            holder.unlockCanvasAndPost(canvas);
        }

        public void resume(){
            running=true;
            gameThread=new Thread(this);
            gameThread.start();
        }

        public void pause() {
            running = false;
            while (true) {
                try {
                    gameThread.join();
                } catch (InterruptedException e) {
                }
            }
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            accelermeterX = event.values[0];

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }
}
