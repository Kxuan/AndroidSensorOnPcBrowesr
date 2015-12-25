package org.ixuan.vrsensor.packet;

import java.nio.ByteBuffer;

public class KeyPacket extends Packet {
    private boolean keyUp;
    private int key;

    public KeyPacket(boolean keyUp, int key) {
        this.keyUp = keyUp;
        this.key = key;
    }

    @Override
    public byte[] toBinary() {
        return ByteBuffer
                .allocate(6)
                .put((byte) 3)
                .put((byte) (keyUp ? 1 : 0))
                .putInt(this.key)
                .array();
    }
}
