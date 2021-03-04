package OxyEngine;

public enum TextureSlot {
    UITEXTURE(-1),
    UNUSED(0),
    ALBEDO(1),
    NORMAL(2),
    ROUGHNESS(3),
    METALLIC(4),
    AO(5),
    HDR(6),
    IRRADIANCE(7),
    PREFILTER(8),
    BDRF(9);

    private final int value;

    TextureSlot(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
