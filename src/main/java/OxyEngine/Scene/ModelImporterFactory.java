package OxyEngine.Scene;

import org.lwjgl.assimp.AIScene;

public sealed interface ModelImporterFactory permits MeshImporter, AnimationImporter {

    void process(AIScene scene, String scenePath, OxyEntity root);

    @SuppressWarnings("unchecked")
    static <T extends ModelImporterFactory> T getInstance(ImporterType type){
        return switch (type){
            case MeshImporter -> (T) new MeshImporter();
            case AnimationImporter -> (T) new AnimationImporter();
        };
    }
}
