package org.ixuan.vrsensor;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.ixuan.vrsensor.packet.AccelerometerPacket;
import org.ixuan.vrsensor.packet.GyroscopePacket;
import org.ixuan.vrsensor.packet.KeyPacket;
import org.ixuan.vrsensor.packet.OrientationPacket;
import org.ixuan.vrsensor.packet.Packet;

import java.io.IOException;
import java.lang.reflect.Constructor;

public class PosterActivity extends AppCompatActivity {

    private SensorEventPoster gyroscopeListener, accelerometerListener, orientationListener;

    private SensorManager sensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poster);

        // 保证不息屏
        // 屏幕关闭将导致部分传感器不再反馈数据
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //获得陀螺仪传感器
        Sensor gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (gyroscope == null) {
            Toast.makeText(this, "无法获取陀螺仪状态", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        //获得加速度传感器
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer == null) {
            Toast.makeText(this, "无法获取加速度传感器状态", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        //获取方向传感器
        Sensor orientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        if (orientation == null) {
            Toast.makeText(this, "无法获取转动角度传感器状态", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //创建时间转发器
        try {
            accelerometerListener = new SensorEventPoster(
                    this,
                    AccelerometerPacket.class,
                    new TextView[]{
                            (TextView) findViewById(R.id.tvAcX),
                            (TextView) findViewById(R.id.tvAcY),
                            (TextView) findViewById(R.id.tvAcZ)
                    }
            );

            gyroscopeListener = new SensorEventPoster(
                    this,
                    GyroscopePacket.class,
                    new TextView[]{
                            (TextView) findViewById(R.id.tvGyX),
                            (TextView) findViewById(R.id.tvGyY),
                            (TextView) findViewById(R.id.tvGyZ)
                    }
            );

            orientationListener = new SensorEventPoster(
                    this,
                    OrientationPacket.class,
                    new TextView[]{
                            (TextView) findViewById(R.id.tvRtX),
                            (TextView) findViewById(R.id.tvRtY),
                            (TextView) findViewById(R.id.tvRtZ)
                    }
            );
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        //注册事件转发器
        sensorManager.registerListener(accelerometerListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(gyroscopeListener, gyroscope, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(orientationListener, orientation, SensorManager.SENSOR_DELAY_UI);

        //启用事件转发器
        gyroscopeListener.setEnabled(true);
        orientationListener.setEnabled(true);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                try {
                    Connection.connection.send(new KeyPacket(true, keyCode));
                } catch (IOException e) {
                    e.printStackTrace();
                    finish();
                }
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                try {
                    Connection.connection.send(new KeyPacket(false, keyCode));
                } catch (IOException e) {
                    e.printStackTrace();
                    finish();
                }
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void finish() {
        if (sensorManager != null) {
            if (gyroscopeListener != null)
                sensorManager.unregisterListener(gyroscopeListener);
            if (accelerometerListener != null)
                sensorManager.unregisterListener(accelerometerListener);
        }
        if (Connection.connection != null)
            Connection.connection.close();

        super.finish();
    }

}
