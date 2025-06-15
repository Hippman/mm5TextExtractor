package util;

import java.io.*;
import java.util.List;

public class CCTextConverter {
    private static String encoding = "Cp866";

    public static void bytes2string(List<String> lines, byte[] bytes) throws UnsupportedEncodingException {
        StringBuffer sb = new StringBuffer();

        for (int j = 0; j < bytes.length; ++j) {
            switch (bytes[j]) {
                case 0:
                    lines.add(sb.toString());
                    sb = new StringBuffer();
                    break;
                case 1:
                case 2:
                case 5:
                case 6:
                case 10:
                case 13:
                case 14:
                    sb.append("{").append(Integer.toHexString(bytes[j])).append("}");
                    break;
                case 3:
                case 8:
                    sb.append("{").append(Integer.toHexString(bytes[j])).append(":").append(new String(bytes, j + 1, 1, encoding)).append("}");
                    ++j;
                    break;
                case 4:
                case 7:
                case 9:
                case 11:
                    sb.append("{").append(Integer.toHexString(bytes[j])).append(":").append(new String(bytes, j + 1, 3, encoding)).append("}");
                    j += 3;
                    break;
                case 12:
                    if (bytes[j + 1] == 100) {
                        sb.append("{").append(Integer.toHexString(bytes[j])).append(":").append(new String(bytes, j + 1, 1, encoding)).append("}");
                        ++j;
                    } else {
                        sb.append("{").append(Integer.toHexString(bytes[j])).append(":").append(new String(bytes, j + 1, 2, encoding)).append("}");
                        j += 2;
                    }
                    break;
                default:
                    sb.append(new String(bytes, j, 1, encoding));
            }
        }
        String tmp = sb.toString();
        if (!tmp.isEmpty()) {
            lines.add(tmp);
        }
    }
}
