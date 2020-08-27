package OxyEngine.Core.Layers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public final class LayerStack {

    private final List<Layer> layerStack = new ArrayList<>();

    public void pushLayer(Layer layer) {
        layerStack.add(layer);
    }

    public void pushLayer(Layer... layers){
        layerStack.addAll(Arrays.asList(layers));
    }

    public void removeLayer(Layer layer) {
        layerStack.remove(layer);
    }

    public void removeLayer(int index) {
        layerStack.remove(index);
    }

    public Iterator<Layer> getIterator(){
        return layerStack.iterator();
    }

    public void popLayer() {
        layerStack.remove(layerStack.size() - 1);
    }

    public List<Layer> getLayerStack() {
        return layerStack;
    }
}
