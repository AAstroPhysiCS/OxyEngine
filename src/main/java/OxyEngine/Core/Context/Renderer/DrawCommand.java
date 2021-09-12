package OxyEngine.Core.Context.Renderer;

import OxyEngine.Components.AnimationComponent;
import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Context.Renderer.Mesh.OpenGLMesh;

public final record DrawCommand(OpenGLMesh mesh, TransformComponent transform, AnimationComponent animation) {
}
