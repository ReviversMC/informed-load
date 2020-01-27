package io.github.giantnuker.fabric.informedload.api;

import io.github.giantnuker.fabric.informedload.InformedLoadUtils;
import net.minecraft.client.util.Window;

import java.awt.Color;


public abstract class ProgressBar {
    public enum SplitType {
        NONE, LEFT, RIGHT
    }
    protected float progress = 0;
    protected String text = null;
    protected Color outer, inner;
    public ProgressBar(Color outer, Color inner) {
        this.outer = outer;
        this.inner = inner;
    }
    public ProgressBar() {
        this(Color.WHITE, new Color(226, 40, 55));
    }
    public void setText(String text) {
        this.text = text;
    }
    public void setProgress(float progress) {
        this.progress = progress;
    }
    public String getText() {
        return text;
    }
    public float getProgress() {
        return progress;
    }
    public void render(Window window) {
        InformedLoadUtils.makeProgressBar(getX(window), getY(window), getMaxX(window), getY(window) + 10, progress, text, outer.getRGB(), inner.getRGB());
    }
    protected abstract int getX(Window window);
    protected abstract int getMaxX(Window window);
    protected abstract int getY(Window window);
    public static abstract class SplitProgressBar extends ProgressBar {
        public SplitProgressBar(SplitType splitType) {
            this.splitType = splitType;
        }
        public SplitType splitType;
        @Override
        protected int getX(Window window) {
            return window.getScaledWidth() / 2 + (splitType == SplitType.RIGHT ? 5 : -150);
        }

        @Override
        protected int getMaxX(Window window) {
            return window.getScaledWidth() / 2 + (splitType == SplitType.LEFT ? -5 : 150);
        }
    }
}
