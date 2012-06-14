/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dimm.vsm.net;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class SearchEntry  implements Serializable
{
    public static final String ARG_NAME = "name";
    public static final String ARG_JOBNAME = "jobname";
    public static final String ARG_JOBCREATION = "jobcreation";
    //public static final String ARG_PATH = "path";
    public static final String ARG_TYP = "typ";
    public static final String ARG_SIZE = "size";
    public static final String ARG_CDATE = "cdate";
    public static final String ARG_MDATE = "mdate";
    public static final String ARG_ADATE = "adate";
    public static final String ARG_UID = "uid";
    public static final String ARG_GID = "gid";
    public static final String ARG_TS = "ts";
    public static final String ARG_IDX = "idx";
    public static final String ARG_JOBIDX = "jobidx";

    public static final String OP_EQUAL = "eq";
    public static final String OP_BEGINS = "begins";
    public static final String OP_ENDS = "ends";
    public static final String OP_CONTAINS = "contains";

    public static final String OP_GT = "gt";
    public static final String OP_GE = "ge";
    public static final String OP_LT = "lt";
    public static final String OP_LE = "le";
    public static final String OP_BETWEEN = "between";  // ARGS ARE SEPERATED BY ; IN FIELD argValue
    public static final String BETWEEN_SEPERATOR = ";";


    String arg;
    String arg2;
    String argName;
    String argOp;
    boolean previous_or;
    boolean previous_neg;
    boolean caseInsensitive;
    List<SearchEntry> children;

    public SearchEntry( String arg, String arg2, String argName, String argOp, boolean previous_or, boolean previous_neg, boolean caseInsensitive, List<SearchEntry> children )
    {
        this.arg = arg;
        this.arg2 = arg2;
        this.argName = argName;
        this.argOp = argOp;
        this.previous_or = previous_or;
        this.previous_neg = previous_neg;
        this.children = new ArrayList<SearchEntry>();
        this.caseInsensitive = caseInsensitive;
    }
    public SearchEntry( String arg, String argName, String argOp, boolean previous_or, boolean previous_neg, boolean caseInsensitive, List<SearchEntry> children )
    {
        this.arg = arg;
        this.arg2 = null;
        this.argName = argName;
        this.argOp = argOp;
        this.previous_or = previous_or;
        this.previous_neg = previous_neg;
        this.children = new ArrayList<SearchEntry>();
        this.caseInsensitive = caseInsensitive;
    }

 
    void add( SearchEntry e )
    {
        children.add(e);
    }

    public String getArgOp()
    {
        return argOp;
    }

    public String getArgType()
    {
        return argName;
    }

    public String getArgValue()
    {
        return arg;
    }


    public List<SearchEntry> getChildren()
    {
        return children;
    }


    public boolean isPrevious_neg()
    {
        return previous_neg;
    }

    public boolean isPrevious_or()
    {
        return previous_or;
    }


        // THIS IS NOT DONE OFTEN, HO HASHING NEEDED
    public String getFnameforArgtype( )
    {
        if (getArgType().equals(ARG_MDATE))
        {
            return "_A_.modificationDateMs";
        }
        if (getArgType().equals(ARG_JOBNAME))
        {
            return "_J_.name";
        }
        if (getArgType().equals(ARG_JOBCREATION))
        {
            return "_J_.startTime";
        }
        if (getArgType().equals(ARG_CDATE))
        {
            return "_A_.creationDateMs";
        }
        if (getArgType().equals(ARG_ADATE))
        {
             return "_A_.accessDateMs";
        }
        if (getArgType().equals(ARG_NAME))
        {
            return "_A_.name";
        }
        if (getArgType().equals(ARG_UID))
        {
            return "_A_.uid";
        }
        if (getArgType().equals(ARG_GID))
        {
            return "_A_.gid";
        }
        if (getArgType().equals(ARG_SIZE))
        {
            return "_A_.fsize";
        }
        if (getArgType().equals(ARG_TYP))
        {
            return "_F_.typ";
        }
        if (getArgType().equals(ARG_IDX))
        {
            return "_F_.idx";
        }
        if (getArgType().equals(ARG_JOBIDX))
        {
            return "_J_.idx";
        }
        if (getArgType().equals(ARG_TS))
        {
            return "_A_.ts";
        }

        return null;
    }
        // THIS IS NOT DONE OFTEN, HO HASHING NEEDED
    public String getLuceneFieldforArgtype( )
    {
        if (getArgType().equals(ARG_MDATE))
        {
            return "modificationDateMs";
        }
        if (getArgType().equals(ARG_JOBNAME))
        {
            return "jobname";
        }
        if (getArgType().equals(ARG_JOBCREATION))
        {
            return "startTime";
        }
        if (getArgType().equals(ARG_CDATE))
        {
            return "creationDateMs";
        }
        if (getArgType().equals(ARG_ADATE))
        {
             return "accessDateMs";
        }
        if (getArgType().equals(ARG_NAME))
        {
            return "name";
        }
        if (getArgType().equals(ARG_UID))
        {
            return "uid";
        }
        if (getArgType().equals(ARG_GID))
        {
            return "gid";
        }
        if (getArgType().equals(ARG_SIZE))
        {
            return "fsize";
        }
        if (getArgType().equals(ARG_TYP))
        {
            return "typ";
        }
        if (getArgType().equals(ARG_IDX))
        {
            return "idx";
        }
        if (getArgType().equals(ARG_JOBIDX))
        {
            return "idx";
        }
        if (getArgType().equals(ARG_TS))
        {
            return "ts";
        }

        return null;
    }
    // THIS IS NOT DONE OFTEN, HO HASHING NEEDED
    public String getOpString( )
    {
        if (getArgOp().equals(OP_EQUAL))
        {
            return "=";
        }
        if (getArgOp().equals(OP_GT))
        {
            return ">";
        }
        if (getArgOp().equals(OP_GE))
        {
            return ">=";
        }
        if (getArgOp().equals(OP_LT))
        {
            return "<";
        }
        if (getArgOp().equals(OP_LE))
        {
            return "<=";
        }
        if (getArgOp().equals(OP_BETWEEN))
        {
            return " between ";
        }
        if (getArgOp().equals(OP_BEGINS))
        {
            return " like ";
        }
        if (getArgOp().equals(OP_ENDS))
        {
            return " like ";
        }
        if (getArgOp().equals(OP_CONTAINS))
        {
            return " like ";
        }

        return null;
    }

    public boolean isStringArgType()
    {
        return getArgType().equals(ARG_NAME) || getArgType().equals(ARG_TYP)  || getArgType().equals(ARG_JOBNAME) ;
    }

    public boolean isCaseInsensitive()
    {
        return caseInsensitive;
    }

    public void setCaseInsensitive( boolean caseInsensitive )
    {
        this.caseInsensitive = caseInsensitive;
    }

    public String getLuceneVal(String arg, String argOp)
    {
        if (isStringArgType())
        {
            if (argOp.equals(OP_CONTAINS))
            {
                return "*" + arg.toLowerCase() + "*";
            }
            if (argOp.equals(OP_BEGINS))
            {
                return arg.toLowerCase() + "*";
            }
            if (argOp.equals(OP_ENDS))
            {
                return "*" + arg.toLowerCase();
            }
            if (argOp.equals(OP_EQUAL))
            {
                return arg.toLowerCase();
            }
            if (argOp.equals(OP_BETWEEN))
            {
                return "{" + arg.toLowerCase() + " TO " + arg2.toLowerCase() + "}";
            }
        }
        if (isHexLongArgType())
        {
            Long l = Long.parseLong(arg);
            String hv = Long.toString(l, 16);

            if (argOp.equals(OP_EQUAL))
            {
                return hv;
            }
            if (argOp.equals(OP_LE) || argOp.equals(OP_LT) )
            {
                return "[0 TO " + hv + "]";
            }
            if (argOp.equals(OP_GE) || argOp.equals(OP_GT) )
            {
                return "[" + hv + " TO " +  Long.toString(Long.MAX_VALUE, 16) + "]";
            }
            if (argOp.equals(OP_BETWEEN))
            {
                Long l2 = Long.parseLong(arg2);
                String hv2 = Long.toString(l2, 16);
                return "[" + hv + " TO " + hv2+ "]";
            }
        }
        return arg;
    }

    private boolean isHexLongArgType()
    {
        return getArgType().equals(ARG_ADATE) || getArgType().equals(ARG_CDATE)  || getArgType().equals(ARG_MDATE)
                || getArgType().equals(ARG_TS)  || getArgType().equals(ARG_SIZE) || getArgType().equals(ARG_JOBCREATION);
    }
    

}
