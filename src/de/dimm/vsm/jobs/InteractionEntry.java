/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.jobs;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class InteractionEntry implements Serializable
{


    public enum INTERACTION_TYPE
    {
        OK,  // == YES
        OK_CANCEL,  // == YES_NO
        OK_RETRY_CANCEL,
        SELECT,
    }
    public enum INTERACTION_ANSWER
    {
        OK, // == YES
        CANCEL, // == NO
        RETRY,
        SELECT,
    }
   
    public enum SEVERITY
    {
        INFO,
        WARN,
        ERROR
    }
    List<String> selectList;
    int defaultSelect;
    int userSelect;

    INTERACTION_TYPE interactionType;
    String text;
    String shortText;
    Date created;
    int timeout_s;
    SEVERITY severity;
    INTERACTION_ANSWER defaultAnswer;
    INTERACTION_ANSWER userAnswer;
    boolean wasAnswered;

    public InteractionEntry( INTERACTION_TYPE interactionType, SEVERITY severity, String text, String shortText, Date created, int timeout_s, INTERACTION_ANSWER defaultAnswer)
    {
        this.interactionType = interactionType;
        this.text = text;
        this.shortText = shortText;
        this.created = created;
        this.timeout_s = timeout_s;
        this.severity = severity;
        this.defaultAnswer = defaultAnswer;
    }
    public InteractionEntry( String text, String shortText, Date created, int timeout_s, List<String> selectList, int defaultSelect)
    {
        this.interactionType = INTERACTION_TYPE.SELECT;
        this.text = text;
        this.shortText = shortText;
        this.created = created;
        this.timeout_s = timeout_s;
        this.severity = SEVERITY.INFO;
        this.defaultAnswer = INTERACTION_ANSWER.SELECT;
        this.selectList = selectList;
        this.defaultSelect = defaultSelect;
    }



    public String getText()
    {
        return text;
    }

    public String getShortText()
    {
        return shortText;
    }

    public INTERACTION_ANSWER getDefaultAnswer()
    {
        return defaultAnswer;
    }

    public int getTimeout_s()
    {
        return timeout_s;
    }

    public void setAnswer( INTERACTION_ANSWER a )
    {
        userAnswer = a;
        wasAnswered = true;
    }

    public Date getCreated()
    {
        return created;
    }

    public INTERACTION_TYPE getInteractionType()
    {
        return interactionType;
    }

    public SEVERITY getSeverity()
    {
        return severity;
    }

    public boolean wasAnswered()
    {
        return wasAnswered;
    }

    public INTERACTION_ANSWER getUserAnswer()
    {
        return userAnswer;
    }
    
    public void setSelect( int s )
    {
        userAnswer = INTERACTION_ANSWER.SELECT;
        userSelect = s;
        wasAnswered = true;
    }
    public int getUserSelect()
    {
        return userSelect;
    }

    
}
