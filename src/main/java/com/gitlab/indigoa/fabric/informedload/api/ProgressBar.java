package com.gitlab.indigoa.fabric.informedload.api;

import com.gitlab.indigoa.fabric.informedload.InformedLoadUtils;
import net.minecraft.client.util.Window;

import java.awt.Color;


public class ProgressBar {
    public enum SplitType {
        NONE, LEFT, RIGHT
    }
    protected int x, y, xm, ym;
    protected float progress = 0;
    protected String text = null;
    protected Color outer, inner;
    public ProgressBar(int x, int y, int xm, Color outer, Color inner) {
        this.x = x;
        this.y = y;
        this.xm = xm;
        this.ym = y + 10;
        this.outer = outer;
        this.inner = inner;
    }
    public ProgressBar(int x, int y, int xm) {
        this(x, y, xm, Color.WHITE, new Color(226, 40, 55));
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
    public void render() {
        InformedLoadUtils.makeProgressBar(x, y, xm, ym, progress, text, 1, outer, inner);
    }
    public static ProgressBar createProgressBar(Window window, int y, SplitType splitType) {
        switch (splitType) {
            case LEFT:
                return new ProgressBar(window.getScaledWidth() / 2 - 150, y, window.getScaledWidth() / 2 - 5);
            case RIGHT:
                return new ProgressBar(window.getScaledWidth() / 2 + 5, y, window.getScaledWidth() / 2 + 150);
            default:
                return new ProgressBar(window.getScaledWidth() / 2 - 150, y, window.getScaledWidth() / 2 + 150);
        }
    }
}
