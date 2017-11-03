/*
 *  Copyright (C) 2017 Daniel H. Huson
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

import jloda.util.Basic;

import java.lang.reflect.Type;

/**
 * extract all properties in an Optionable class by reflection
 * Created by huson on 12/31/16.
 */
public class Option {
    private final String name;
    private final IOptionable optionable;
    private final IGetMethod<IOptionable, String> infoMethod;
    private final IGetMethod<IOptionable, Object> getMethod;
    private final ISetMethod<IOptionable, Object> setMethod;
    private final IGetMethod<IOptionable, String[]> legalValues;
    private final Type type;
    private Object newValue;

    /**
     * constructors an option
     *
     * @param name
     * @param optionable
     * @param getMethod
     * @param setMethod
     */
    Option(String name, IOptionable optionable, IGetMethod<IOptionable, Object> getMethod, ISetMethod<IOptionable, Object> setMethod, IGetMethod<IOptionable, String> infoMethod, IGetMethod<IOptionable, String[]> legalValuesMethod, Type type) {
        this.name = name;
        this.optionable = optionable;
        this.infoMethod = infoMethod;
        this.getMethod = getMethod;
        this.setMethod = setMethod;
        this.type = type;
        this.legalValues = legalValuesMethod;
    }

    /**
     * sets the name of the option
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * sets the description of the option
     *
     * @return
     */
    public String getInfo() {
        try {
            return (infoMethod == null ? null : infoMethod.invoke(optionable));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * get list of all legal values, if the values are constrained
     *
     * @return legal values (as strings) or null
     */
    public String[] getLegalValues() {
        try {
            return (legalValues == null ? null : legalValues.invoke(optionable));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * gets the value of an option
     *
     * @return value
     */
    public Object getValue() {
        try {
            return getMethod.invoke(optionable);
        } catch (Exception e) {
            Basic.caught(e);
            return null;
        }
    }

    /**
     * sets a new value for the option, but does not write it back. Need to call setValue() for this value to be set
     *
     * @param newValue
     */
    public void holdValue(Object newValue) {
        this.newValue = newValue;
    }

    /**
     * writes back a new value, if it was set
     */
    public void setValue() {
        if (newValue != null) {
            try {
                setMethod.invoke(optionable, newValue);
            } catch (Exception e) {
                Basic.caught(e);
            }
        }
    }

    /**
     * writes back the given value
     *
     * @param newValue
     */
    public void setValue(Object newValue) {
        try {
            holdValue(newValue);
            setValue();
        } catch (Exception e) {
            Basic.caught(e);
        }
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
            return "name=" + name + " info=" + infoMethod + " getMethod=" + getMethod + " setMethod=" + setMethod + " type=" + type.getTypeName() + " value=" + getValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * get method interface
     *
     * @param <O> object type
     * @param <V> return value type
     */
    interface IGetMethod<O, V> {
        V invoke(O object);
    }

    /**
     * set method interface
     *
     * @param <O> object type
     * @param <V> value type
     */
    interface ISetMethod<O, V> {
        void invoke(O object, V value);
    }
}
