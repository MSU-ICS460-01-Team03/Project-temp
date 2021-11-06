package client;

import java.util.Arrays;

public class DataPacket extends AckPacket {
    private static final long serialVersionUID = 1L;

    int seqno; // 32-bit 4-byte
    byte data[]; // 0-500 bytes.

    public DataPacket(short len, int ackno, int seqno, byte[] data) {
        super(len, ackno);
        this.seqno = seqno;
        this.data = data;
    }

    @Override
    public String toString() {
        return "DataPacket [ackno=" + ackno + ", len=" + len + ", cksum=" + cksum + ", seqno=" + seqno + ", data="
                + Arrays.toString(data) + "]";
    }

}

