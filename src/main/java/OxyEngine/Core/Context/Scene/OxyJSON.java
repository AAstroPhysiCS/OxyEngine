package OxyEngine.Core.Context.Scene;

import OxyEngine.System.OxyFileSystem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static OxyEngine.System.OxySystem.oxyAssert;

public class OxyJSON {

    private static OxyJSON INSTANCE = null;

    private static OxyJSONWriterBuilder WRITER = null;
    private static OxyJSONReaderBuilder READER = null;

    public static OxyJSON getInstance() {
        if (INSTANCE == null) INSTANCE = new OxyJSON();
        return INSTANCE;
    }

    private OxyJSON() {
    }


    OxyJSONWriterBuilder openWritingStream() {
        if (WRITER == null) WRITER = new OxyJSONWriter();
        return WRITER;
    }

    OxyJSONReaderBuilder openReadingStream() {
        if (READER == null) READER = new OxyJSONReader();
        return READER;
    }

    static class OxyJSONArray {

        private String name;
        private final List<OxyJSONObject> objectList;

        OxyJSONArray(String name, List<OxyJSONObject> objectList) {
            this.objectList = objectList;
            this.name = name;
        }

        OxyJSONArray() {
            this(null, new ArrayList<>());
        }

        OxyJSONObject createOxyJSONObject(String name) {
            OxyJSONObject object = new OxyJSONObject(name, this, new ArrayList<>());
            objectList.add(object);
            return object;
        }

        public List<OxyJSONObject> getObjectList() {
            return objectList;
        }

        OxyJSONWriterBuilder separate() {
            return WRITER;
        }
    }

    static class OxyJSONObject {

        private String name;
        private OxyJSONArray src;
        private final List<OxyJSONField> fieldList;

        private final List<OxyJSONObject> fatherObjects = new ArrayList<>();
        private final List<OxyJSONObject> innerObject = new ArrayList<>();

        OxyJSONObject(String name, OxyJSONArray src, List<OxyJSONField> fieldList) {
            this.src = src;
            this.name = name;
            this.fieldList = fieldList;
        }

        OxyJSONObject() {
            this(null, null, new ArrayList<>());
        }

        OxyJSONObject putField(String tag, String value) {
            fieldList.add(new OxyJSONField(tag, value));
            return this;
        }

        OxyJSONObject createInnerObject(String name) {
            OxyJSONObject object = new OxyJSONObject(name, src, new ArrayList<>());
            innerObject.add(object);
            object.fatherObjects.add(this);
            return object;
        }

        public OxyJSONField getField(String tag) {
            for (OxyJSONField f : fieldList) {
                if (f.tag.equals(tag)) {
                    return f;
                }
            }
            return null;
        }

        public String getName() {
            return name;
        }

        public List<OxyJSONField> getFieldList() {
            return fieldList;
        }

        public List<OxyJSONObject> getInnerObjects() {
            return innerObject;
        }

        public OxyJSONObject getInnerObjectByName(String name) {
            for (OxyJSONObject obj : innerObject) {
                if (obj.name.equals(name)) {
                    return obj;
                }
            }
            return null;
        }

        public OxyJSONObject backToObject() {
            return fatherObjects.get(fatherObjects.size() - 1);
        }

        OxyJSONWriterBuilder separate() {
            return WRITER;
        }
    }

    static record OxyJSONField(String tag, String value) {
    }

    interface OxyJSONWriterBuilder {

        OxyJSONArray createOxyJSONArray(String name);

        OxyJSONObject createOxyJSONObject(String name);

        OxyJSONWriterBuilder build();

        OxyJSONWriterBuilder file(File f, boolean append);

        OxyJSONWriterBuilder file(File f);

        void writeAndCloseStream();
    }

    interface OxyJSONReaderBuilder {

        OxyJSONReaderBuilder read(String s);

        OxyJSONReaderBuilder read(File f);

