/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.fsengine;

import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author Administrator
 */
public interface JDBCConnectionFactory
{

    public Connection createConnection() throws SQLException;
}
