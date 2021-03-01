package com.example.drollbirds;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class GameView extends View {


    private int viewWidth;
    private int viewHeight;
    private int points = 0;
    private int level = 0;
    private boolean running = true;
    private Timer t;

    private Sprite playerBird; //объект птички
    private Sprite enemyBird; // объект вражеской птички
    private Sprite bonus;
    private Sprite karateGuy;
    private Sprite pause;

    private final int timerInterval = 30; //таймер

    public GameView(Context context) {
        super(context);
        //------------------птичка-герой------------------------------------------------------------
        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.player);

        int w = b.getWidth()/5;
        int h = b.getHeight()/3;

        Rect firstFrame = new Rect(0, 0, w, h);

        playerBird = new Sprite(10, 0, 0, 100, firstFrame, b);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }
                if (i == 2 && j == 3) {
                    continue;
                }
                playerBird.addFrame(new Rect(j * w, i * h,
                        j * w + w, i * w + w));
            }
        }
        //------------------птичка-враг-------------------------------------------------------------
        b = BitmapFactory.decodeResource(getResources(), R.drawable.enemy);

        w = b.getWidth()/5;
        h = b.getHeight()/3;

        firstFrame = new Rect(4*w, 0, 5*w, h);

        enemyBird = new Sprite(2000, 250, -300, 0, firstFrame, b);
        for (int i = 0; i < 3; i++) {
            for (int j = 4; j >= 0; j--) {
                if (i ==0 && j == 4) {
                    continue;
                }
                if (i ==2 && j == 0) {
                    continue;
                }
                enemyBird.addFrame(new Rect(j*w, i*h, j*w+w, i*w+w));
            }
        }
        //-------------------бонус------------------------------------------------------------------
        b = BitmapFactory.decodeResource(getResources(), R.drawable.bonus);

        w = b.getWidth();
        h = b.getHeight();

        firstFrame = new Rect(0, 0, w, h);

        bonus = new Sprite(2000, 250, -300, 0, firstFrame, b);
        //------------------кликабельный противник--------------------------------------------------
        b = BitmapFactory.decodeResource(getResources(), R.drawable.karateguy);

        w = b.getWidth()/5;
        h = b.getHeight();

        firstFrame = new Rect(0, 0, w, h);
        karateGuy = new Sprite(2000, 250, -300, 0, firstFrame, b);
        for (int i = 0; i < 5; i++){
            karateGuy.addFrame(new Rect(i*w, 0, i*w+w, h));
        }
        //------------------кнопка паузы------------------------------------------------------------
        b = BitmapFactory.decodeResource(getResources(), R.drawable.pause);

        w = b.getWidth();
        h = b.getHeight();

        firstFrame = new Rect(0, 0, w, h);
        pause = new Sprite(200, 100, 0, 0, firstFrame, b);

        t = new Timer();
        t.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        viewWidth = w;
        viewHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawARGB(250, 127, 199, 255); // заливаем цветом


        playerBird.draw(canvas);
        enemyBird.draw(canvas);
        bonus.draw(canvas);
        karateGuy.draw(canvas);
        pause.draw(canvas);


        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setTextSize(55.0f);
        p.setColor(Color.WHITE);
        canvas.drawText("points: "+points+""+"\nlevel: "+level, 100, 70, p);
    }

    protected void update () { //анимируем
        playerBird.update(timerInterval);
        enemyBird.update(timerInterval);
        bonus.update(timerInterval);
        karateGuy.update((int) (timerInterval*1.5));

        if (playerBird.getY() + playerBird.getFrameHeight() > viewHeight) {
            playerBird.setY(viewHeight - playerBird.getFrameHeight());
            playerBird.setVy(-playerBird.getVy());
            points--;
        }
        else if (playerBird.getY() < 0) {
            playerBird.setY(0);
            playerBird.setVy(-playerBird.getVy());
            points--;
        }

        if (enemyBird.getX() < - enemyBird.getFrameWidth()) {
            teleportEnemy();
            points +=70;
        }

        if (enemyBird.intersect(playerBird)) {
            teleportEnemy();
            points -= 40;
        }

        if (bonus.getX() < -bonus.getFrameWidth()) {
            teleportBonus();
        }

        if (karateGuy.getX() < - karateGuy.getFrameWidth()) {
            teleportkarateGuy();
        }


        if (bonus.intersect(playerBird)) {
            teleportBonus();
            points += 40;
        }

        if (points >= 150){
            level++; points = 0;
            enemyBird.setVx(enemyBird.getVx()*1.5);
        }

        if (points <= -100){
            t.cancel();
            Toast toast = Toast.makeText(getContext(), "Вы проиграли", Toast.LENGTH_LONG);
            toast.show();
        }
        invalidate();
    }

    private void teleportEnemy () {
        enemyBird.setX(viewWidth + Math.random() * 500);
        enemyBird.setY(Math.random() * (viewHeight - enemyBird.getFrameHeight()));
    }

    private void teleportBonus () {
        bonus.setX(viewWidth + Math.random() * 500);
        bonus.setY(Math.random() * (viewHeight - bonus.getFrameHeight()));
    }
    private void teleportkarateGuy() {
        karateGuy.setX(viewWidth + Math.random() * 500);
        karateGuy.setY(Math.random() * (viewHeight - karateGuy.getFrameHeight()));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int eventAction = event.getAction();

        if (eventAction == MotionEvent.ACTION_DOWN)  {
            // Движение вверх
            if (event.getY() < playerBird.getBoundingBoxRect().top) {
                playerBird.setVy(-100);
                //points--;
            }
            else if (event.getY() > (playerBird.getBoundingBoxRect().bottom)) {
                playerBird.setVy(100);
                //points--;
            }
            if(pause.isCollition(event.getX(), event.getY()) && running){ // если кликаем на паузу и игра запущена
                t.cancel(); // то останавливаем таймер
                running = false; // и устанавливаем флаг запуска игры на ложь
            } else if(pause.isCollition(event.getX(), event.getY()) && !(running)){ // если кликаем на паузу и игра остановлена
                t.start(); // то запускаем таймер
                running = true; // и устанавливаем флаг запуска игры на истину
            }
        }
        if(karateGuy.isCollition(event.getX(), event.getY())){ // если кликаем в пределах его фрейма
            points += 20; // то прибавляем очки
            teleportkarateGuy(); // и телепортируем его
        }
        return true;
    }

    class Timer extends CountDownTimer {
        public Timer() {
            super(Integer.MAX_VALUE, timerInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            update ();
        }

        @Override
        public void onFinish() {
        }
    }
}


