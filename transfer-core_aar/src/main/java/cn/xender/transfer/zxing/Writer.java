package cn.xender.transfer.zxing;

import java.util.Map;

public interface Writer {

    BitMatrix encode(String contents,
                     BarcodeFormat format,
                     int width,
                     int height,
                     Map<EncodeHintType,?> hints)
            throws WriterException;
}
