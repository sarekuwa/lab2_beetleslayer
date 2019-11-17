package com.example.lab2_beetlegame;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class Startmenu extends AppCompatActivity implements OnClickListener {


    //Player name
    EditText entername;

    //Difficulty
    public String difficulty;

    public void onCreate(Bundle savedInstanceState) {

        //Set layout instance (Инициализируем экран)
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_startmenu);

        //Music service
        startService(new Intent(Startmenu.this, SoundServiceStartMenu.class));

        //Ввод имени игрока
        entername = (EditText) findViewById(R.id.entername);

        //Инициализируем кнопки
        Button startButton = (Button)findViewById(R.id.button);
        startButton.setOnClickListener(this);

        Button exitButton = (Button)findViewById(R.id.button2);
        exitButton.setOnClickListener(this);

        Button normal = (Button)findViewById(R.id.normal);
        normal.setOnClickListener(this);

        Button nightmare = (Button)findViewById(R.id.nightmare);
        nightmare.setOnClickListener(this);

        Button hell = (Button)findViewById(R.id.hell);
        hell.setOnClickListener(this);
    }

    /** Обработка нажатия кнопок */
    public void onClick(View v) {
        switch (v.getId()) {
            //переход на MainActivity
            case R.id.button: {
                Intent intent = new Intent();
                intent.setClass(this, MainActivity.class);
                //Отправляем данные в MainActivity
                intent.putExtra("entername", entername.getText().toString());
                intent.putExtra("difficulty", difficulty);
                startActivity(intent);
            }
            break;

            case R.id.normal: {
                difficulty = "normal";
            }break;

            case R.id.nightmare: {
                difficulty = "nightmare";
            }break;

            case R.id.hell: {
                difficulty = "hell";
            }break;

            //выход
            case R.id.button2: {
                finish();
            }
            break;


            default:
                break;
        }
    }
    //Остановка фонового звука при закрытии приложения
    protected void onDestroy() {
        stopService(new Intent(Startmenu.this, SoundServiceStartMenu.class));
        super.onDestroy();
    }

    protected void onPause() {
        stopService(new Intent(Startmenu.this, SoundServiceStartMenu.class));
        super.onPause();
    }

    protected void onResume() {
        startService(new Intent(Startmenu.this, SoundServiceStartMenu.class));
        super.onResume();
    }

}