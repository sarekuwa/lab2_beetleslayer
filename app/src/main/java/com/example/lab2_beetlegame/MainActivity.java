package com.example.lab2_beetlegame;
import android.content.pm.ActivityInfo;
import android.media.SoundPool;
import android.view.Window;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    public enum Status {
        ALIVE,
        DEAD
    }

    SoundPool mSoundPool;
    int mSoundId;

    TextView playername;
    TextView gamescore;
    TextView difficultytext;

    public int quantity;
    public Bitmap mBitmap;
    Timer timer;

    //Чтобы приложение не вылетало, обнуляем кол-во жуков и пауков.
    @Override
    protected void onPause() {
        quantity = 0;
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Звуки нажатий по жукам
        mSoundPool = new SoundPool.Builder()
                .setMaxStreams(10)
                .build();
        mSoundId = mSoundPool.load(this, R.raw.death, 1);

        //Инициализируем экран
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        //Получаем все поля по id из activity_main.xml
        playername =(TextView) findViewById(R.id.playername);
        gamescore = (TextView) findViewById(R.id.gamescore);
        difficultytext = (TextView) findViewById(R.id.difficultytext);

        //Получаем данные из StartActivity
        Intent intent = getIntent();
        //Имя игрока
        String entername = intent.getStringExtra("entername");
        playername.setText(entername);
        //Сложность
        String difficulty = intent.getStringExtra("difficulty");
        difficultytext.setText(difficulty);

        final Integer[] position = {0};
        timer = new Timer();
        final Point point = new Point();
        this.getWindow().getWindowManager().getDefaultDisplay().getSize(point);

        mBitmap = Bitmap.createBitmap(42, 42,
                Bitmap.Config.ARGB_8888);
        mBitmap.setPixel(40, 40, 123);

        //Проигрывать фоновый звук bugs.mp3
        startService(new Intent(MainActivity.this, SoundService.class));

        //Инициализируем жуков и пауков
        final List<Bug> Bugs = new ArrayList<>();
        final List<Spider> Spiders = new ArrayList<>();

            //Выбор сложности  (количества жуков/пауков) кол-во x2
            switch(difficulty) {
                case "normal":
                    quantity = 10;
                    break;
                case "nightmare":
                    quantity = 20;
                    break;
                case "hell":
                    quantity = 25;
                    break;
            }

        for (int i = 0; i < quantity; i++) {
            Bugs.add(new Bug(point, this));
            Spiders.add(new Spider(point, this));
        }

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                for (int i = 0; i < quantity; i++) {
                    Bugs.get(i).animate();
                    Spiders.get(i).animate();
                }
            }
        };

        timer.schedule(timerTask, 25, 25);
    }

    //Остановка фонового звука при закрытии приложения
    protected void onDestroy() {
        stopService(new Intent(MainActivity.this, SoundService.class));
        super.onDestroy();
    }

    public class Position {
        private int x;
        private int y;
        private int angle;

        Position(int x, int y, int angle) {
            this.x = x;
            this.y = y;
            this.angle = angle;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getAngle() {
            return angle;
        }
    }

    private class MovingPattern {
        private int tick = 0;

        public Position move() {
            tick++;
            return action();
        }

        protected Position action() {
            return new Position(0, 0, 0);
        }

        public MovingPattern notifyEdge(Edges edge) {
            if (edge == Edges.TOP) {
                reachedTopEdge();
            } else if (edge == Edges.RIGHT) {
                reachedRightEdge();
            } else if (edge == Edges.BOTTOM) {
                reachedBottomEdge();
            } else if (edge == Edges.LEFT) {
                reachedLeftEdge();
            }

            return this;
        }

        protected void reachedTopEdge() {
            throw new UnsupportedOperationException();
        }

        protected void reachedBottomEdge() {
            throw new UnsupportedOperationException();
        }

        protected void reachedRightEdge() {
            throw new UnsupportedOperationException();
        }

        protected void reachedLeftEdge() {
            throw new UnsupportedOperationException();
        }
    }

    private class SimpleMovingPattern extends MovingPattern {
        protected int directionX = 1;
        protected int directionY = 1;
        protected int angle = 135;

        @Override
        protected Position action() {
            return new Position(directionX, directionY, angle);
        }

        private void calculateAngle() {
            if (directionX > 0 && directionY > 0) {
                angle = 135;
            } else if (directionX > 0 && directionY < 0) {
                angle = 45;
            } else if (directionX < 0 && directionY > 0) {
                angle = 210;
            } else {
                angle = 315;
            }
        }

        @Override
        public MovingPattern notifyEdge(Edges edge) {
            return super.notifyEdge(edge);
        }

        @Override
        protected void reachedTopEdge() {
            directionY *= -1;
            calculateAngle();
        }

        @Override
        protected void reachedRightEdge() {
            directionX *= -1;
            calculateAngle();
        }

        @Override
        protected void reachedBottomEdge() {
            directionY *= -1;
            calculateAngle();
        }

        @Override
        protected void reachedLeftEdge() {
            directionX *= -1;
            calculateAngle();
        }
    }

    public enum Edges {TOP, BOTTOM, RIGHT, LEFT};
    //Счёт игры
    public int score;


    //Майские жуки
    private class Bug {

        private ImageView imageView;
        private Integer originalWidth;
        private Integer originalHeight;
        private Float renderedAngle;
        private int directionX = 1;
        private int directionY = 1;
        private Float movingAngle;
        private Status status;
        private float positionX;
        private float positionY;
        private int speed;
        private Point screenDimensions;
        private MovingPattern movingPattern;

        Bug(Point screenDimensions, Context context) {
            this.screenDimensions = screenDimensions;
            initImage(context);
            this.positionX = (int) ((Math.random() * 100000) % (this.screenDimensions.x - originalWidth));
            this.positionY = (int) ((Math.random() * 100000) % (this.screenDimensions.y - originalHeight));
            movingPattern = new SimpleMovingPattern();
            status = Status.ALIVE;
            speed = (int) (Math.random() * 20) % 10;
            this.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imageView.setImageResource(R.drawable.blood); //Анимация крови
                    status = Status.DEAD;
                    v.setVisibility(View.INVISIBLE);
                    score += 10 + Math.random() * 20;
                    gamescore.setText(String.valueOf(score));
                    mSoundPool.play(mSoundId, 1, 1, 1, 0, 1);
                }
            });
        }

        protected void initImage(Context context) {
            imageView = new ImageView(context);
            imageView.setImageResource(R.drawable.bugvs);
            RelativeLayout relativeLayout = findViewById(R.id.layout);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );

            BitmapFactory.Options dimensions = new BitmapFactory.Options();
            dimensions.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round, dimensions);
            originalHeight = dimensions.outHeight;
            originalWidth = dimensions.outWidth;
            relativeLayout.addView(imageView, layoutParams);
        }

        void animate() {
            float newX = positionX;
            float newY = positionY;

            Position newSpeed = movingPattern.move();
            newX = newX + newSpeed.getX() * directionX * speed;
            newY = newY + newSpeed.getY() * directionY * speed;
            renderedAngle = (float) newSpeed.getAngle();

            boolean reachedEdge = false;
            if (newY < 0) {
                reachedEdge = true;
                movingPattern.notifyEdge(Edges.TOP);
            }

            if (newX > screenDimensions.x - originalWidth) {
                reachedEdge = true;
                movingPattern.notifyEdge(Edges.RIGHT);
            }

            if (newY > screenDimensions.y - originalHeight) {
                reachedEdge = true;
                movingPattern.notifyEdge(Edges.BOTTOM);
            }

            if (newX < 0) {
                reachedEdge = true;
                movingPattern.notifyEdge(Edges.LEFT);
            }

            if (reachedEdge) {
                newSpeed = movingPattern.move();
                newX = newX + newSpeed.getX() * directionX * speed;
                newY = newY + newSpeed.getY() * directionY * speed;
                renderedAngle = (float) newSpeed.getAngle();
            }

            this.positionX = newX;
            this.positionY = newY;
            imageView.setX((int) newX);
            imageView.setY((int) newY);
            imageView.setRotation(renderedAngle);
        }
    }
    //Тарантулы (пауки)
    private class Spider {
        private ImageView imageView;
        private Integer originalWidth;
        private Integer originalHeight;
        private Float renderedAngle;
        private int directionX = 1;
        private int directionY = 1;
        private float positionX;
        private float positionY;
        private int speed;
        private Float movingAngle;
        private Status status;

        private Point screenDimensions;
        private MovingPattern movingPattern;

        Spider(Point screenDimensions, Context context) {
            this.screenDimensions = screenDimensions;
            initImage(context);
            this.positionX = (int) ((Math.random() * 200000) % (this.screenDimensions.x - originalWidth));
            this.positionY = (int) ((Math.random() * 200000) % (this.screenDimensions.y - originalHeight));
            movingPattern = new SimpleMovingPattern();
            status = Status.ALIVE;
            speed = (int) (Math.random() * 20) % 30;
            this.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    imageView.setImageResource(R.drawable.blood); //Анимация крови
                    status = Status.DEAD;
                    v.setVisibility(View.INVISIBLE);
                    score += 10 + Math.random() * 20;
                    gamescore.setText(String.valueOf(score));
                    mSoundPool.play(mSoundId, 1, 1, 1, 0, 1);
                }
            });
        }



        protected void initImage(Context context) {
            imageView = new ImageView(context);
            imageView.setImageResource(R.drawable.spider);
            RelativeLayout relativeLayout = findViewById(R.id.layout);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );

            BitmapFactory.Options dimensions = new BitmapFactory.Options();
            dimensions.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round, dimensions);
            originalHeight = dimensions.outHeight;
            originalWidth = dimensions.outWidth;
            relativeLayout.addView(imageView, layoutParams);
        }

        void animate() {
            float newX = positionX;
            float newY = positionY;

            Position newSpeed = movingPattern.move();
            newX = newX + newSpeed.getX() * directionX * speed;
            newY = newY + newSpeed.getY() * directionY * speed;
            renderedAngle = (float) newSpeed.getAngle();

            boolean reachedEdge = false;
            if (newY < 0) {
                reachedEdge = true;
                movingPattern.notifyEdge(Edges.TOP);
            }

            if (newX > screenDimensions.x - originalWidth) {
                reachedEdge = true;
                movingPattern.notifyEdge(Edges.RIGHT);
            }

            if (newY > screenDimensions.y - originalHeight) {
                reachedEdge = true;
                movingPattern.notifyEdge(Edges.BOTTOM);
            }

            if (newX < 0) {
                reachedEdge = true;
                movingPattern.notifyEdge(Edges.LEFT);
            }

            if (reachedEdge) {
                newSpeed = movingPattern.move();
                newX = newX + newSpeed.getX() * directionX * speed;
                newY = newY + newSpeed.getY() * directionY * speed;
                renderedAngle = (float) newSpeed.getAngle();
            }

            this.positionX = newX;
            this.positionY = newY;
            imageView.setX((int) newX);
            imageView.setY((int) newY);
            imageView.setRotation(renderedAngle);

        }
    }
}
