package util;

import dto.Offset;
import dto.OffsetType;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class OffsetUtils {
    public static List<Offset> getOffsets(byte[] exe, Integer localAddress) {
        List<Offset> offsets = new ArrayList<>();
        offsets.addAll(getDBOffsets(exe, localAddress));
        offsets.addAll(getPrintfOffsets(exe, localAddress));
        offsets.addAll(getPrintfB8Offsets(exe, localAddress));
        return offsets;
    }

    //Настройка поиска указателей типа Printf
    private static List<Offset> getPrintfOffsets(byte[] exe, Integer localAddress) {
        byte[] bytes = ByteBuffer.allocate(4).putInt(localAddress).array();
        byte[] search = ByteBuffer.allocate(3).array();
        search[0] = (byte) 0x68;
        search[1] = bytes[3];
        search[2] = bytes[2];
        return findOffsets(exe, search, OffsetType.PRINTF);
    }
    private static List<Offset> getPrintfB8Offsets(byte[] exe, Integer localAddress) {
        byte[] bytes = ByteBuffer.allocate(4).putInt(localAddress).array();
        byte[] search = ByteBuffer.allocate(3).array();
        search[0] = (byte) 0xb8;
        search[1] = bytes[3];
        search[2] = bytes[2];
        return findOffsets(exe, search, OffsetType.PRINTFB8);
    }

    //настройка поиска указателей типа DB
    private static List<Offset> getDBOffsets(byte[] exe, Integer localAddress) {
        byte[] bytes = ByteBuffer.allocate(4).putInt(localAddress).array();
        byte[] search = ByteBuffer.allocate(4).array();
        search[0] = bytes[3];
        search[1] = bytes[2];
        search[2] = (byte) 0xe2;
        search[3] = (byte) 0x46;
        return findOffsets(exe, search, OffsetType.DB);
    }

    //непосредственный поиск указателей
    private static List<Offset> findOffsets(byte[] exe, byte[] search, OffsetType type) {
        List<Offset> offsets = new ArrayList<>();
        for (int a = 0; a < exe.length - search.length; a++) {
            boolean check = true;
            for (int b = 0; b < search.length; b++) {
                if (exe[a + b] != search[b]) {
                    check = false;
                    break;
                }
            }
            if (check) {
                Offset offset = new Offset(type, a);
                offsets.add(offset);
            }
        }
        return offsets;
    }

    public static void writeOffsets(byte[] exe, List<Offset> offsets, Integer writeAddress, Byte first, Byte second) {
        offsets.forEach(of -> {
            if (of.getType() == OffsetType.DB) {
                writeDBOffset(exe, writeAddress, of.getOffset(), first, second);
            } else {
                writePrintfOffset(exe, writeAddress, of.getOffset());
            }
        });
    }

    //Настройка поиска указателей типа Printf
    public static void writePrintfOffset(byte[] exe, Integer localAddress, Integer pointerAddress) {
        byte[] bytes = ByteBuffer.allocate(4).putInt(localAddress).array();
        byte[] data = ByteBuffer.allocate(3).array();
        data[0] = (byte) 0x68;
        data[1] = bytes[3];
        data[2] = bytes[2];
        writeOffset(exe, data, pointerAddress);
    }

    //настройка поиска указателей типа DB
    public static void writeDBOffset(byte[] exe, Integer localAddress, Integer pointerAddress, Byte first, Byte second) {
        byte[] bytes = ByteBuffer.allocate(4).putInt(localAddress).array();
        byte[] data = ByteBuffer.allocate(4).array();
        data[0] = bytes[3];
        data[1] = bytes[2];
        data[2] = second;
        data[3] = first;
        writeOffset(exe, data, pointerAddress);
    }

    public static void writeOffset(byte[] ex, byte[] data, int address) {
        for (int a = 0; a < data.length; a++) {
            ex[a + address] = data[a];
        }
    }
}
