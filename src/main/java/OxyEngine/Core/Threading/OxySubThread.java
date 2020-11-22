package OxyEngine.Core.Threading;

import java.util.concurrent.atomic.AtomicBoolean;

public class OxySubThread {

    private Thread worker;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public OxySubThread(Runnable r, String name) {
        worker = new Thread(r, name);
    }

    public OxySubThread(){
    }

    public void setTarget(Runnable r){
        worker = new Thread(r, "OxySubThread");
    }

    public void start() {
        running.set(true);
        worker.start();
    }

    public void shutdown() {
        running.set(false);
        try {
            worker.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void interrupt(){
        worker.interrupt();
    }

    public Thread.State getState(){
        return worker.getState();
    }

    public AtomicBoolean getRunningState() {
        return running;
    }
}