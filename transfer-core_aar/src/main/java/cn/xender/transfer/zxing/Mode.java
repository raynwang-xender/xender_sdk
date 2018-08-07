package cn.xender.transfer.zxing;

public enum Mode {

    ALPHANUMERIC(new int[]{9, 11, 13}, 0x02),
    NUMERIC(new int[]{10, 12, 14}, 0x01),
    BYTE(new int[]{8, 16, 16}, 0x04),
    ECI(new int[]{0, 0, 0}, 0x07), // character counts don't apply
    KANJI(new int[]{8, 10, 12}, 0x08);


    private final int[] characterCountBitsForVersions;
    private final int bits;

    Mode(int[] characterCountBitsForVersions, int bits) {
        this.characterCountBitsForVersions = characterCountBitsForVersions;
        this.bits = bits;
    }

    public int getBits() {
        return bits;
    }

    public int getCharacterCountBits(Version version) {
        int number = version.getVersionNumber();
        int offset;
        if (number <= 9) {
            offset = 0;
        } else if (number <= 26) {
            offset = 1;
        } else {
            offset = 2;
        }
        return characterCountBitsForVersions[offset];
    }
}
