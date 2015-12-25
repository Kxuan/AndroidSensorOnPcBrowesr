package org.ixuan.vrsensor;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import org.ixuan.vrsensor.packet.Packet;

import java.lang.reflect.Constructor;

public class SensorEventPoster implements SensorEventListener {

    private Constructor<? extends Packet> packager;
    private Activity activity;
    private TextView[] textViews;

    private boolean enabled = false;
    private float[] originValue;

    public SensorEventPoster(
            Activity activity,
            Class<? extends Packet> packager,
            TextView[] textViews) throws NoSuchMethodException {
        this.activity = activity;
        this.textViews = textViews;
        this.packager = packager.getConstructor(float[].class);
        originValue = new float[textViews.length];
    }


    public boolean isSmallMovement(float[] values) {
        if (values.length != originValue.length)
            return true;
        for (int i = 0; i < values.length; i++) {
            if (Math.abs(originValue[i] - values[i]) > 1e-6)
                return false;
        }
        originValue = values;
        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        final float[] values = event.values.clone();
        for (int i = 0; i < textViews.length; i++) {
            textViews[i].setText(String.format("%.3f", values[i]));
        }

        if (!enabled || Connection.connection == null || Connection.connection.getSenderHandler() == null)
            return;

        Connection.connection.getSenderHandler().post(new Runnable() {
            @Override
            public void run() {
                boolean needPost = !isSmallMovement(values);
                if (needPost) {
                    try {
                        Packet packet = packager.newInstance(new Object[]{values});
                        Connection.connection.send(packet);
                    } catch (NullPointerException e) {
                        activity.finish();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                }

            }
        });
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
