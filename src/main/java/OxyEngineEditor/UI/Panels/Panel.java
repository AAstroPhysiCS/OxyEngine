package OxyEngineEditor.UI.Panels;

public abstract class Panel {

    public static final float[] bgC = new float[]{33 / 255f, 33 / 255f, 36 / 255f, 1.0f};
    public static final float[] frameBgC = new float[]{58 / 255f, 56 / 255f, 58 / 255f, 1.0f};
    public static final float[] masterCardColor = new float[]{37 / 255f, 38 / 255f, 39 / 255f, 1.0f};
    public static final float[] childCardBgC = new float[]{45 / 255f, 44 / 255f, 45 / 255f, 1.0f};

    public abstract void preload();

    public abstract void renderPanel();
}
