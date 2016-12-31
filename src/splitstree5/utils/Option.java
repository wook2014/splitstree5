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

package splitstree5.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * extract all properties in a class by reflection
 * Created by huson on 12/31/16.
 */
public class Option {

    private final String name;
    private final Object object;
    private final Method getMethod;
    private Object newValue;
    private final Method setMethod;
    private final Type type;

    /**
     * constructors an option
     *
     * @param name
     * @param object
     * @param getMethod
     * @param setMethod
     */
    private Option(String name, Object object, Method getMethod, Method setMethod, Type type) {
        this.name = name;
        this.object = object;
        this.getMethod = getMethod;
        this.setMethod = setMethod;
        this.type = type;
    }

    /**
     * sets the name of an option
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * gets the value of an option
     *
     * @return value
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public Object getValue() throws InvocationTargetException, IllegalAccessException {
        return getMethod.invoke(object);
    }

    /**
     * sets a new value for the option, but does not write it back. Need to call setValue() for this value to be set
     *
     * @param newValue
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public void holdValue(Object newValue) {
        this.newValue = newValue;
    }

    /**
     * writes back a new value, if it was set
     *
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public void setValue() throws InvocationTargetException, IllegalAccessException {
        if (newValue != null)
            setMethod.invoke(object, newValue);
    }

    /**
     * writes back the given value
     *
     * @param newValue
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public void setValue(Object newValue) throws InvocationTargetException, IllegalAccessException {
        holdValue(newValue);
        setValue();
    }


    /**
     * gets the type of this option
     *
     * @return type
     */
    public Type getType() {
        return type;
    }

    public String toString() {
        try {
            return "name=" + name + " getMethod=" + getMethod + " setMethod=" + setMethod + " type=" + type.getTypeName() + " value=" + getValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * gets all options associated with an object.
     * An option is given by a getOption/setOption pair of methods
     *
     * @param object
     * @return options
     */
    public static ArrayList<Option> getAllOptions(Object object) {
        final Map<String, Method> name2GetMethod = new HashMap<>();
        final Map<String, Method> name2SetMethod = new HashMap<>();

        final ArrayList<Option> options = new ArrayList<>();
        for (Method method : object.getClass().getMethods()) {
            String name = method.getName();
            if (name.startsWith("getOption") && method.getParameterCount() == 0) {
                name = name.substring("getOption".length());
                if (name.length() > 0)
                    name2GetMethod.put(name, method);
            } else if (name.startsWith("isOption") && method.getParameterCount() == 0) {
                name = name.substring("isOption".length());
                if (name.length() > 0)
                    name2GetMethod.put(name, method);
            } else if (name.startsWith("setOption") && method.getParameterCount() == 1) {
                name = name.substring("setOption".length());
                if (name.length() > 0)
                    name2SetMethod.put(name, method);
            }
        }
        for (String name : name2GetMethod.keySet()) {
            if (name2SetMethod.keySet().contains(name)) {
                options.add(new Option(name, object, name2GetMethod.get(name), name2SetMethod.get(name), name2GetMethod.get(name).getReturnType()));
            }
        }
        return options;
    }
}
