/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.net;

import de.dimm.vsm.records.Schedule;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class ScheduleStatusEntry
{

    private Schedule schedule;
    private long timestamp;
    private List<ValueEntry> values;

    public class ValueEntry
    {
        String niceName;
        String token;
        Object value;

        public ValueEntry( String niceName, String token, Object value )
        {
            this.niceName = niceName;
            this.token = token;
            this.value = value;
        }


        @Override
        public String toString()
        {
            return niceName + ": " + value.toString();
        }

        public String getNiceName()
        {
            return niceName;
        }

        public Object getValue()
        {
            return value;
        }
        
    }

    public ScheduleStatusEntry( Schedule schedule )
    {
        this.schedule = schedule;
        this.timestamp = System.currentTimeMillis();
        this.values = new ArrayList<ValueEntry>();
    }


    public void add( String name, String token, Object val )
    {
        ValueEntry entry = new ValueEntry(name, token, val);
        values.add(entry);
    }

    public List<ValueEntry> getValues()
    {
        return values;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public Schedule getSchedule()
    {
        return schedule;
    }

    
}
