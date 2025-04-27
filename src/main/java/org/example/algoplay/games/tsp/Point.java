package org.example.algoplay.games.tsp;

/**
 * Model class representing a point with x and y coordinates
 */
public class Point {
    public double x, y;

    /**
     * Creates a new point with the specified coordinates
     *
     * @param x The x-coordinate
     * @param y The y-coordinate
     */
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Calculate the Euclidean distance to another point
     *
     * @param other The other point
     * @return The distance between this point and the other point
     */
    public double distanceTo(Point other) {
        double dx = x - other.x;
        double dy = y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}