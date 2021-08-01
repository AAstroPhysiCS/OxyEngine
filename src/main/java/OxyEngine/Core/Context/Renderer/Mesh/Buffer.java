package OxyEngine.Core.Context.Renderer.Mesh;

import OxyEngine.System.OxyDisposable;

public abstract class Buffer implements OxyDisposable {

    protected int bufferId;

    protected abstract void load();

    public boolean glBufferNull() {
        return bufferId == 0;
    }

    public int getBufferId() {
        return bufferId;
    }
}
