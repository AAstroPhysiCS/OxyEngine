package OxyEngine.Core.Renderer;

public class OxyRenderPass {

    /*
     * Program this later! TODO:
     */

    private OxyRenderer mainRenderer;

    private static final class OxyRendererDeferred {

        public void lightningPass() {

        }

        public void geometryPass() {

        }

        public void depthPass() {

        }
    }

    private static final class OxyRendererForward {

    }

    public void use(OxyRendererSpecification spec){
        if(spec == OxyRendererSpecification.Deferred){
        } else {
        }
    }

    public OxyRenderer getMainRenderer() {
        return mainRenderer;
    }
}
