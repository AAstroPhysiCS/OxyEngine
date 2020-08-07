package OxyEngine.Core.Renderer.Texture;

public enum OxyTextureCoords {
    FULL(1), CUBE(2);

    private float[] tcs;

    OxyTextureCoords(int id) {
        switch (id) {
            case 1 -> tcs = new float[]{
                    0, 0,
                    0, 1,
                    1, 0,
                    1, 1,

                    0, 0,
                    0, 1,
                    1, 0,
                    1, 1,

                    0, 0,
                    0, 1,
                    1, 0,
                    1, 1,

                    0, 0,
                    0, 1,
                    1, 0,
                    1, 1,

                    0, 0,
                    0, 1,
                    1, 0,
                    1, 1,

                    0, 0,
                    0, 1,
                    1, 0,
                    1, 1
            };
            case 2 -> tcs = new float[]{
                    0.063f, 0,
                    0.048f, 0,
                    0.063f, 0.015f,
                    0.048f, 0.015f,

                    0.063f, 0,
                    0.048f, 0,
                    0.063f, 0.015f,
                    0.048f, 0.015f,

                    0.063f, 0,
                    0.048f, 0,
                    0.063f, 0.015f,
                    0.048f, 0.015f,

                    0.063f, 0,
                    0.048f, 0,
                    0.063f, 0.015f,
                    0.048f, 0.015f,

                    0.145f, 0.177f,
                    0.160f, 0.177f,
                    0.145f, 0.191f,
                    0.160f, 0.191f,

                    0.047f, 0,
                    0.032f, 0,
                    0.047f, 0.015f,
                    0.032f, 0.015f,
            };
        }
    }

    public float[] getTcs() {
        return tcs;
    }
}
