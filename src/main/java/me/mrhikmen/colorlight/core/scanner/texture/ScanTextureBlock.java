package me.mrhikmen.colorlight.core.scanner.texture;

import com.mojang.blaze3d.platform.NativeImage;
import me.mrhikmen.colorlight.ColorLightClient;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ScanTextureBlock {

    public static class TextureData {

        public final int width;
        public final int height;

        public final List<PixelData> pixels;
        public final PixelData[][] image;

        public TextureData(int width, int height, List<PixelData> pixels, PixelData[][] image) {
            this.width = width;
            this.height = height;
            this.pixels = pixels;
            this.image = image;
        }
    }

    public static TextureData scan(Identifier texture) {

        Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(texture);

        if (resource.isEmpty()) {
            ColorLightClient.LOGGER.warn("[ColorLight] Missing texture {}, block skipped.", texture);
            return null;
        }

        try (InputStream stream = resource.get().open()) {

            NativeImage image = NativeImage.read(stream);

            int width = image.getWidth();
            int height = image.getHeight();

            PixelData[][] map = new PixelData[width][height];
            List<PixelData> pixels = new ArrayList<>(width * height);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgba = image.getPixel(x, y);

                    int a = (rgba >>> 24) & 255;
                    int b = (rgba >>> 16) & 255;
                    int g = (rgba >>> 8) & 255;
                    int r = rgba & 255;

                    PixelData pixel = new PixelData(x, y, b, g, r, a);

                    pixels.add(pixel);
                    map[x][y] = pixel;
                }
            }
            image.close();

            return new TextureData(width, height, pixels, map);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static double brightness(PixelData p) {
        return (0.2126 * p.r + 0.7152 * p.g + 0.0722 * p.b) / 255.0;
    }

    public static double localBrightness(PixelData[][] image, PixelData center, int width, int height) {

        double sum = 0;
        int count = 0;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {

                int nx = center.x + dx;
                int ny = center.y + dy;

                if (nx < 0 || ny < 0)
                    continue;

                if (nx >= width || ny >= height)
                    continue;

                PixelData p = image[nx][ny];

                if (p == null || !p.isVisible())
                    continue;

                sum += brightness(p);
                count++;
            }
        }
        return count == 0 ? 0 : sum / count;
    }
    public static class Component {

        public final List<PixelData> pixels = new ArrayList<>();
        public int size() {
            return pixels.size();
        }
    }

    private static final int COLOR_DISTANCE = 70;
    private static boolean colorDistance(PixelData a, PixelData b) {

        int dr = a.r - b.r;
        int dg = a.g - b.g;
        int db = a.b - b.b;

        return Math.sqrt(dr * dr + dg * dg + db * db) <= COLOR_DISTANCE;
    }

    public static Component component(PixelData start, PixelData[][] image, boolean[][] visited, int width, int height) {

        Component component = new Component();

        ArrayDeque<PixelData> queue = new ArrayDeque<>();

        queue.add(start);
        visited[start.x][start.y] = true;

        while (!queue.isEmpty()) {

            PixelData current = queue.poll();
            component.pixels.add(current);

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0)
                        continue;

                    int nx = current.x + dx;
                    int ny = current.y + dy;

                    if (nx < 0 || ny < 0)
                        continue;

                    if (nx >= width || ny >= height)
                        continue;

                    if (visited[nx][ny])
                        continue;

                    PixelData neighbour = image[nx][ny];

                    if (neighbour == null)
                        continue;

                    if (!neighbour.isVisible())
                        continue;

                    if (!colorDistance(start, neighbour))
                        continue;

                    visited[nx][ny] = true;

                    queue.add(neighbour);
                }
            }
        }
        return component;
    }

    public static List<Component> findComponents(TextureData texture) {

        List<Component> components = new ArrayList<>();
        boolean[][] visited = new boolean[texture.width][texture.height];

        for (PixelData pixel : texture.pixels) {
            if (!pixel.isVisible())
                continue;

            if (visited[pixel.x][pixel.y])
                continue;

            Component component = component(pixel, texture.image, visited, texture.width, texture.height);

            if (!component.pixels.isEmpty())
                components.add(component);
        }
        return components;
    }

    public static int largestComponent(List<Component> components) {

        int max = 1;

        for (Component component : components)
            max = Math.max(max, component.size());


        return max;
    }

    public static double anomaly(PixelData p, int avgR, int avgG, int avgB) {

        double dr = p.r - avgR;
        double dg = p.g - avgG;
        double db = p.b - avgB;

        double distance = Math.sqrt(dr * dr + dg * dg + db * db);

        return distance / 441.67;
    }
    public static double saturation(PixelData p) {

        int max = Math.max(p.r, Math.max(p.g, p.b));
        int min = Math.min(p.r, Math.min(p.g, p.b));

        return (max - min) / 255.0;
    }

    public static double whitePenalty(PixelData p) {

        int max = Math.max(p.r, Math.max(p.g, p.b));

        int min = Math.min(p.r, Math.min(p.g, p.b));


        double saturation = (max - min) / 255.0;

        if (saturation < 0.08 && brightness(p) > 0.85)
            return 0.3;


        return 1.0;
    }

    public static double glowColorScore(PixelData p) {

        double saturation = saturation(p);

        double bright = brightness(p);

        return bright * 0.6 + saturation * 0.4;
    }
}