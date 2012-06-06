/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dimm.vsm.net.interfaces;

import de.dimm.vsm.net.GuiWrapper;

/**
 *
 * @author Administrator
 */
public interface GuiLoginApi
{
    public GuiWrapper login( String user, String pwd );
    public GuiWrapper relogin( GuiWrapper wrapper, String user, String pwd );
    public boolean logout(GuiWrapper wrapper );

    public GuiServerApi getDummyGuiServerApi();

    public boolean isStillValid( GuiWrapper wrapper );
}
