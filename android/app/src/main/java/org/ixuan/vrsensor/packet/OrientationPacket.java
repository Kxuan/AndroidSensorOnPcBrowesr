package org.ixuan.vrsensor.packet;

import java.nio.ByteBuffer;

public class OrientationPacket extends Packet {
    private float x, y, z;

    public OrientationPacket(float[]v) {
        this.x = v[0];
        this.y = v[1];
        this.z = v[2];
    }

    @Override
    public byte[] toBinary() {
        ByteBuffer buffer = ByteBuffer.allocate(13);
        //4 is identify to RotationSensor
        buffer.put((byte) 4);

        buffer.putFloat(x);
        buffer.putFloat(y);
        buffer.putFloat(z);
        return buffer.array();
    }
}
