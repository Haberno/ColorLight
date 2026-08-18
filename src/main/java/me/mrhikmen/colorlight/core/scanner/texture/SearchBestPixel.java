package me.mrhikmen.colorlight.core.scanner.texture;

import me.mrhikmen.colorlight.ColorLightClient;

import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchBestPixel {

    public static PixelData search(Identifier textureId) {

        Identifier fileId = Identifier.fromNamespaceAndPath(textureId.getNamespace(), "textures/" + textureId.getPath() + ".png");

        ScanTextureBlock.TextureData texture = ScanTextureBlock.scan(fileId);

        if (texture == null || texture.pixels.isEmpty())
            return null;

        long avgR = 0;
        long avgG = 0;
        long avgB = 0;
        int count = 0;

        for(PixelData p : texture.pixels){
            if(!p.isVisible())
                continue;

            avgR += p.r;
            avgG += p.g;
            avgB += p.b;

            count++;
        }

        if(count == 0)
            return null;

        avgR /= count;
        avgG /= count;
        avgB /= count;

        List<ScanTextureBlock.Component> components = ScanTextureBlock.findComponents(texture);

        if (components.isEmpty())
            return null;

        int maxRegion = ScanTextureBlock.largestComponent(components);

        Map<PixelData, Integer> regionSize = new HashMap<>();

        for (ScanTextureBlock.Component component : components) {
            int size = component.size();

            for (PixelData pixel : component.pixels) {
                regionSize.put(pixel, size);
            }
        }

        PixelData best = null;

        for (PixelData pixel : texture.pixels) {

            if (!pixel.isVisible())
                continue;

            double brightness = ScanTextureBlock.brightness(pixel);

            double localBrightness = ScanTextureBlock.localBrightness(texture.image, pixel, texture.width, texture.height);

            double regionScore = regionSize.getOrDefault(pixel, 1) / (double) maxRegion;

            double anomaly = ScanTextureBlock.anomaly(pixel, (int)avgR, (int)avgG, (int)avgB);

            double alphaScore = pixel.a / 255.0;

            double saturation = ScanTextureBlock.saturation(pixel);

            double whitePenalty = ScanTextureBlock.whitePenalty(pixel);

            double glowColorScore = ScanTextureBlock.glowColorScore(pixel);

            pixel.score =
                            brightness * (ColorLightClient.config.BRIGHTNESS_WEIGHT / 100) +
                            localBrightness * (ColorLightClient.config.LOCAL_WEIGHT / 100) +
                            regionScore * (ColorLightClient.config.REGION_WEIGHT / 100) +
                            alphaScore * (ColorLightClient.config.ALPHA_WEIGHT / 100) +
                            anomaly * (ColorLightClient.config.ANOMALY_WEIGHT / 100) +
                            saturation * (ColorLightClient.config.SATURATION_WEIGHT / 100) +
                            glowColorScore * (ColorLightClient.config.GLOWCOLORSCORE_WEIGHT / 100) +
                            whitePenalty * (ColorLightClient.config.WHITEPENALTY_WEIGHT / 100);

            if (best == null || pixel.score > best.score) {
                best = pixel;
            }
        }
        return best;
    }
}