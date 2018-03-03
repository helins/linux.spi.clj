package dvlopt.spi ;


import java.lang.IllegalArgumentException ;

import com.sun.jna.Native     ;
import com.sun.jna.Library    ;
import com.sun.jna.NativeLong ;

import dvlopt.spi.SpiIOCTransfer ;


public class Spi {


    static {
        Native.register( "c" ) ;
    }




    public static native int ioctl( int            fd      ,
                                    NativeLong     request ,
                                    SpiIOCTransfer arg     ) ;




    static public interface ISpi extends Library {


        int ioctl( int fd             ,
                   NativeLong request ,
                   Object... args     ) ;
    }


    public static ISpi proxy = (ISpi)Native.loadLibrary( "c"        ,
                                                         ISpi.class ) ;




    public static class Requests {


        public static NativeLong iocMessage( int n ) {
            
            if ( n >= 512 )  throw new IllegalArgumentException( "'n' must be <= 512." ) ;

            return new NativeLong( 1073769216L + n * 2097152 ,
                                   true                      ) ;
        }



        public static NativeLong SPI_IOC_RD_MODE          = new NativeLong( 2147576577L ,
                                                                            true        ) ;

        public static NativeLong SPI_IOC_WR_MODE          = new NativeLong( 1073834753L ,
                                                                            true        ) ;


        public static NativeLong SPI_IOC_RD_MODE32        = new NativeLong( 2147773189L ,
                                                                            true        ) ;

        public static NativeLong SPI_IOC_WR_MODE32        = new NativeLong( 1074031365L ,
                                                                            true        ) ;


        public static NativeLong SPI_IOC_RD_LSB_FIRST     = new NativeLong( 2147576578L ,
                                                                            true        ) ;

        public static NativeLong SPI_IOC_WR_LSB_FIRST     = new NativeLong( 1073834754L ,
                                                                            true        ) ;

        public static int        SPI_LSB_FIRST            = 8 ;



        public static NativeLong SPI_IOC_RD_BITS_PER_WORD = new NativeLong( 2147576579L ,
                                                                            true        ) ;

        public static NativeLong SPI_IOC_WR_BITS_PER_WORD = new NativeLong( 1073834755L ,
                                                                            true        ) ;


        public static NativeLong SPI_IOC_RD_MAX_SPEED_HZ  = new NativeLong( 2147773188L ,
                                                                            true        ) ;

        public static NativeLong SPI_IOC_WR_MAX_SPEED_HZ  = new NativeLong( 1074031364L ,
                                                                            true        ) ;
    }
}
