package OxyEngine.Core.Renderer.Texture;

public enum TextureSlot {
    UITEXTURE(-1),
    UNUSED(0),
    ALBEDO(1),
    NORMAL(2),
    ROUGHNESS(3),
    METALLIC(4),
    AO(5),
    EMISSIVE(6),
    HDR(7),
    IRRADIANCE(8),
    PREFILTER(9),
    BDRF(10),
    CSM(11); // + NUMBER CASCADES

    private final int value;

    TextureSlot(int value) {
        this.value = value;
    }

    public static TextureSlot find(int textureSlot) {
        for(TextureSlot t : TextureSlot.values()){
            if(t.getValue() == textureSlot) return t;
        }
        throw new IllegalStateException("Texture slot not defined!");
    }

    public int getValue() {
        return value;
    }
}
