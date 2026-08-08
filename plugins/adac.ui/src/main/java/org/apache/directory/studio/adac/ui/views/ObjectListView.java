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


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.studio.adac.ui.AdacChrome;
import org.apache.directory.studio.adac.ui.AdacEntryLabels;
import org.apache.directory.studio.adac.ui.AdacUIConstants;
import org.apache.directory.studio.adac.ui.AdacUIPlugin;
import org.apache.directory.studio.adac.ui.selection.AdacShellContext;
import org.apache.directory.studio.ldapbrowser.core.events.ChildrenInitializedEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EntryModificationEvent;
import org.apache.directory.studio.ldapbrowser.core.events.EntryUpdateListener;
import org.apache.directory.studio.ldapbrowser.core.events.EventRegistry;
import org.apache.directory.studio.ldapbrowser.core.jobs.InitializeAttributesRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.InitializeChildrenRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.StudioBrowserJob;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;


/**
 * Center object list with breadcrumb and quick filter.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ObjectListView extends ViewPart implements AdacShellContext.Listener, EntryUpdateListener
{
    private Composite breadcrumbBar;
    private Text filterText;
    private TableViewer tableViewer;
    private IEntry currentContainer;
    private String filter = ""; //$NON-NLS-1$


    public static String getId()
    {
        return AdacUIConstants.VIEW_OBJECT_LIST;
    }


    @Override
    public void createPartControl( Composite parent )
    {
        Composite root = new Composite( parent, SWT.NONE );
        root.setLayout( GridLayoutFactory.fillDefaults().numColumns( 1 ).margins( 4, 4 ).spacing( 4, 4 ).create() );

        breadcrumbBar = new Composite( root, SWT.NONE );
        breadcrumbBar.setLayoutData( GridDataFactory.fillDefaults().grab( true, false ).hint( SWT.DEFAULT, 28 ).create() );
        RowLayout rowLayout = new RowLayout( SWT.HORIZONTAL );
        rowLayout.spacing = 2;
        rowLayout.marginLeft = 8;
        rowLayout.marginRight = 8;
        rowLayout.marginTop = 4;
        rowLayout.marginBottom = 4;
        rowLayout.center = true;
        breadcrumbBar.setLayout( rowLayout );
        breadcrumbBar.setBackground( AdacChrome.headerBackground() );
        breadcrumbBar.setData( "org.eclipse.e4.ui.css.id", "adac-breadcrumb" ); //$NON-NLS-1$ //$NON-NLS-2$

        filterText = new Text( root, SWT.SEARCH | SWT.ICON_CANCEL | SWT.BORDER );
        filterText.setMessage( "Filter" ); //$NON-NLS-1$
        filterText.setLayoutData( GridDataFactory.fillDefaults().grab( true, false ).create() );
        filterText.addModifyListener( e ->
        {
            filter = filterText.getText() != null ? filterText.getText() : ""; //$NON-NLS-1$
            if ( tableViewer != null )
            {
                tableViewer.refresh();
            }
        } );

        Table table = new Table( root, SWT.FULL_SELECTION | SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL );
        table.setHeaderVisible( true );
        table.setLinesVisible( true );
        table.setLayoutData( GridDataFactory.fillDefaults().grab( true, true ).create() );

        tableViewer = new TableViewer( table );
        tableViewer.setContentProvider( ArrayContentProvider.getInstance() );
        tableViewer.addFilter( new ViewerFilter()
        {
            @Override
            public boolean select( Viewer viewer, Object parentElement, Object element )
            {
                if ( filter == null || filter.isBlank() )
                {
                    return true;
                }
                if ( !( element instanceof IEntry ) )
                {
                    return false;
                }
                IEntry entry = ( IEntry ) element;
                String needle = filter.toLowerCase( Locale.ROOT );
                return AdacEntryLabels.getDisplayName( entry ).toLowerCase( Locale.ROOT ).contains( needle )
                    || AdacEntryLabels.getObjectType( entry ).toLowerCase( Locale.ROOT ).contains( needle )
                    || AdacEntryLabels.getDescription( entry ).toLowerCase( Locale.ROOT ).contains( needle );
            }
        } );

        createColumn( "Name", 220, entry -> AdacEntryLabels.getDisplayName( entry ) ); //$NON-NLS-1$
        createColumn( "Type", 140, entry -> AdacEntryLabels.getObjectType( entry ) ); //$NON-NLS-1$
        createColumn( "Description", 280, entry -> AdacEntryLabels.getDescription( entry ) ); //$NON-NLS-1$

        tableViewer.addSelectionChangedListener( event ->
        {
            IStructuredSelection selection = ( IStructuredSelection ) event.getSelection();
            Object first = selection.getFirstElement();
            if ( first instanceof IEntry )
            {
                AdacShellContext.getInstance().setSelectedObject( ( IEntry ) first );
            }
            else
            {
                AdacShellContext.getInstance().setSelectedObject( null );
            }
        } );

        tableViewer.addDoubleClickListener( event ->
        {
            IStructuredSelection selection = ( IStructuredSelection ) event.getSelection();
            Object first = selection.getFirstElement();
            if ( first instanceof IEntry )
            {
                IEntry entry = ( IEntry ) first;
                if ( AdacEntryLabels.isContainerLike( entry ) || entry.hasChildren()
                    || !entry.isChildrenInitialized() )
                {
                    AdacShellContext.getInstance().setCurrentContainer( entry );
                }
                else
                {
                    new org.apache.directory.studio.adac.ui.dialogs.AdacPropertyDialog( getSite().getShell(), entry )
                        .open();
                }
            }
        } );

        AdacShellContext.getInstance().addListener( this );
        EventRegistry.addEntryUpdateListener( this, AdacUIPlugin.getDefault().getEventRunner() );
        rebuildBreadcrumb( null );
        getSite().setSelectionProvider( tableViewer );
    }


    private void createColumn( String title, int width, java.util.function.Function<IEntry, String> labelFn )
    {
        TableViewerColumn column = new TableViewerColumn( tableViewer, SWT.NONE );
        column.getColumn().setText( title );
        column.getColumn().setWidth( width );
        column.setLabelProvider( new ColumnLabelProvider()
        {
            @Override
            public String getText( Object element )
            {
                if ( element instanceof IEntry )
                {
                    return labelFn.apply( ( IEntry ) element );
                }
                return ""; //$NON-NLS-1$
            }
        } );
    }


    @Override
    public void setFocus()
    {
        if ( tableViewer != null )
        {
            tableViewer.getControl().setFocus();
        }
    }


    @Override
    public void dispose()
    {
        AdacShellContext.getInstance().removeListener( this );
        EventRegistry.removeEntryUpdateListener( this );
        super.dispose();
    }


    @Override
    public void containerChanged( IEntry container )
    {
        currentContainer = container;
        rebuildBreadcrumb( container );
        loadChildren( container );
    }


    @Override
    public void selectionChanged( IEntry selected )
    {
        // selection owned by this view / Tasks — nothing to do
    }


    @Override
    public void entryUpdated( EntryModificationEvent event )
    {
        if ( currentContainer == null || tableViewer == null || tableViewer.getControl().isDisposed() )
        {
            return;
        }
        if ( event instanceof ChildrenInitializedEvent
            && currentContainer.equals( event.getModifiedEntry() ) )
        {
            showChildren( currentContainer );
        }
        else if ( event.getModifiedEntry() != null
            && currentContainer.equals( event.getModifiedEntry().getParententry() ) )
        {
            tableViewer.refresh( event.getModifiedEntry() );
        }
    }


    private void loadChildren( IEntry container )
    {
        if ( tableViewer == null || tableViewer.getControl().isDisposed() )
        {
            return;
        }
        if ( container == null )
        {
            tableViewer.setInput( new Object[0] );
            return;
        }
        if ( !container.isChildrenInitialized() )
        {
            tableViewer.setInput( new String[]
                { "Loading…" } ); //$NON-NLS-1$
            new StudioBrowserJob( new InitializeChildrenRunnable( false, container ) ).execute();
            return;
        }
        showChildren( container );
    }


    private void showChildren( IEntry container )
    {
        IEntry[] children = container.getChildren();
        if ( children == null )
        {
            children = new IEntry[0];
        }
        tableViewer.setInput( children );
        if ( children.length > 0 )
        {
            // Load attributes for Type / Description columns
            new StudioBrowserJob( new InitializeAttributesRunnable( children ) ).execute();
        }
    }


    private void rebuildBreadcrumb( IEntry container )
    {
        if ( breadcrumbBar == null || breadcrumbBar.isDisposed() )
        {
            return;
        }
        for ( org.eclipse.swt.widgets.Control child : breadcrumbBar.getChildren() )
        {
            child.dispose();
        }

        if ( container == null )
        {
            CLabel empty = new CLabel( breadcrumbBar, SWT.NONE );
            empty.setText( "Select a container in Navigation" ); //$NON-NLS-1$
            empty.setBackground( AdacChrome.headerBackground() );
            empty.setForeground( AdacChrome.headerForeground() );
            breadcrumbBar.layout( true, true );
            return;
        }

        List<IEntry> chain = new ArrayList<>();
        IEntry walk = container;
        while ( walk != null )
        {
            chain.add( 0, walk );
            walk = walk.getParententry();
        }

        for ( int i = 0; i < chain.size(); i++ )
        {
            final IEntry segment = chain.get( i );
            if ( i > 0 )
            {
                CLabel sep = new CLabel( breadcrumbBar, SWT.NONE );
                sep.setText( " › " ); //$NON-NLS-1$
                sep.setBackground( AdacChrome.headerBackground() );
                sep.setForeground( AdacChrome.headerForeground() );
            }
            Link link = new Link( breadcrumbBar, SWT.NONE );
            String label = escapeLink( AdacEntryLabels.getDisplayName( segment ) );
            link.setText( "<a>" + label + "</a>" ); //$NON-NLS-1$ //$NON-NLS-2$
            link.setBackground( AdacChrome.headerBackground() );
            link.setForeground( AdacChrome.headerForeground() );
            link.setToolTipText( segment.getDn() != null ? segment.getDn().getName() : "" ); //$NON-NLS-1$
            link.addSelectionListener( new SelectionAdapter()
            {
                @Override
                public void widgetSelected( SelectionEvent e )
                {
                    AdacShellContext.getInstance().setCurrentContainer( segment );
                }
            } );
        }

        // Also allow jumping to DN parent via RDN segments if parententry chain is short
        Dn dn = container.getDn();
        if ( dn != null && chain.size() <= 1 && dn.size() > 1 )
        {
            // keep simple: parententry chain is authoritative
        }

        breadcrumbBar.layout( true, true );
        breadcrumbBar.getParent().layout( true, true );
    }


    private static String escapeLink( String text )
    {
        if ( text == null )
        {
            return ""; //$NON-NLS-1$
        }
        return text.replace( "&", "&&" ).replace( "<", " " ).replace( ">", " " ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
    }
}
