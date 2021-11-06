package client;


import java.io.Serializable;

public class AckPacket implements Serializable {
    private static final long serialVersionUID = 1L;
    short cksum; // 16-bit 2-byte
    short len; // 16-bit 2-byte
    int ackno; // 32-bit 4-byte

    public AckPacket(short len, int ackno) {
        this.len = len;
        this.ackno = ackno;
        generateCksum();
    }

    protected void generateCksum() {
        short nob = (short) ((Math.floor(Math.log(len) / Math.log(2))) + 1);
        cksum += (short) (((1 << nob) - 1) ^ len);
        nob = (short) ((Math.floor(Math.log(ackno) / Math.log(2))) + 1);
        cksum += (short) (((1 << nob) - 1) ^ ackno);
    }

    public boolean isError() {
        short nob = (short) ((Math.floor(Math.log(len) / Math.log(2))) + 1);
        short sum = (short) (((1 << nob) - 1) ^ len);
        nob = (short) ((Math.floor(Math.log(ackno) / Math.log(2))) + 1);
        sum += (short) (((1 << nob) - 1) ^ ackno);
        nob = (short) ((Math.floor(Math.log(cksum) / Math.log(2))) + 1);
        sum += (short) (((1 << nob) - 1) ^ cksum);
        nob = (short) ((Math.floor(Math.log(sum) / Math.log(2))) + 1);
        sum = (short) (((1 << nob) - 1) ^ sum);
        // System.out.println(sum);
        return sum != 0;
    }

    @Override
    public String toString() {
        return "AckPacket [ackno=" + ackno + ", cksum=" + cksum + ", len=" + len + "]";
    }

}

