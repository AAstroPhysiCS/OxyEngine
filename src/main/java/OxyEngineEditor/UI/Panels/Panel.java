package OxyEngineEditor.UI.Panels;

public abstract class Panel {

    public static final float[] bgC = new float[]{33 / 255f, 33 / 255f, 36 / 255f, 1.0f};

    public abstract void preload();

    public abstract void renderPanel();
}
