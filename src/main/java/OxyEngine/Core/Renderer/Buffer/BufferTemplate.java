package OxyEngine.Core.Renderer.Buffer;

@FunctionalInterface
public interface BufferTemplate {

    record Attributes(int index, int size, int type, boolean normalized, int stride, long pointer) {
    }

    enum Usage {
        DYNAMIC(), STATIC()
    }

    interface BufferTemplateBuilder {
        BufferTemplateImpl setAttribPointer(Attributes... attribPointers);

        BufferTemplateImpl setUsage(Usage usage);

        BufferTemplateImpl setVerticesStrideSize(int size);
    }

    class BufferTemplateImpl implements BufferTemplateBuilder {

        private int strideSize = -1;
        private Attributes[] attribPointers;
        private Usage usage;

        public BufferTemplateImpl() {
        }

        @Override
        public BufferTemplateImpl setAttribPointer(Attributes... attribPointers) {
            this.attribPointers = attribPointers;
            return this;
        }

        @Override
        public BufferTemplateImpl setUsage(Usage usage) {
            this.usage = usage;
            return this;
        }

        @Override
        public BufferTemplateImpl setVerticesStrideSize(int size) {
            this.strideSize = size;
            return this;
        }

        public Attributes[] getAttribPointers() {
            return attribPointers;
        }

        public Usage getUsage() {
            return usage;
        }

        public int getStrideSize() {
            return strideSize;
        }
    }

    BufferTemplateImpl setup();
}
