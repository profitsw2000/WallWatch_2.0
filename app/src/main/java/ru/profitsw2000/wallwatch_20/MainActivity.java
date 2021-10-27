package ru.profitsw2000.wallwatch_20;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {


    static final int STATE_LISTENING = 1    ;
    static final int STATE_CONNECTING = 2   ;
    static final int STATE_CONNECTED = 3    ;
    static final int STATE_CONNECTION_FAILED = 4    ;
    private static final String APP_NAME = "BTChat"    ;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") ;

    private Timer myTimer    ;
    private SendReceive sendReceive ;

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
        pickupDevice();                         //обработка нажатия пользователем элемента из списка
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


    private void pickupDevice() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                listView.setVisibility(View.GONE);
                btn_update.setEnabled(true);
                ClientClass clientClass = new ClientClass(btArray[i])   ;
                clientClass.start() ;
                status.setText("Connecting");
            }
        });
    }

    /**
     * Метод для возврата сообщения при запросе
     */
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what)
            {
                case STATE_LISTENING:
                    status.setText("Listening");
                    break;
                case STATE_CONNECTING:
                    status.setText("Connecting");
                    break   ;
                case STATE_CONNECTED:
                    status.setText("Connected");
                    break;
                case STATE_CONNECTION_FAILED:
                    status.setText("Connection failed");
                    break;
            }
            return true;
        }
    });

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

    private class ClientClass extends  Thread{
        private BluetoothDevice device  ;
        private BluetoothSocket socket  ;

        public ClientClass(BluetoothDevice device1)
        {
            device = device1    ;

            try {
                socket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID)  ;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run()
        {
            try {
                socket.connect();
                Message message = Message.obtain()  ;
                message.what = STATE_CONNECTED  ;
                handler.sendMessage(message)    ;
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
                Message message = Message.obtain()  ;
                message.what = STATE_CONNECTION_FAILED  ;
                handler.sendMessage(message)    ;
            }
        }
    }

    private class SendReceive extends Thread
    {
        private final BluetoothSocket bluetoothSocket ;
        private final InputStream inputStream   ;
        private final OutputStream outputStream ;

        public SendReceive (BluetoothSocket socket)
        {
            bluetoothSocket = socket    ;
            InputStream tempIn = null   ;
            OutputStream tempOut = null ;

            try {
                tempIn = bluetoothSocket.getInputStream()   ;
                tempOut = bluetoothSocket.getOutputStream() ;
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream = tempIn    ;
            outputStream = tempOut  ;
        }

        public void run()
        {
            byte[] buffer = new  byte[1024] ;
            int bytes   ;

            while (true)
            {
                try {
                    bytes = inputStream.read(buffer)    ;
                    //handler.obtainMessage(STATE_MESSAGE_RECEIVED,bytes,-1,buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes)
        {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void write_byte(byte bytes)
        {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
