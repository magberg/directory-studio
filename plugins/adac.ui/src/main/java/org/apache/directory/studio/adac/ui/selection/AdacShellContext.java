/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.apache.directory.studio.adac.ui.selection;


import java.util.ArrayList;
import java.util.List;

import org.apache.directory.studio.ldapbrowser.core.model.IEntry;


/**
 * Shared ADAC shell context: current container for the object list and
 * currently selected object for the Tasks pane.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class AdacShellContext
{
    private static final AdacShellContext INSTANCE = new AdacShellContext();

    private final List<Listener> listeners = new ArrayList<>();

    private IEntry currentContainer;
    private IEntry selectedObject;


    public interface Listener
    {
        void containerChanged( IEntry container );


        void selectionChanged( IEntry selected );
    }


    private AdacShellContext()
    {
    }


    public static AdacShellContext getInstance()
    {
        return INSTANCE;
    }


    public synchronized void addListener( Listener listener )
    {
        if ( listener != null )
        {
            listeners.add( listener );
        }
    }


    public synchronized void removeListener( Listener listener )
    {
        listeners.remove( listener );
    }


    public IEntry getCurrentContainer()
    {
        return currentContainer;
    }


    public IEntry getSelectedObject()
    {
        return selectedObject;
    }


    public void setCurrentContainer( IEntry container )
    {
        this.currentContainer = container;
        this.selectedObject = null;
        List<Listener> snapshot;
        synchronized ( this )
        {
            snapshot = new ArrayList<>( listeners );
        }
        for ( Listener listener : snapshot )
        {
            listener.containerChanged( container );
            listener.selectionChanged( null );
        }
    }


    public void setSelectedObject( IEntry selected )
    {
        this.selectedObject = selected;
        List<Listener> snapshot;
        synchronized ( this )
        {
            snapshot = new ArrayList<>( listeners );
        }
        for ( Listener listener : snapshot )
        {
            listener.selectionChanged( selected );
        }
    }
}
