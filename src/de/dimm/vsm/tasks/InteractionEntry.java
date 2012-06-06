/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.tasks;

import java.io.Serializable;
import java.util.Date;

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
    }
    public enum INTERACTION_ANSWER
    {
        OK, // == YES
        CANCEL, // == NO
        RETRY,
    }
   
    public enum SEVERITY
    {
        INFO,
        WARN,
        ERROR
    }

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
    

    
}
