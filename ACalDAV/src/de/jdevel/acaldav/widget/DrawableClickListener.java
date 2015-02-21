package de.jdevel.acaldav.widget;

/**
 * @author Joseph Weigl
 */
public interface DrawableClickListener {

    public static enum DrawablePosition {TOP, BOTTOM, LEFT, RIGHT}

    public void onClick(DrawablePosition target);
}
