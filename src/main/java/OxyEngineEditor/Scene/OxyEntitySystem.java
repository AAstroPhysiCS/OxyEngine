package OxyEngineEditor.Scene;

import java.util.List;

public interface OxyEntitySystem {
    void run();

    final record EntitySystemRunnable(List<OxyEntitySystem>systems) implements Runnable {
        @Override
        public void run() {
            for (OxyEntitySystem s : systems) {
                s.run();
            }
        }
    }
}
