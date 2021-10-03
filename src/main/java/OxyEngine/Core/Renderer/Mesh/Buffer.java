package OxyEngine.Core.Renderer.Mesh;

import OxyEngine.System.Disposable;

public abstract class Buffer<T> implements Disposable {

    protected int bufferId;

    protected T data;

    public Buffer(T data) {
        this.data = data;
    }

    protected abstract void load();

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean emptyData() {
        if (data instanceof Object[] s) {
            return s.length == 0;
        }
        return false;
    }

    public int getBufferId() {
        return bufferId;
    }
}
