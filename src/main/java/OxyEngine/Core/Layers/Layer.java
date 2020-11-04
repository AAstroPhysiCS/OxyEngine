package OxyEngine.Core.Layers;

public abstract class Layer {

    public abstract void build();

    public abstract void rebuild();

    public abstract void update(float ts);

    public abstract void render(float ts);
}
