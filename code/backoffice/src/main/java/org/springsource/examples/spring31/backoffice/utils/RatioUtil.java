package org.springsource.examples.spring31.backoffice.utils;

import java.awt.*;

/**
 * The RatioUtil class contains static utility functions for manipulating and determining ratios.
 * <p/>
 * Ported from https://github.com/mattupstate/AS3-Toolkit/blob/2866650232d61838a1f252fb736c76aa04592dbf/src/net/nobien/utils/RatioUtil.as
 *
 * @author Josh Long
 */
public abstract class RatioUtil {
    /**
     * Determines the ratio of width to height.
     *
     * @param size: The area's width and height expressed as a Rectangle. The Rectangle's x and y values are ignored.
     * @return The ratio of width to height
     */
    public static double widthToHeight(Rectangle size) {
        return (double) size.width / (double) size.height;
    }

    /**
     * Determines the ratio of height to width.
     *
     * @param size: The area's width and height expressed as a Rectangle. The Rectangle's x and y values are ignored.
     * @return The ratio of height to width
     */
    public static double heightToWidth(Rectangle size) {
        return ((double) size.height) / ((double) size.width);
    }

    /**
     * Scales an area's width and height while preserving aspect ratio.
     *
     * @param size:   The area's width and height expressed as a Rectangle. The Rectangle's x and y values are ignored.
     * @param amount: The amount you wish to scale by.
     * @return The scaled Rectangle
     */
    public static Rectangle scale(Rectangle size, double amount) {
        Rectangle scaled = clone(size);
        int scaledWidth = (int) (((double) scaled.width) * amount);
        int scaledHeight = (int) (((double) scaled.height) * amount);
        scaled.setSize(scaledWidth, scaledHeight);
        return scaled;
    }

    private static Rectangle clone(Rectangle r) {
        Rectangle rectangle = new Rectangle();
        rectangle.setRect(r.getBounds());
        return rectangle;
    }

    /**
     * Scales the width of an area while preserving aspect ratio.
     *
     * @param size:   The area's width and height expressed as a Rectangle. The Rectangle's x and y values are ignored.
     * @param height: The new height of the area.
     * @return The scaled Rectangle
     */
    public static Rectangle scaleWidth(Rectangle size, double height) {
        Rectangle scaled = (Rectangle) size.clone();
        double ratio = RatioUtil.widthToHeight(size);
        scaled.setSize((int) (height * ratio), (int) height);
        return scaled;
    }

    /**
     * Scales the height of an area while preserving aspect ratio.
     *
     * @param size:  The area's width and height expressed as a Rectangle. The Rectangle's x and y values are ignored.
     * @param width: The new width of the area.
     * @return The scaled Rectangle
     */
    public static Rectangle scaleHeight(Rectangle size, double width) {
        Rectangle scaled = (Rectangle) size.clone();
        double ratio = RatioUtil.heightToWidth(size);
        scaled.setSize((int) width, (int) (width * ratio));
        return scaled;
    }

    /**
     * Resizes an area to fill the bounding area while preserving aspect ratio.
     *
     * @param size:   The area's width and height expressed as a Rectangle. The Rectangle's x and y values are ignored.
     * @param bounds: The area to fill. The Rectangle's x and y values are ignored.
     * @return The resized Rectangle
     */
    public static Rectangle scaleToFill(Rectangle size, Rectangle bounds) {
        Rectangle scaled = RatioUtil.scaleHeight(size, bounds.width);

        if (scaled.height < bounds.height)
            scaled = RatioUtil.scaleWidth(size, bounds.height);

        return scaled;
    }

    /**
     * Resizes an area to the maximum size of a bounding area without exceeding while preserving aspect ratio.
     *
     * @param size:   The area's width and height expressed as a Rectangle. The Rectangle's x and y values are ignored.
     * @param bounds: The area the rectangle needs to fit within. The Rectangle's x and y values are ignored.
     * @return The resized Rectangle
     */
    public static Rectangle scaleToFit(Rectangle size, Rectangle bounds) {
        Rectangle scaled = RatioUtil.scaleHeight(size, bounds.width);

        if (scaled.height > bounds.height)
            scaled = RatioUtil.scaleWidth(size, bounds.height);

        return scaled;
    }
}
