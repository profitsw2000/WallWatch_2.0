package ru.profitsw2000.wallwatch_20;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

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
        bluetoothOff();                          //обработка нажатия кнопки выключения bluetooth
        listPairedDevices();                    //вывести список парных устройств
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
                    }
                        stateOnPressed();
                }
            }
        });
    }

    /**
     * Выключение bluetooth
     */
    private void bluetoothOff() {
        btn_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stateOnStart();
                if (myBluetoothAdapter.isEnabled()) {
                    myBluetoothAdapter.disable()    ;
                    Toast.makeText(getApplicationContext(),"Bluetooth is disabled!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Проверка результата действий пользователя на запрос о включении Bluetooth
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == requestCodeForEnable)
        {
            if(resultCode == RESULT_OK)
            {
                Toast.makeText(getApplicationContext(),"Bluetooth is enabled", Toast.LENGTH_LONG).show();
                stateOnPressed();
            }
            else if (resultCode == RESULT_CANCELED)
            {
                Toast.makeText(getApplicationContext(),"Bluetooth enabling cancelled", Toast.LENGTH_LONG).show();
                stateOnStart();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Показать список парных устройств.
     */
    private void listPairedDevices() {
        listDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Set<BluetoothDevice> bt = myBluetoothAdapter.getBondedDevices() ;
                String[] strings = new String[bt.size()]    ;
                btArray = new BluetoothDevice[bt.size()]    ;
                int index = 0   ;

                if (bt.size() > 0)
                {
                    for (BluetoothDevice device : bt)
                    {
                        btArray[index] = device ;
                        strings[index] = device.getName()   ;
                        index++ ;
                    }

                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, strings) ;
                    listView.setAdapter(arrayAdapter);
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
