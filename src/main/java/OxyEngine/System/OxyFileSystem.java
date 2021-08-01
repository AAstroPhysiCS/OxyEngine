package OxyEngine.System;

import OxyEngine.Core.Context.Scene.SceneRuntime;
import OxyEngine.Core.Context.Scene.SceneState;
import OxyEngineEditor.EntryPoint;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.nfd.NativeFileDialog;

import java.io.*;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import static OxyEngine.Core.Context.Scene.SceneRuntime.ACTIVE_SCENE;
import static org.lwjgl.BufferUtils.createByteBuffer;

public interface OxyFileSystem {

    static String load(String path) {
        final StringBuilder builder = new StringBuilder();
        try {
            File file = new File(path);
            FileReader fileReader = new FileReader(file);
            BufferedReader reader = new BufferedReader(fileReader);
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            fileReader.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    static boolean deleteDir(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (! Files.isSymbolicLink(f.toPath())) {
                    deleteDir(f);
                }
            }
        }
        return file.delete();
    }

    static String load(File f) {
        return load(f.getPath());
    }

    static ByteBuffer loadAsByteBuffer(File f) {
        try {
            byte[] buf = Files.readAllBytes(Path.of(f.getPath()));
            ByteBuffer buffer = createByteBuffer(buf.length);
            buffer.put(buf).flip();
            return buffer;
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new BufferUnderflowException();
    }

    static void writeAsByteBuffer(File fDest, ByteBuffer buffer) {
        try (FileOutputStream stream = new FileOutputStream(fDest)) {
            buffer.rewind();
            byte[] buff = new byte[buffer.remaining()];
            buffer.get(buff);
            stream.write(buff);
            stream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static String getResourceByPath(String path) {
        String fullPath = EntryPoint.class.getResource(path).getPath();
        return (String) fullPath.subSequence(1, fullPath.length());
    }

    static String openDialog(String filterList, String defaultPath) {
        SceneRuntime.onStop();
        ACTIVE_SCENE.STATE = SceneState.WAITING;
        String path = null;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer buffer = stack.mallocPointer(1);
            int result = NativeFileDialog.NFD_OpenDialog(filterList, defaultPath, buffer);
            if (result == NativeFileDialog.NFD_OKAY) {
                path = buffer.getStringASCII();
            }
        }
        ACTIVE_SCENE.STATE = SceneState.IDLE;
        return path;
    }

    static String saveDialog(String filterList, String defaultPath) {
        SceneRuntime.onStop();
        ACTIVE_SCENE.STATE = SceneState.WAITING;
        String path = null;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer buffer = stack.mallocPointer(1);
            int result = NativeFileDialog.NFD_SaveDialog(filterList, defaultPath, buffer);
            if (result == NativeFileDialog.NFD_OKAY) {
                path = buffer.getStringASCII();
            }
        }
        ACTIVE_SCENE.STATE = SceneState.IDLE;
        return path;
    }
}
