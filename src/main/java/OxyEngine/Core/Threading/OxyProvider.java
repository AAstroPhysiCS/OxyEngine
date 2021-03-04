package OxyEngine.Core.Threading;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class OxyProvider {

    protected final AtomicBoolean ready = new AtomicBoolean(false);

    public abstract void invokeCreate();

    public abstract void invokeUpdate(float ts);

    public boolean isReady(){
        return ready.get();
    }

    protected void setReadyState(boolean state){
        ready.set(state);
    }
}
