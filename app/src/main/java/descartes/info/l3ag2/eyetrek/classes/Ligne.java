package descartes.info.l3ag2.eyetrek.classes;

import org.opencv.core.Point;

/**
 * Cette classe sera utiliser pour l'astronomie pour tracer les traits
 */
public class Ligne {
    Point point_a;
    Point point_b;

    public Ligne(Point point_a, Point point_b) {
        this.point_a = point_a;
        this.point_b = point_b;
    }

    public Point getPoint_a() {
        return point_a;
    }

    public Point getPoint_b() {
        return point_b;
    }

    public void setPoint_a(Point point_a) {
        this.point_a = point_a;
    }

    public void setPoint_b(Point point_b) {
        this.point_b = point_b;
    }

}
