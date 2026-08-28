package IllustrationShape;

public final class ShapeFactory {
    private ShapeFactory() {}

    public static HViewShape create(ShapeType type, int x, int y, int width, int height) {
        return type.create(x, y, width, height);
    }
}