/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.parameters;


public class ParameterTest {

    enum MyEnum { V1, V2, V3 };
    
    int intVal = 42;
    boolean boolVal = true;
    String stringVal = "lalalle";
    
    MyEnum enumVal = MyEnum.V2;

    public int getIntVal() {
        return intVal;
    }

    public void setIntVal(int intVal) {
        this.intVal = intVal;
        System.err.println("set intval " + intVal);
    }

    public boolean getBoolVal() {
        return boolVal;
    }

    public void setBoolVal(boolean boolVal) {
        this.boolVal = boolVal;
        System.err.println("set boolval " + boolVal);
    }

    public String getStringVal() {
        return stringVal;
    }

    public void setStringVal(String stringVal) {
        this.stringVal = stringVal;
        System.err.println("set stringval " + stringVal);
    }

    public MyEnum getEnumVal() {
        return enumVal;
    }

    public void setEnumVal(MyEnum enumVal) {
        this.enumVal = enumVal;
        System.err.println("set enum " + enumVal);
    }
    
}
