package OxyEngineEditor.UI.Selector.Tools;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngineEditor.Sandbox.Components.BoundingBoxComponent;
import OxyEngineEditor.Sandbox.Components.SelectedComponent;
import OxyEngineEditor.Sandbox.Components.TagComponent;
import OxyEngineEditor.Sandbox.Components.TransformComponent;
import OxyEngineEditor.Sandbox.Scene.OxyEntity;
import org.joml.Intersectionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.Set;

public interface ObjectSelector {

    Vector3f min = new Vector3f(), max = new Vector3f();
    Vector2f nearFar = new Vector2f();

    Vector3f toClipSpace(float width, float height, Vector2f mousePos);

    Vector3f getObjectPosRelativeToCamera(float width, float height, Vector2f mousePos, OxyCamera camera);

    //It's better to not summarize this method with the other ones...
    default OxyEntity selectObject(Set<OxyEntity> entities, Vector3f origin, Vector3f direction) {
        reset();
        OxyEntity selectedEntity = null;
        float closestDistance = Float.POSITIVE_INFINITY;

        for (OxyEntity entity : entities) {

            if (!entity.has(SelectedComponent.class) || !entity.has(TransformComponent.class)) continue;
            if (entity.get(SelectedComponent.class).fixedValue) continue;

            TransformComponent c = entity.get(TransformComponent.class);
            SelectedComponent selected = entity.get(SelectedComponent.class);
            BoundingBoxComponent boundingBox = entity.get(BoundingBoxComponent.class);
            TagComponent tag = entity.get(TagComponent.class);

            Vector3f position = new Vector3f(c.position);

            selected.selected = false;

            if (tag.tag().startsWith("Sphere")) {
                if (c.scale.x == c.scale.y && c.scale.x == c.scale.z) {
                    if (Intersectionf.intersectRaySphere(origin, direction, position, boundingBox.max().y * boundingBox.max().y * c.scale.y * c.scale.y, nearFar) && nearFar.x < closestDistance) {
                        closestDistance = nearFar.x;
                        selectedEntity = entity;
                        selected.selected = true;
                    }
                } else {
                    float result;
                    for (int i = 0; i < entity.vertices.length; ) {
                        if (i >= entity.vertices.length - 18) break;
                        Vector3f firstVertex = new Vector3f(entity.vertices[i++], entity.vertices[i++], entity.vertices[i++]);
                        i += 5;
                        Vector3f secondVertex = new Vector3f(entity.vertices[i++], entity.vertices[i++], entity.vertices[i++]);
                        i += 5;
                        Vector3f thirdVertex = new Vector3f(entity.vertices[i++], entity.vertices[i++], entity.vertices[i++]);
                        i += 5;
                        if ((result = Intersectionf.intersectRayTriangle(origin, direction, firstVertex, secondVertex, thirdVertex, 0.000001f)) != -1) {
                            if (result < closestDistance) {
                                closestDistance = result;
                                selectedEntity = entity;
                                selected.selected = true;
                            }
                        }
                    }
                }
            } else if (tag.tag().startsWith("Cube")) {
                min.set(position);
                max.set(position);
                min.add(new Vector3f(boundingBox.min()).negate().mul(c.scale));
                max.add(new Vector3f(boundingBox.max()).mul(c.scale));
                if (Intersectionf.intersectRayAab(origin, direction, min, max, nearFar) && nearFar.x < closestDistance) {
                    closestDistance = nearFar.x;
                    selectedEntity = entity;
                    selected.selected = true;
                }
            } else if (tag.tag().startsWith("Cone")) {
                float result;
                for (int i = 0; i < entity.vertices.length; ) {
                    if (i >= entity.vertices.length - 18) break;
                    Vector3f firstVertex = new Vector3f(entity.vertices[i++], entity.vertices[i++], entity.vertices[i++]);
                    i += 5;
                    Vector3f secondVertex = new Vector3f(entity.vertices[i++], entity.vertices[i++], entity.vertices[i++]);
                    i += 5;
                    Vector3f thirdVertex = new Vector3f(entity.vertices[i++], entity.vertices[i++], entity.vertices[i++]);
                    i += 5;
                    if ((result = Intersectionf.intersectRayTriangle(origin, direction, firstVertex, secondVertex, thirdVertex, 0.000001f)) != -1) {
                        if (result < closestDistance) {
                            closestDistance = result;
                            selectedEntity = entity;
                            selected.selected = true;
                        }
                    }
                }
            }
        }
        return selectedEntity;
    }

    //mainly for gizmo
    default OxyEntity selectObject(OxyEntity entity, Vector3f origin, Vector3f direction) {
        reset();
        if (origin == null || direction == null || !entity.has(SelectedComponent.class) || !entity.has(TransformComponent.class))
            return null;

        OxyEntity selectedEntity = null;
        TransformComponent c = entity.get(TransformComponent.class);
        SelectedComponent selected = entity.get(SelectedComponent.class);
        BoundingBoxComponent boundingBox = entity.get(BoundingBoxComponent.class);
        TagComponent tag = entity.get(TagComponent.class);

        Vector3f position = new Vector3f(c.position);

        selected.selected = false;

        if (tag.tag().startsWith("Circle")) {
            float result;
            float closestDistance = Float.POSITIVE_INFINITY;
            for (int i = 0; i < entity.vertices.length; ) {
                if (i >= entity.vertices.length - 18) break;
                Vector3f firstVertex = new Vector3f(entity.vertices[i++], entity.vertices[i++], entity.vertices[i++]);
                i += 5;
                Vector3f secondVertex = new Vector3f(entity.vertices[i++], entity.vertices[i++], entity.vertices[i++]);
                i += 5;
                Vector3f thirdVertex = new Vector3f(entity.vertices[i++], entity.vertices[i++], entity.vertices[i++]);
                i += 5;
                if ((result = Intersectionf.intersectRayTriangle(origin, direction, firstVertex, secondVertex, thirdVertex, 0.000001f)) != -1) {
                    if (result < closestDistance) {
                        closestDistance = result;
                        selectedEntity = entity;
                        selected.selected = true;
                    }
                }
            }
        } else {
            min.set(position);
            max.set(position);
            min.add(new Vector3f(boundingBox.min()).negate().mul(c.scale));
            max.add(new Vector3f(boundingBox.max()).mul(c.scale));
            if (Intersectionf.intersectRayAab(origin, direction, min, max, nearFar)) {
                selectedEntity = entity;
                selected.selected = true;
            }
        }
        return selectedEntity;
    }

    default void reset() {
        min.zero();
        max.zero();
        nearFar.zero();
    }
}