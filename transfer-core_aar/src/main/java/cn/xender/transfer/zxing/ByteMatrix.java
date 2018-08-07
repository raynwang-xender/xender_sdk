package cn.xender.transfer.zxing;

public final class ByteMatrix {

    private final byte[][] bytes;
    private final int width;
    private final int height;

    public ByteMatrix(int width, int height) {
        bytes = new byte[height][width];
        this.width = width;
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public byte get(int x, int y) {
        return bytes[y][x];
    }

    public byte[][] getArray() {
        return bytes;
    }

    public void set(int x, int y, int value) {
        bytes[y][x] = (byte) value;
    }

    public void set(int x, int y, boolean value) {
        bytes[y][x] = (byte) (value ? 1 : 0);
    }

    public void clear(byte value) {
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                bytes[y][x] = value;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(2 * width * height + 2);
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                switch (bytes[y][x]) {
                    case 0:
                        result.append(" 0");
                        break;
                    case 1:
                        result.append(" 1");
                        break;
                    default:
                        result.append("  ");
                        break;
                }
            }
            result.append('\n');
        }
        return result.toString();
    }
}
