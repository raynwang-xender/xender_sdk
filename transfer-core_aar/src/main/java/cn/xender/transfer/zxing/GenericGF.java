package cn.xender.transfer.zxing;

public final class GenericGF {


    public static final GenericGF AZTEC_DATA_6 = new GenericGF(0x43, 64, 1); // x^6 + x + 1
    public static final GenericGF QR_CODE_FIELD_256 = new GenericGF(0x011D, 256, 0); // x^8 + x^4 + x^3 + x^2 + 1
    public static final GenericGF DATA_MATRIX_FIELD_256 = new GenericGF(0x012D, 256, 1); // x^8 + x^5 + x^3 + x^2 + 1

    private final int[] expTable;
    private final int[] logTable;
    private final GenericGFPoly zero;
    private final GenericGFPoly one;
    private final int size;
    private final int primitive;
    private final int generatorBase;

    /**
     * Create a representation of GF(size) using the given primitive polynomial.
     *
     * @param primitive irreducible polynomial whose coefficients are represented by
     *  the bits of an int, where the least-significant bit represents the constant
     *  coefficient
     * @param size the size of the field
     * @param b the factor b in the generator polynomial can be 0- or 1-based
     *  (g(x) = (x+a^b)(x+a^(b+1))...(x+a^(b+2t-1))).
     *  In most cases it should be 1, but for QR code it is 0.
     */
    public GenericGF(int primitive, int size, int b) {
        this.primitive = primitive;
        this.size = size;
        this.generatorBase = b;

        expTable = new int[size];
        logTable = new int[size];
        int x = 1;
        for (int i = 0; i < size; i++) {
            expTable[i] = x;
            x *= 2; // we're assuming the generator alpha is 2
            if (x >= size) {
                x ^= primitive;
                x &= size - 1;
            }
        }
        for (int i = 0; i < size - 1; i++) {
            logTable[expTable[i]] = i;
        }
        zero = new GenericGFPoly(this, new int[]{0});
        one = new GenericGFPoly(this, new int[]{1});
    }

    GenericGFPoly getZero() {
        return zero;
    }


    GenericGFPoly buildMonomial(int degree, int coefficient) {
        if (degree < 0) {
            throw new IllegalArgumentException();
        }
        if (coefficient == 0) {
            return zero;
        }
        int[] coefficients = new int[degree + 1];
        coefficients[0] = coefficient;
        return new GenericGFPoly(this, coefficients);
    }

    static int addOrSubtract(int a, int b) {
        return a ^ b;
    }

    int exp(int a) {
        return expTable[a];
    }

    int log(int a) {
        if (a == 0) {
            throw new IllegalArgumentException();
        }
        return logTable[a];
    }

    int inverse(int a) {
        if (a == 0) {
            throw new ArithmeticException();
        }
        return expTable[size - logTable[a] - 1];
    }

    int multiply(int a, int b) {
        if (a == 0 || b == 0) {
            return 0;
        }
        return expTable[(logTable[a] + logTable[b]) % (size - 1)];
    }

    public int getSize() {
        return size;
    }

    public int getGeneratorBase() {
        return generatorBase;
    }

    @Override
    public String toString() {
        return "GF(0x" + Integer.toHexString(primitive) + ',' + size + ')';
    }
}
