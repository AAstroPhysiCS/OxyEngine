package OxyEngine.Core.Context.Renderer.Texture;

import OxyEngine.Core.Context.OxyRenderer;
import OxyEngine.TargetPlatform;

public interface TextureParameterBuilder {

    TextureParameterBuilder setWrapS(TextureParameter parameter);

    TextureParameterBuilder setWrapR(TextureParameter parameter);

    TextureParameterBuilder setWrapT(TextureParameter parameter);

    TextureParameterBuilder setMinFilter(TextureParameter parameter);

    TextureParameterBuilder setMagFilter(TextureParameter parameter);

    TextureParameterBuilder setLODBias(float bias);

    TextureParameterBuilder enableMipMap(boolean mipMap);

    static <T extends TextureParameterBuilder> T create() {
        if (OxyRenderer.getCurrentTargetPlatform() == TargetPlatform.OpenGL) {
            return (T) new POpenGL();
        }
        throw new IllegalStateException("API not supported yet!");
    }

    final class POpenGL implements TextureParameterBuilder {

        public boolean generateMipMap;
        TextureParameter wrapSParameter, wrapRParameter, wrapTParameter;
        TextureParameter minFilterParameter, magFilterParameter;
        float lodBias = -1;

        @Override
        public TextureParameterBuilder setWrapS(TextureParameter parameter) {
            this.wrapSParameter = parameter;
            return this;
        }

        @Override
        public TextureParameterBuilder setWrapR(TextureParameter parameter) {
            this.wrapRParameter = parameter;
            return this;
        }

        @Override
        public TextureParameterBuilder setWrapT(TextureParameter parameter) {
            this.wrapTParameter = parameter;
            return this;
        }

        @Override
        public TextureParameterBuilder setMinFilter(TextureParameter parameter) {
            this.minFilterParameter = parameter;
            return this;
        }

        @Override
        public TextureParameterBuilder setMagFilter(TextureParameter parameter) {
            this.magFilterParameter = parameter;
            return this;
        }

        @Override
        public TextureParameterBuilder setLODBias(float bias) {
            this.lodBias = bias;
            return this;
        }

        @Override
        public TextureParameterBuilder enableMipMap(boolean mipMap) {
            this.generateMipMap = mipMap;
            return this;
        }
    }
}
