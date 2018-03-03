package dvlopt.spi ;


import java.util.Arrays       ;
import java.util.List         ;
import com.sun.jna.Structure  ;
import com.sun.jna.NativeLong ;


public class SpiIOCTransfer extends Structure {


    public NativeLong txBuff ;

    public NativeLong rxBuff ;

    public int        len ;

    public int        speedHz ;

    public short      delayMicros ;

    public byte       bitsPerWord ;

    public byte       csChange ;

    // Undocumented in <linux/spi/spidev.h>

    public byte       txNBits ;

    public byte       rxNBits ;

    public short      pad ;


    protected List getFieldOrder() {


        return Arrays.asList( new String[] {
            "txBuff"      ,
            "rxBuff"      ,
            "len"         ,
            "speedHz"     ,
            "delayMicros" ,
            "bitsPerWord" ,
            "csChange"    ,
            "txNBits"     ,
            "rxNBits"     ,
            "pad"
        } ) ;
    }
}
