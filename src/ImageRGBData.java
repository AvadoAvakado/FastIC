import java.awt.Color;
import java.awt.image.BufferedImage;

public class ImageRGBData {
    private final int[][] R;
    private final int[][] G;
    private final int[][] B;
    private final int width;
    private final int height;

    public ImageRGBData(final BufferedImage image) {
        width = image.getWidth();
        height = image.getHeight();
        R = new int[width][height];
        G = new int[width][height];
        B = new int[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                final int colorOfPixelInBiteFormat = image.getRGB(x, y);
                B[x][y] = colorOfPixelInBiteFormat & 0xff;
                G[x][y] = (colorOfPixelInBiteFormat & 0xff00) >> 8;
                R[x][y] = (colorOfPixelInBiteFormat & 0xff0000) >> 16;
            }
        }
    }

    private void verifyCoordinates(int width, int height) {
        if (width > this.width || height > this.height) {
            throw new IllegalArgumentException(String.format("Illegal coordinates: (width=%s, height=%s)\nImage size: width=%s, height=%s"
                    , width, height, this.width, this.height));
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    private int getColor(int[][] color, int width, int height) {
        verifyCoordinates(width, height);
        return color[width][height];
    }

    public int getRed(int width, int height) {
        return getColor(R, width, height);
    }

    public int getGreen(int width, int height) {
        return getColor(G, width, height);
    }

    public int getBlue(int width, int height) {
        return getColor(B, width, height);
    }

    public Color getColor(int width, int height) {
        return new Color(getRed(width, height), getGreen(width, height), getBlue(width, height));
    }
}