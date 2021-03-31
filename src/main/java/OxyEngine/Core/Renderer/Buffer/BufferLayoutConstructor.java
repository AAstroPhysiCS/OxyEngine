package OxyEngine.Core.Renderer.Buffer;

import OxyEngine.Core.Renderer.Buffer.Platform.BufferConstructor;

import java.util.ArrayList;
import java.util.List;

public class BufferLayoutConstructor {

    private BufferLayoutConstructor() {
    }

    public enum Usage {
        DYNAMIC(), STATIC()
    }

    public interface BufferLayout {

        BufferLayoutImpl setAttribPointer(BufferLayoutAttributes... attribPointers);

        BufferLayoutImpl setUsage(Usage usage);

        BufferLayoutBuilder create();
    }

    public interface BufferLayoutBuilder {
        BufferLayout createLayout(Class<? extends Buffer> destClass);

        BufferLayoutRecord finalizeRecord();
    }

    public static class BufferLayoutImpl implements BufferLayout {

        private BufferLayoutAttributes[] attribPointers;
        private Usage usage;

        private final BufferLayoutBuilderImpl src;
        private final Class<? extends Buffer> destClass;

        public BufferLayoutImpl(Class<? extends Buffer> destClass, BufferLayoutBuilderImpl src) {
            this.src = src;
            this.destClass = destClass;
        }

        @Override
        public BufferLayoutImpl setAttribPointer(BufferLayoutAttributes... attribPointers) {
            this.attribPointers = attribPointers;
            return this;
        }

        @Override
        public BufferLayoutImpl setUsage(Usage usage) {
            this.usage = usage;
            return this;
        }

        @Override
        public BufferLayoutBuilder create() {
            return src;
        }

        public BufferLayoutAttributes[] getAttribPointers() {
            return attribPointers;
        }

        public Usage getUsage() {
            return usage;
        }
    }

    public static class BufferLayoutBuilderImpl implements BufferLayoutBuilder {

        private final List<BufferLayoutImpl> implementations = new ArrayList<>();

        @Override
        public BufferLayout createLayout(Class<? extends Buffer> destClass) {
            BufferLayoutImpl impl = new BufferLayoutImpl(destClass, this);
            implementations.add(impl);
            return impl;
        }

        @Override
        public BufferLayoutRecord finalizeRecord() {

            BufferLayoutImpl vertexLayout = null, normalsLayout = null, tangentLayout = null, textureLayout = null, indexLayout = null;
            for (BufferLayoutImpl impl : implementations) {
                if (VertexBuffer.class.equals(impl.destClass)) {
                    vertexLayout = impl;
                } else if (TangentBuffer.class.equals(impl.destClass)) {
                    tangentLayout = impl;
                } else if (NormalsBuffer.class.equals(impl.destClass)) {
                    normalsLayout = impl;
                } else if (TextureBuffer.class.equals(impl.destClass)) {
                    textureLayout = impl;
                } else if (IndexBuffer.class.equals(impl.destClass)){
                    indexLayout = impl;
                }
            }

            VertexBuffer vertexBuffer = BufferConstructor.createVertexBuffer(vertexLayout);
            IndexBuffer indexBuffer = BufferConstructor.createIndexBuffer(indexLayout);
            TangentBuffer tangentBuffer = BufferConstructor.createTangentBuffer(tangentLayout);
            NormalsBuffer normalsBuffer = BufferConstructor.createNormalsBuffer(normalsLayout);
            TextureBuffer textureBuffer = BufferConstructor.createTextureBuffer(textureLayout);

            return new BufferLayoutRecord(vertexBuffer, indexBuffer, textureBuffer, tangentBuffer, normalsBuffer);
        }
    }

    public static BufferLayoutBuilder create() {
        return new BufferLayoutBuilderImpl();
    }
}
