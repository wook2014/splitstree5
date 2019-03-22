/*
 *  Copyright (C) 2018 Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jloda.fx;


import javafx.stage.Stage;
import jloda.util.Basic;

import java.io.IOException;

public class WindowGeometry {
    private double x;
    private double y;
    private double width;
    private double height;

    public String toString() {
        return String.format("%.1f %.1f %.1f %.1f", x, y, width, height);
    }

    public void fromString(String text) throws IOException {
        final String[] tokens = text.split("\\s+");
        if (tokens.length == 4) {
            x = Basic.parseDouble(tokens[0]);
            y = Basic.parseDouble(tokens[0]);
            width = Basic.parseDouble(tokens[0]);
            height = Basic.parseDouble(tokens[0]);
        } else
            throw new IOException("Invalid geometry string: " + text);
    }

    public void fromStage(Stage stage) {
        x = stage.getX();
        y = stage.getY();
        width = stage.getWidth();
        height = stage.getHeight();
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }
}
