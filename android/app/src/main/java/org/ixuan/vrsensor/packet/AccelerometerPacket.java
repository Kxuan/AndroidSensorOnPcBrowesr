package org.ixuan.vrsensor.packet;

import java.nio.ByteBuffer;

public class AccelerometerPacket extends Packet {
    private float x;
    private float y;
    private float z;

    public AccelerometerPacket(float[] v) {
        this.x = v[0];
        this.y = v[1];
        this.z = v[2];
    }

    @Override
    public byte[] toBinary() {
        ByteBuffer buffer = ByteBuffer.allocate(13);
        //1 is identify to AccelerometerSensor
        buffer.put((byte) 1);

        buffer.putFloat(x);
        buffer.putFloat(y);
        buffer.putFloat(z);
        return buffer.array();
    }
}
