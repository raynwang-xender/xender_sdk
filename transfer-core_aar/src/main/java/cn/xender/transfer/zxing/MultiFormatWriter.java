package cn.xender.transfer.zxing;

import java.util.Map;

public final class MultiFormatWriter implements Writer{


    @Override
    public BitMatrix encode(String contents,
                            BarcodeFormat format,
                            int width, int height,
                            Map<EncodeHintType,?> hints) throws WriterException {

        Writer writer;
        switch (format) {
            case QR_CODE:
                writer = new QRCodeWriter();
                break;
            default:
                throw new IllegalArgumentException("No encoder available for format " + format);
        }
        return writer.encode(contents, format, width, height, hints);
    }

}
