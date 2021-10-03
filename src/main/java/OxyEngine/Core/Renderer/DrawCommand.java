package OxyEngine.Core.Renderer;

import OxyEngine.Components.AnimationComponent;
import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Renderer.Mesh.OpenGLMesh;

public final record DrawCommand(OpenGLMesh mesh, TransformComponent transform, AnimationComponent animation) {
}
