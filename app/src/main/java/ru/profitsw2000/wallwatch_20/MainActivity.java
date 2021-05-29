package ru.profitsw2000.wallwatch_20;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    static int counter = 0  ;
    private Timer myTimer    ;

    private Button btn_on, btn_off, listDevices, btn_update ;
    private ListView listView   ;
    private TextView status   ;
    private BluetoothAdapter myBluetoothAdapter ;
    private BluetoothDevice[] btArray   ;

    private Intent btEnablingIntent ;
    int requestCodeForEnable    ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()   ;
        btEnablingIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)   ;
        requestCodeForEnable = 1    ;

        displayUpdatedTime();                   //запуск отображения времени и даты
        findViewByIdes();                       //присваивание переменным элементов активити
        stateOnStart();                         //установка начального состояния элементов активити
        bluetoothOn();                          //обработка нажатия кнопки включения bluetooth
    }

    /**
     * Метод запускает таймер, по которому обновляется время на дисплее
     */
    private void displayUpdatedTime() {
        myTimer = new Timer()    ;
        MyTimerTask myTimerTask = new MyTimerTask();
        myTimer.schedule(myTimerTask, 0, 300);
    }

    /**
     * Метод, связывающий переменные с элементами активити
     */
    private void findViewByIdes() {
        btn_on = (Button) findViewById(R.id.BT_ON)  ;
        btn_off = (Button) findViewById(R.id.BT_OFF)    ;
        btn_update = (Button) findViewById(R.id.update) ;
        listDevices = (Button) findViewById(R.id.listDevices)   ;
        listView = (ListView) findViewById(R.id.listView)   ;
        status = (TextView) findViewById(R.id.bt_status)    ;
    }

    /**
     * Метод устанавливает начальные значения элементов активити
     */
    private void stateOnStart() {
        btn_on.setEnabled(true) ;
        btn_off.setEnabled(false)   ;
        listDevices.setEnabled(false)   ;
        listView.setVisibility(View.VISIBLE)    ;
        btn_update.setEnabled(false);
        listView.setAdapter(null);
    }

    /**
     * Метод устанавливает значения элементов активити после нажатия кнопки выключения
     */
    private void stateOnPressed() {
        btn_on.setEnabled(false) ;
        btn_off.setEnabled(true)   ;
        listDevices.setEnabled(true)   ;
        listView.setVisibility(View.VISIBLE)    ;
        btn_update.setEnabled(false);
    }

    /**
     * Метод для обработки нажатия кнопки включения блютуз
     */
    private void bluetoothOn() {
        btn_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myBluetoothAdapter == null) {
                    Toast.makeText(getApplicationContext(),"Bluetooth does not support on this device!", Toast.LENGTH_LONG).show();
                }
                else {
                    if (!myBluetoothAdapter.isEnabled()) {
                        startActivityForResult(btEnablingIntent, requestCodeForEnable);
                        stateOnPressed();
                        //тут надо будет доработать код
                    }
                    else {
                        stateOnPressed();
                    }
                }
            }
        });
    }

    /**
     * Класс, имплементирующий таймер.
     */
    class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Calendar calendar = Calendar.getInstance()    ;
                    Locale locale = new Locale("ru")    ;
                    SimpleDateFormat format_time = new SimpleDateFormat("HH:mm:ss", locale)   ;
                    SimpleDateFormat format_date = new SimpleDateFormat("dd.MM E", locale)    ;
                    String time = format_time.format(calendar.getTime())  ;
                    String date = format_date.format(calendar.getTime())  ;

                    TextView timeView = (TextView) findViewById(R.id.timeText) ;
                    TextView dateView = (TextView) findViewById(R.id.dateText)  ;
                    timeView.setText(time);
                    dateView.setText(date);
                }
            });
        }
    }
}
