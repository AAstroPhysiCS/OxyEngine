package OxyEngine.Core.Renderer.Buffer;

import OxyEngineEditor.Sandbox.OxyObjects.OxyEntity;
import OxyEngine.System.OxyDisposable;

public abstract class Buffer implements OxyDisposable {

    protected int bufferId;

    public Buffer(){}

    protected abstract void load();

    public abstract void addToBuffer(OxyEntity oxyEntity);

    public boolean empty(){
        return bufferId == 0;
    }

    public int getBufferId(){
        return bufferId;
    }
}
