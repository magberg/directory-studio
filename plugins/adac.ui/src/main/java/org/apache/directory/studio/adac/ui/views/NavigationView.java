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
package org.apache.directory.studio.adac.ui.views;


import org.apache.directory.studio.adac.ui.AdacEntryLabels;
import org.apache.directory.studio.adac.ui.AdacUIConstants;
import org.apache.directory.studio.adac.ui.AdacUIPlugin;
import org.apache.directory.studio.adac.ui.selection.AdacShellContext;
import org.apache.directory.studio.connection.core.Connection;
import org.apache.directory.studio.connection.core.ConnectionFolder;
import org.apache.directory.studio.connection.core.event.ConnectionEventRegistry;
import org.apache.directory.studio.connection.core.event.ConnectionUpdateListener;
import org.apache.directory.studio.ldapbrowser.core.BrowserCorePlugin;
import org.apache.directory.studio.ldapbrowser.core.events.ChildrenInitializedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EntryModificationEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EntryUpdateListener;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.jobs.InitializeChildrenRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.StudioBrowserJob;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IRootDSE;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;


/**
 * Left-side Navigation tree (containers / OUs under connections).
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NavigationView extends ViewPart implements ConnectionUpdateListener, EntryUpdateListener
{
    private TreeViewer viewer;


    public static String getId()
    {
        return AdacUIConstants.VIEW_NAVIGATION;
    }


    @Override
    public void createPartControl( Composite parent )
    {
        viewer = new TreeViewer( parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER );
        viewer.setContentProvider( new NavigationContentProvider() );
        viewer.setLabelProvider( new NavigationLabelProvider() );
        viewer.addFilter( new ContainerFilter() );
        viewer.setInput( "ROOT" ); //$NON-NLS-1$

        viewer.addSelectionChangedListener( event ->
        {
            Object first = ( ( StructuredSelection ) event.getSelection() ).getFirstElement();
            if ( first instanceof IEntry )
            {
                AdacShellContext.getInstance().setCurrentContainer( ( IEntry ) first );
            }
            else if ( first instanceof IBrowserConnection )
            {
                IBrowserConnection bc = ( IBrowserConnection ) first;
                Connection connection = bc.getConnection();
                if ( connection != null && connection.getConnectionWrapper() != null
                    && connection.getConnectionWrapper().isConnected() )
                {
                    IRootDSE rootDSE = bc.getRootDSE();
                    if ( rootDSE != null )
                    {
                        AdacShellContext.getInstance().setCurrentContainer( rootDSE );
                    }
                }
            }
        } );

        ConnectionEventRegistry.addConnectionUpdateListener( this, AdacUIPlugin.getDefault().getEventRunner() );
        EventRegistry.addEntryUpdateListener( this, AdacUIPlugin.getDefault().getEventRunner() );
        getSite().setSelectionProvider( viewer );
    }


    @Override
    public void setFocus()
    {
        if ( viewer != null )
        {
            viewer.getControl().setFocus();
        }
    }


    @Override
    public void dispose()
    {
        ConnectionEventRegistry.removeConnectionUpdateListener( this );
        EventRegistry.removeEntryUpdateListener( this );
        super.dispose();
    }


    private void refreshViewer()
    {
        if ( viewer != null && !viewer.getControl().isDisposed() )
        {
            viewer.refresh();
        }
    }


    @Override
    public void connectionOpened( Connection connection )
    {
        refreshViewer();
    }


    @Override
    public void connectionClosed( Connection connection )
    {
        refreshViewer();
    }


    @Override
    public void connectionAdded( Connection connection )
    {
        refreshViewer();
    }


    @Override
    public void connectionRemoved( Connection connection )
    {
        refreshViewer();
    }


    @Override
    public void connectionUpdated( Connection connection )
    {
        refreshViewer();
    }


    @Override
    public void connectionFolderModified( ConnectionFolder connectionFolder )
    {
        refreshViewer();
    }


    @Override
    public void connectionFolderAdded( ConnectionFolder connectionFolder )
    {
        refreshViewer();
    }


    @Override
    public void connectionFolderRemoved( ConnectionFolder connectionFolder )
    {
        refreshViewer();
    }


    @Override
    public void entryUpdated( EntryModificationEvent event )
    {
        if ( event instanceof ChildrenInitializedEvent && viewer != null && !viewer.getControl().isDisposed() )
        {
            viewer.refresh( event.getModifiedEntry(), true );
        }
    }


    private static final class NavigationLabelProvider extends LabelProvider
    {
        @Override
        public String getText( Object element )
        {
            if ( element instanceof IBrowserConnection )
            {
                Connection connection = ( ( IBrowserConnection ) element ).getConnection();
                return connection != null ? connection.getName() : element.toString();
            }
            if ( element instanceof IRootDSE )
            {
                return "Root DSE"; //$NON-NLS-1$
            }
            if ( element instanceof IEntry )
            {
                return AdacEntryLabels.getDisplayName( ( IEntry ) element );
            }
            if ( element instanceof String )
            {
                return ( String ) element;
            }
            return element != null ? element.toString() : ""; //$NON-NLS-1$
        }
    }


    private static final class NavigationContentProvider implements ITreeContentProvider
    {
        @Override
        public Object[] getElements( Object inputElement )
        {
            IBrowserConnection[] connections = BrowserCorePlugin.getDefault().getConnectionManager()
                .getBrowserConnections();
            return connections != null ? connections : new Object[0];
        }


        @Override
        public Object[] getChildren( Object parentElement )
        {
            if ( parentElement instanceof IBrowserConnection )
            {
                IBrowserConnection bc = ( IBrowserConnection ) parentElement;
                Connection connection = bc.getConnection();
                if ( connection == null || connection.getConnectionWrapper() == null
                    || !connection.getConnectionWrapper().isConnected() )
                {
                    return new String[]
                        { "Not connected" }; //$NON-NLS-1$
                }
                IRootDSE rootDSE = bc.getRootDSE();
                if ( rootDSE == null )
                {
                    return new Object[0];
                }
                if ( !rootDSE.isChildrenInitialized() )
                {
                    new StudioBrowserJob( new InitializeChildrenRunnable( false, rootDSE ) ).execute();
                    return new String[]
                        { "Loading…" }; //$NON-NLS-1$
                }
                IEntry[] children = rootDSE.getChildren();
                return children != null ? children : new Object[0];
            }
            if ( parentElement instanceof IEntry )
            {
                IEntry entry = ( IEntry ) parentElement;
                if ( !entry.isChildrenInitialized() )
                {
                    new StudioBrowserJob( new InitializeChildrenRunnable( false, entry ) ).execute();
                    return new String[]
                        { "Loading…" }; //$NON-NLS-1$
                }
                IEntry[] children = entry.getChildren();
                return children != null ? children : new Object[0];
            }
            return new Object[0];
        }


        @Override
        public Object getParent( Object element )
        {
            if ( element instanceof IEntry )
            {
                return ( ( IEntry ) element ).getParententry();
            }
            return null;
        }


        @Override
        public boolean hasChildren( Object element )
        {
            if ( element instanceof IBrowserConnection )
            {
                return true;
            }
            if ( element instanceof IEntry )
            {
                return ( ( IEntry ) element ).hasChildren() || !( ( IEntry ) element ).isChildrenInitialized();
            }
            return false;
        }


        @Override
        public void dispose()
        {
        }


        @Override
        public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
        {
        }
    }


    private static final class ContainerFilter extends ViewerFilter
    {
        @Override
        public boolean select( Viewer viewer, Object parentElement, Object element )
        {
            if ( element instanceof String || element instanceof IBrowserConnection || element instanceof IRootDSE )
            {
                return true;
            }
            if ( element instanceof IEntry )
            {
                IEntry entry = ( IEntry ) element;
                if ( !entry.isChildrenInitialized() )
                {
                    return true;
                }
                return AdacEntryLabels.isContainerLike( entry ) || entry.hasChildren();
            }
            return true;
        }
    }
}
