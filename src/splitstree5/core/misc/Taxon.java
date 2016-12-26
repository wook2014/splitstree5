/*
 *  Copyright (C) 2016 Daniel H. Huson
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

package splitstree5.core.misc;

import java.io.Serializable;

/**
 * a taxon
 * Created by huson on Dec-2016
 */
public class Taxon implements Serializable {
    private String name;
    private String info;

    public Taxon() {
    }

    public Taxon(String name) {
        setName(name);
    }

    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getInfo() {
        return info;
    }


    public void setInfo(String info) {
        this.info = info;
    }

    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Taxon) {
            Taxon that = (Taxon) other;
            return this.getName().equals(that.getName());
        } else
            return false;
    }
}
