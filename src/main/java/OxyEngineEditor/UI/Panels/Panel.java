package OxyEngineEditor.UI.Panels;

public abstract class Panel {

    public static final float[] bgC = new float[]{32 / 255f, 32 / 255f, 32 / 255f, 1.0f};

    public abstract void preload();

    public abstract void renderPanel();
}
