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
        running.set(true);
        worker = new Thread(r);
    }

    public void start() {
        running.set(true);
        worker.start();
    }

    @SuppressWarnings("deprecation")
    public void shutdown() {
        worker.checkAccess();
        worker.stop(); //deprecated, should not be used
        try {
            worker.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("removal")
    public void restart(){
        running.set(true);
        worker.checkAccess();
        worker.resume(); //deprecated, should not be used
    }

    @SuppressWarnings("removal")
    public void stop(){
        running.set(false);
        worker.checkAccess();
        worker.suspend(); //deprecated, should not be used
    }

    public AtomicBoolean getRunningState() {
        return running;
    }
}