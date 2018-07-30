package cn.xender.transfer.zxing;



import java.util.Arrays;

public final class BitArray implements Cloneable {

    private int[] bits;
    private int size;

    public BitArray() {
        this.size = 0;
        this.bits = new int[1];
    }

    BitArray(int[] bits, int size) {
        this.bits = bits;
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public int getSizeInBytes() {
        return (size + 7) / 8;
    }

    private void ensureCapacity(int size) {
        if (size > bits.length * 32) {
            int[] newBits = makeArray(size);
            System.arraycopy(bits, 0, newBits, 0, bits.length);
            this.bits = newBits;
        }
    }

    public boolean get(int i) {
        return (bits[i / 32] & (1 << (i & 0x1F))) != 0;
    }

    public void appendBit(boolean bit) {
        ensureCapacity(size + 1);
        if (bit) {
            bits[size / 32] |= 1 << (size & 0x1F);
        }
        size++;
    }

    public void appendBits(int value, int numBits) {
        if (numBits < 0 || numBits > 32) {
            throw new IllegalArgumentException("Num bits must be between 0 and 32");
        }
        ensureCapacity(size + numBits);
        for (int numBitsLeft = numBits; numBitsLeft > 0; numBitsLeft--) {
            appendBit(((value >> (numBitsLeft - 1)) & 0x01) == 1);
        }
    }

    public void appendBitArray(BitArray other) {
        int otherSize = other.size;
        ensureCapacity(size + otherSize);
        for (int i = 0; i < otherSize; i++) {
            appendBit(other.get(i));
        }
    }

    public void xor(BitArray other) {
        if (size != other.size) {
            throw new IllegalArgumentException("Sizes don't match");
        }
        for (int i = 0; i < bits.length; i++) {
            // The last int could be incomplete (i.e. not have 32 bits in
            // it) but there is no problem since 0 XOR 0 == 0.
            bits[i] ^= other.bits[i];
        }
    }
    public void toBytes(int bitOffset, byte[] array, int offset, int numBytes) {
        for (int i = 0; i < numBytes; i++) {
            int theByte = 0;
            for (int j = 0; j < 8; j++) {
                if (get(bitOffset)) {
                    theByte |= 1 << (7 - j);
                }
                bitOffset++;
            }
            array[offset + i] = (byte) theByte;
        }
    }

    private static int[] makeArray(int size) {
        return new int[(size + 31) / 32];
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BitArray)) {
            return false;
        }
        BitArray other = (BitArray) o;
        return size == other.size && Arrays.equals(bits, other.bits);
    }

    @Override
    public int hashCode() {
        return 31 * size + Arrays.hashCode(bits);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            if ((i & 0x07) == 0) {
                result.append(' ');
            }
            result.append(get(i) ? 'X' : '.');
        }
        return result.toString();
    }

    @Override
    public BitArray clone() {
        return new BitArray(bits.clone(), size);
    }

}
