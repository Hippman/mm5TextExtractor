package util;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class DataUtils {
    private static Integer blockStart = 0x4c290;

    public static void string2bytes(String str, ByteArrayOutputStream baos) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes(Charset.forName("cp866"));
        int j = 0;

        for (byte cbrace = "{".getBytes()[0]; j < bytes.length; ++j) {
            if (bytes[j] == cbrace) {
                int nextByte = Integer.parseInt(new String(bytes, j + 1, 1), 16);
                switch (nextByte) {
                    case 1:
                    case 2:
                    case 5:
                    case 6:
                    case 10:
                    case 13:
                    case 14:
                        baos.write(nextByte);
                        j += 2;
                        break;
                    case 3:
                    case 8:
                        baos.write(nextByte);
                        baos.write(bytes[j + 3]);
                        j += 4;
                        break;
                    case 4:
                    case 7:
                    case 9:
                    case 11:
                        baos.write(nextByte);
                        baos.write(bytes, j + 3, 3);
                        j += 6;
                        break;
                    case 12:
                        baos.write(nextByte);
                        if (bytes[j + 3] == 100) {
                            baos.write(bytes, j + 3, 1);
                            j += 4;
                        } else {
                            baos.write(bytes, j + 3, 2);
                            j += 5;
                        }
                        break;
                    default:
                        j += 2;
                }
            } else {
                baos.write(bytes[j]);
            }
        }

        baos.write(0);
    }

    public static byte[] calcPrintfPointer(int offset) {
        int locOffset = offset - blockStart;
        byte[] bytes = ByteBuffer.allocate(4).putInt(locOffset).array();
        byte[] ret = new byte[3];
        ret[0] = 0x68;
        ret[1] = bytes[3];
        ret[2] = bytes[2];
        return ret;
    }
    public static byte[] calcPrintfB8Pointer(int offset) {
        int locOffset = offset - blockStart;
        byte[] bytes = ByteBuffer.allocate(4).putInt(locOffset).array();
        byte[] ret = new byte[3];
        ret[0] = (byte)0xB8;
        ret[1] = bytes[3];
        ret[2] = bytes[2];
        return ret;
    }

    public static byte[] calcDbPointer(int offset) {
        byte[] ret = new byte[4];
        int tmpOffset = offset - 0x5470;
        int smallOffset = tmpOffset - ((tmpOffset / 0x10) * 0x10);
        int rightPart = tmpOffset / 0x10;
        byte[] bytes = ByteBuffer.allocate(4).putInt(smallOffset).array();
        ret[0] = bytes[3];
        ret[1] = 0;
        bytes = ByteBuffer.allocate(4).putInt(rightPart).array();
        ret[2] = bytes[3];
        ret[3] = bytes[2];
        return ret;
    }
    /*
    public static int getWord(byte[] b, int index) {
        return getByte(b, index) + (getByte(b, index + 1) << 8);
    }*/
}
