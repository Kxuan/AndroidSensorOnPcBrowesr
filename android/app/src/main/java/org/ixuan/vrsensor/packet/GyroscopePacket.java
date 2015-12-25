package org.ixuan.vrsensor.packet;

import java.nio.ByteBuffer;

public class GyroscopePacket extends Packet {
    private float x, y, z;

    public GyroscopePacket(float[]v) {
        this.x = v[0];
        this.y = v[1];
        this.z = v[2];
    }

    @Override
    public byte[] toBinary() {
        ByteBuffer buffer = ByteBuffer.allocate(13);
        //1 is identify to GyroscopeSensor
        buffer.put((byte) 2);

        buffer.putFloat(x);
        buffer.putFloat(y);
        buffer.putFloat(z);
        return buffer.array();
    }
}
