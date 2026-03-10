/*
 * Copyright 2026 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.apzda.hajimi.captcha.utils;

import cn.hutool.core.img.FontUtil;
import jakarta.annotation.Nonnull;
import lombok.val;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author fengz (windywany@gmail.com)
 * @version 1.0.0
 * @since 1.0.0
 **/
public abstract class ImageUtil {

    public static void watermark(@Nonnull BufferedImage oriImage, String text) {
        val graphics2D = oriImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.setColor(Color.black);
        graphics2D.setFont(FontUtil.createSansSerifFont(30).deriveFont(Font.BOLD));
        graphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.4f));
        graphics2D.drawString(text, 10, 40);
        graphics2D.dispose();
    }

    public static void interfereTemplate(@Nonnull BufferedImage oriImage, @Nonnull BufferedImage templateImage, int x,
            int y) {
        val matrix = new int[3][3];
        val values = new int[9];

        val xLength = templateImage.getWidth();
        val yLength = templateImage.getHeight();
        for (int i = 0; i < xLength; i++) {
            for (int j = 0; j < yLength; j++) {
                int rgb = templateImage.getRGB(i, j);
                if (rgb < 0) {
                    readPixel(oriImage, x + i, y + j, values);
                    fillMatrix(matrix, values);
                    oriImage.setRGB(x + i, y + j, avgMatrix(matrix));
                }
            }
        }
    }

    public static void cutByTemplate(@Nonnull BufferedImage oriImage, @Nonnull BufferedImage templateImage,
            BufferedImage newImage, int x, int y) {
        val matrix = new int[3][3];
        val values = new int[9];

        val xLength = templateImage.getWidth();
        val yLength = templateImage.getHeight();
        for (var i = 0; i < xLength; i++) {
            for (int j = 0; j < yLength; j++) {
                val rgb = templateImage.getRGB(i, j);
                if (rgb < 0) {
                    newImage.setRGB(i, j, oriImage.getRGB(x + i, y + j));

                    readPixel(oriImage, x + i, y + j, values);
                    fillMatrix(matrix, values);
                    oriImage.setRGB(x + i, y + j, avgMatrix(matrix));
                }

                if (i == (xLength - 1) || j == (yLength - 1)) {
                    continue;
                }
                val rightRgb = templateImage.getRGB(i + 1, j);
                val downRgb = templateImage.getRGB(i, j + 1);
                val rgbImage = ((rgb >= 0 && rightRgb < 0) || (rgb < 0 && rightRgb >= 0) || (rgb >= 0 && downRgb < 0)
                        || (rgb < 0 && downRgb >= 0));

                if (rgbImage) {
                    newImage.setRGB(i, j, Color.GRAY.getRGB());
                }
            }
        }
    }

    public static void readPixel(BufferedImage img, int x, int y, int[] pixels) {
        val xStart = x - 1;
        val yStart = y - 1;
        var current = 0;
        for (var i = xStart; i < 3 + xStart; i++) {
            for (var j = yStart; j < 3 + yStart; j++) {
                var tx = i;
                if (tx < 0) {
                    tx = -tx;

                }
                else if (tx >= img.getWidth()) {
                    tx = x;
                }
                int ty = j;
                if (ty < 0) {
                    ty = -ty;
                }
                else if (ty >= img.getHeight()) {
                    ty = y;
                }
                pixels[current++] = img.getRGB(tx, ty);
            }
        }
    }

    public static void fillMatrix(@Nonnull int[][] matrix, int[] values) {
        var filled = 0;
        for (int[] x : matrix) {
            for (int j = 0; j < x.length; j++) {
                x[j] = values[filled++];
            }
        }
    }

    public static int avgMatrix(@Nonnull int[][] matrix) {
        int r = 0;
        int g = 0;
        int b = 0;
        for (int[] x : matrix) {
            for (int j = 0; j < x.length; j++) {
                if (j == 1) {
                    continue;
                }
                Color c = new Color(x[j]);
                r += c.getRed();
                g += c.getGreen();
                b += c.getBlue();
            }
        }
        return new Color(r / 8, g / 8, b / 8).getRGB();
    }

}
