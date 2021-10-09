package androidx.constraintlayout.solver.widgets;

public class Rectangle {
    public int height;
    public int width;

    /* renamed from: x */
    public int f260x;

    /* renamed from: y */
    public int f261y;

    public void setBounds(int x, int y, int width2, int height2) {
        this.f260x = x;
        this.f261y = y;
        this.width = width2;
        this.height = height2;
    }

    /* access modifiers changed from: package-private */
    public void grow(int w, int h) {
        this.f260x -= w;
        this.f261y -= h;
        this.width += w * 2;
        this.height += h * 2;
    }

    /* access modifiers changed from: package-private */
    public boolean intersects(Rectangle bounds) {
        return this.f260x >= bounds.f260x && this.f260x < bounds.f260x + bounds.width && this.f261y >= bounds.f261y && this.f261y < bounds.f261y + bounds.height;
    }

    public boolean contains(int x, int y) {
        return x >= this.f260x && x < this.f260x + this.width && y >= this.f261y && y < this.f261y + this.height;
    }

    public int getCenterX() {
        return (this.f260x + this.width) / 2;
    }

    public int getCenterY() {
        return (this.f261y + this.height) / 2;
    }
}
