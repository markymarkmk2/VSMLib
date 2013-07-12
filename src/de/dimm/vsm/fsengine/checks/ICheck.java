/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.fsengine.checks;

import java.util.List;

/**
 *
 * @author Administrator
 */
public interface ICheck {

    public boolean init(Object o, Object optArg);
    public boolean check();
    public void abort();
    public String getName();
    public String getStatus();
    public String getDescription();
    public String getErrText();

    public String fillUserOptions(List<String> userSelect);
    public boolean handleUserChoice( int select, StringBuffer errText );
   

    void close();

    public String getProcessPercent();
    public String getProcessPercentDimension();

    public String getStatisticStr();


}
