package cn.xender.transfer.zxing;

public final class BitMatrix implements Cloneable{
    private final int width;
    private final int height;
    private final int rowSize;
    private final int[] bits;

    public BitMatrix(int width, int height) {
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("Both dimensions must be greater than 0");
        }
        this.width = width;
        this.height = height;
        this.rowSize = (width + 31) / 32;
        bits = new int[rowSize * height];
    }
    public boolean get(int x, int y) {
        int offset = y * rowSize + (x / 32);
        return ((bits[offset] >>> (x & 0x1f)) & 1) != 0;
    }
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setRegion(int left, int top, int width, int height) {
        if (top < 0 || left < 0) {
            throw new IllegalArgumentException("Left and top must be nonnegative");
        }
        if (height < 1 || width < 1) {
            throw new IllegalArgumentException("Height and width must be at least 1");
        }
        int right = left + width;
        int bottom = top + height;
        if (bottom > this.height || right > this.width) {
            throw new IllegalArgumentException("The region must fit inside the matrix");
        }
        for (int y = top; y < bottom; y++) {
            int offset = y * rowSize;
            for (int x = left; x < right; x++) {
                bits[offset + (x / 32)] |= 1 << (x & 0x1f);
            }
        }
    }
}
