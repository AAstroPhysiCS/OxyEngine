package OxyEngineEditor.Scene;

import OxyEngineEditor.Scene.Objects.Model.OxyMaterial;

import java.util.List;

@FunctionalInterface
public interface SerializationManager<T> {

    List<T> manage(String sValue);

    final class OxyMaterialSerializationManager implements SerializationManager<OxyMaterial>{

        private OxyMaterialSerializationManager(){}

        @Override
        public List<OxyMaterial> manage(String sValue) {
            return null;
        }
    }

    default List<? extends T> getAllSerializationManagers(){
        return null;
    }
}
