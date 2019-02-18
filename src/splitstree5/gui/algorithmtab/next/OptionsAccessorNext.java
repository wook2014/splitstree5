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

/*
 *  Copyright (C) 2019 Daniel H. Huson
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

package splitstree5.gui.algorithmtab.next;

import javafx.beans.property.Property;
import jloda.util.Basic;
import splitstree5.utils.IOptionable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * gets all the options of an optionable class
 * Daniel Huson, 2.2019
 */
public class OptionsAccessorNext {
    /**
     * gets all options associated with an optionable.
     * An option is given by a getOption/setOptionValue pair of methods
     *
     * @param optionable
     * @return options
     */
    public static ArrayList<OptionNext> getAllOptions(IOptionable optionable) {
        final Map<String, OptionNext> name2AnOption = new TreeMap<>();

        Method listMethod = null;

        final ArrayList<OptionNext> options = new ArrayList<>();
        Method tooltipMethod = null;
        try {
            tooltipMethod = optionable.getClass().getMethod("getToolTip", String.class);
        } catch (Exception ex) {
            Basic.caught(ex);
        }

        for (Method method : optionable.getClass().getMethods()) {
            final String methodName = method.getName();
            if (methodName.startsWith("option") && methodName.endsWith("Property") && method.getParameterCount() == 0) {
                final String optionName = methodName.replaceAll("^option", "").replaceAll("Property$", "");

                try {
                    final String toolTipText = (tooltipMethod != null ? tooltipMethod.invoke(optionable, optionName).toString() : null);
                    final OptionNext option = new OptionNext((Property) method.invoke(optionable), optionName, toolTipText);
                    options.add(option);
                    name2AnOption.put(optionName, option);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    Basic.caught(e);
                }
            } else if (methodName.equals("listOptions") && method.getParameterCount() == 0)
                listMethod = method;
        }

        // determine the order in which to return options
        final Collection<String> order = new ArrayList<>(name2AnOption.size());
        if (listMethod == null) {
            order.addAll(name2AnOption.keySet());
        } else {
            try {
                final Set<String> set = new HashSet<>();
                final List list = (List) listMethod.invoke(optionable);
                if (list != null) {
                    for (Object a : list) {
                        String optionName = a.toString();
                        if (optionName.startsWith("option"))
                            optionName = optionName.replaceAll("^option", "").replaceAll("Property$", "");
                        order.add(optionName);
                        set.add(optionName);
                    }
                }
                // add other parameters not mentioned in the order
                for (String name : name2AnOption.keySet()) {
                    if (!set.contains(name))
                        order.add(name);
                }

            } catch (Exception e) {
                order.clear();
                order.addAll(name2AnOption.keySet());
            }
        }
        options.clear();
        for (String name : order) {
            options.add(name2AnOption.get(name));
        }
        return options;
    }
}
