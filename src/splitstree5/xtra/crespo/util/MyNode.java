/*
 *  MyNode.java Copyright (C) 2019 Daniel H. Huson
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

package splitstree5.xtra.crespo.util;

import java.util.ArrayList;

public class MyNode {

    private String id = "";
    private double branchLength = 1.;
    private ArrayList<MyNode> children = new ArrayList<MyNode>();
    private MyNode parent;

    private Object[] distortionOperation;

    public MyNode() {
    }

    public MyNode(String id) {
        this.id = id;
    }

    public void addChildren(MyNode v) {
        if (!children.contains(v)) {
            children.add(v);
            v.setParent(this);
        }
    }

    public String toNewick(String newickString) {
        if (children.isEmpty() && parent == null)
            return "(" + newickLabel() + ")";
        else if (children.isEmpty()) {
            return newickLabel();
        } else {
            String subString = "(";
            for (MyNode c : children) {
                if (children.indexOf(c) == children.size() - 1)
                    subString = subString.concat(c.toNewick(newickString) + ")");
                else
                    subString = subString.concat(c.toNewick(newickString) + ",");
            }
            return subString + newickLabel();
        }
    }

    public void removeChild(MyNode c) {
        children.remove(c);
        c.setParent(null);
    }

    public MyNode copy() {
        MyNode vCopy = new MyNode();
        vCopy.setID(this.id);
        vCopy.setBranchLength(this.getBranchLength());
        return vCopy;
    }

    private String newickLabel() {
        return id + ":" + branchLength;
    }

    public ArrayList<MyNode> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<MyNode> children) {
        this.children = children;
    }

    public MyNode getParent() {
        return parent;
    }

    public void setParent(MyNode parent) {
        this.parent = parent;
    }

    public String getId() {
        return id;
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public double getBranchLength() {
        return branchLength;
    }

    public void setBranchLength(double branchLength) {
        this.branchLength = branchLength;
    }

    public void setID(String id) {
        this.id = id;
    }

    public Object[] getDistortionOperation() {
        return distortionOperation;
    }

    public void setDistortionOperation(Object[] distortionOperation) {
        this.distortionOperation = distortionOperation;
    }

}