        OxyJSONReaderBuilder getOxyJSONObject(String name, OxyJSONObject ref);

        OxyJSONReaderBuilder getOxyJSONArray(String name, OxyJSONArray ref);
    }

    static class OxyJSONWriter implements OxyJSONWriterBuilder {

        private FileWriter writer;
        private final StringBuilder builder = new StringBuilder();

        private final List<OxyJSONArray> oxyJSONArrays = new ArrayList<>();
        private final List<OxyJSONObject> oxyJSONSingleObjects = new ArrayList<>();

        @Override
        public OxyJSONWriterBuilder file(File f, boolean append) {
            try {
                assert f.exists() || f.createNewFile() : oxyAssert("Failed to create a scene serialization file");
                writer = new FileWriter(f, append);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return this;
        }

        @Override
        public OxyJSONWriterBuilder file(File f) {
            file(f, false);
            return this;
        }

        @Override
        public OxyJSONArray createOxyJSONArray(String name) {
            OxyJSONArray arr = new OxyJSONArray(name, new ArrayList<>());
            oxyJSONArrays.add(arr);
            return arr;
        }

        @Override
        public OxyJSONObject createOxyJSONObject(String name) {
            OxyJSONObject obj = new OxyJSONObject(name, null, new ArrayList<>());
            oxyJSONSingleObjects.add(obj);
            return obj;
        }

        private void buildJSONObject(List<OxyJSONObject> innerObject, int count) {
            for (OxyJSONObject i_Obj : innerObject) {
                builder.append("\t".repeat(Math.max(0, count)));
                builder.append("\t\t").append(i_Obj.name).append(": {").append("\n");
                for (OxyJSONField field : i_Obj.fieldList) {
                    builder.append("\t".repeat(Math.max(0, count))); //making sure that it is correctly shown.
                    builder.append("\t\t\t").append(field.tag).append(": ").append(field.value).append("\n");
                }
                buildJSONObject(i_Obj.innerObject, count + 1); // recursively calls inner methods...
                builder.append("\t".repeat(Math.max(0, count)));
                builder.append("\t\t").append("}").append("\n");
            }
        }

        @Override
        public OxyJSONWriterBuilder build() {

            for (OxyJSONArray arr : oxyJSONArrays) {
                builder.append(arr.name).append(" [").append("\n");
                for (OxyJSONObject obj : arr.objectList) {
                    builder.append("\t").append(obj.name).append(": {").append("\n");
                    for (OxyJSONField field : obj.fieldList) {
                        builder.append("\t\t").append(field.tag).append(": ").append(field.value).append("\n");
                    }
                    buildJSONObject(obj.innerObject, 0); // recursive method
                    builder.append("\t").append("}").append("\n");
                }
                builder.append("]").append("\n");
            }

            for (OxyJSONObject obj : oxyJSONSingleObjects) {
                builder.append(obj.name).append(": {").append("\n");
                for (OxyJSONField field : obj.fieldList) {
                    builder.append("\t").append(field.tag).append(": ").append(field.value).append("\n");
                }
                builder.append("}").append("\n");
            }
            return WRITER;
        }

        @Override
        public void writeAndCloseStream() {
            try {
                writer.write(builder.toString());
                writer.flush();
                writer.close();
                builder.setLength(0);
                oxyJSONArrays.clear();
                oxyJSONSingleObjects.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class OxyJSONReader implements OxyJSONReaderBuilder {

        private String loadedS;
        private String[] lineSplitted;

        @Override
        public OxyJSONReaderBuilder read(String s) {
            loadedS = OxyFileSystem.load(s);
            lineSplitted = loadedS.split("\n");
            return this;
        }

        @Override
        public OxyJSONReaderBuilder read(File f) {
            read(f.getPath());
            return this;
        }

        @Override
        public OxyJSONReaderBuilder getOxyJSONObject(String name, OxyJSONObject ref) {
            for (int i = 0; i < lineSplitted.length; i++) {
                String line = lineSplitted[i];
                if (line.endsWith(": {")) {
                    String[] nameSplit = line.split(":"); // name of oxyJsonObject
                    if (nameSplit.length == 2) {
                        String nameS = nameSplit[0].trim().strip();
                        if (name.equals(nameS)) {
                            ref.name = nameS;
                            int ptr = i;
                            do {
                                ptr++;
                                if (lineSplitted[ptr].endsWith(": {")) { //inner object
                                    String innerName = lineSplitted[ptr].split(": *")[0].trim().strip();
                                    OxyJSONObject innerRef = new OxyJSONObject();
                                    innerRef.name = innerName;
                                    ref.innerObject.add(innerRef);
                                    innerRef.fatherObjects.add(ref);
                                    do {
                                        ptr++; //check if the inner object fields are still focused, if yes... increment the ptr
                                        String[] field = lineSplitted[ptr].split(": "); // take the field
                                        if (field.length > 1) innerRef.putField(field[0], field[1]);
                                    } while (!lineSplitted[ptr].endsWith("}"));
                                } else { // it's a field
                                    String[] tagValue = lineSplitted[ptr].split(": ");
                                    if (tagValue.length == 2) {
                                        ref.fieldList.add(new OxyJSONField(tagValue[0].trim().strip(), tagValue[1].trim().strip()));
                                    }
                                }
                            } while (!lineSplitted[ptr].endsWith("}"));
                        }
                    }
                }
            }
            return this;
        }

        private int getInnerObjects(OxyJSONObject parent, int ptr) {
            ptr++;
            if (lineSplitted[ptr].endsWith(": {")) { //inner object
                String innerName = lineSplitted[ptr].split(": *")[0].trim().strip();
                OxyJSONObject innerObject = new OxyJSONObject();
                innerObject.name = innerName;
                parent.innerObject.add(innerObject);
                innerObject.fatherObjects.add(parent);
                while (!lineSplitted[ptr].endsWith("}")) {
                    ptr++;
                    String[] tagValue = lineSplitted[ptr].split(": ");
                    if (tagValue.length == 2) {
                        innerObject.putField(tagValue[0].trim().strip(), tagValue[1].trim().strip());
                    }
                    if (lineSplitted[ptr].endsWith(": {"))
                        ptr = getInnerObjects(innerObject, --ptr); // so that inner inner inner objects also work (for example Script)
                }
                ptr = getInnerObjects(parent, ptr);
            }
            return ptr;
        }

        @Override
        public OxyJSONReaderBuilder getOxyJSONArray(String name, OxyJSONArray ref) {
            for (int i = 0; i < lineSplitted.length; i++) {
                String line = lineSplitted[i];
                if (line.endsWith("[")) {
                    String[] nameSplit = line.split(" "); // name of oxyJsonArray
                    if (nameSplit.length == 2) {
                        String nameS = nameSplit[0].trim().strip();
                        if (name.equals(nameS)) {
                            ref.name = nameS;
                            int ptr = i;
                            while (!lineSplitted[ptr].endsWith("]")) {
                                ptr++;
                                if (lineSplitted[ptr].endsWith(": {")) { //inner object
                                    String innerName = lineSplitted[ptr].split(": *")[0].trim().strip();
                                    OxyJSONObject innerObject = new OxyJSONObject();
                                    innerObject.name = innerName;
                                    ref.objectList.add(innerObject);
                                    innerObject.src = ref;
                                    ptr++;
                                    while (!lineSplitted[ptr].endsWith(": {")) {
                                        String[] tagValue = lineSplitted[ptr].split(": ");
                                        ptr++;
                                        if (tagValue.length == 2) {
                                            innerObject.putField(tagValue[0].trim().strip(), tagValue[1].trim().strip());
                                        }
                                    }
                                    ptr = getInnerObjects(innerObject, --ptr);
                                }
                            }
                        }
                    }
                }
            }
            return this;
        }
    }
}
