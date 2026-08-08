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


import java.util.HashSet;
import java.util.Set;

import org.apache.directory.studio.adac.ui.AdacChrome;
import org.apache.directory.studio.adac.ui.AdacEntryLabels;
import org.apache.directory.studio.adac.ui.AdacServerSupport.Kind;
import org.apache.directory.studio.adac.ui.AdacUIConstants;
import org.apache.directory.studio.adac.ui.dialogs.AdacPropertyDialog;
import org.apache.directory.studio.adac.ui.selection.AdacShellContext;
import org.apache.directory.studio.adac.ui.wizards.NewAdacObjectWizard;
import org.apache.directory.studio.ldapbrowser.core.jobs.DeleteEntriesRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.StudioBrowserJob;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IRootDSE;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.part.ViewPart;


/**
 * Right-side Tasks pane (ADAC-style sections).
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class TasksView extends ViewPart implements AdacShellContext.Listener
{
    private Link propertiesLink;
    private Link deleteLink;
    private Link newUserLink;
    private Link newGroupLink;
    private Link newOuLink;
    private Label selectionLabel;


    public static String getId()
    {
        return AdacUIConstants.VIEW_TASKS;
    }


    @Override
    public void createPartControl( Composite parent )
    {
        Composite root = new Composite( parent, SWT.NONE );
        root.setLayout( GridLayoutFactory.fillDefaults().numColumns( 1 ).margins( 10, 10 ).spacing( 6, 8 ).create() );
        root.setBackground( AdacChrome.tasksBackground() );
        root.setBackgroundMode( SWT.INHERIT_DEFAULT );
        root.setData( "org.eclipse.e4.ui.css.id", "adac-tasks" ); //$NON-NLS-1$ //$NON-NLS-2$

        selectionLabel = new Label( root, SWT.WRAP );
        selectionLabel.setLayoutData( GridDataFactory.fillDefaults().grab( true, false ).create() );
        selectionLabel.setText( "No selection" ); //$NON-NLS-1$
        selectionLabel.setBackground( AdacChrome.tasksBackground() );

        addSectionHeader( root, "Tasks" ); //$NON-NLS-1$
        propertiesLink = addTaskLink( root, "Properties", this::openProperties ); //$NON-NLS-1$
        deleteLink = addTaskLink( root, "Delete", this::deleteSelected ); //$NON-NLS-1$

        addSectionHeader( root, "Create" ); //$NON-NLS-1$
        newUserLink = addTaskLink( root, "New User…", () -> openCreateWizard( Kind.USER ) ); //$NON-NLS-1$
        newGroupLink = addTaskLink( root, "New Group…", () -> openCreateWizard( Kind.GROUP ) ); //$NON-NLS-1$
        newOuLink = addTaskLink( root, "New Organizational Unit…", () -> openCreateWizard( Kind.OU ) ); //$NON-NLS-1$

        AdacShellContext.getInstance().addListener( this );
        updateEnabledState( AdacShellContext.getInstance().getSelectedObject(),
            AdacShellContext.getInstance().getCurrentContainer() );
    }


    private void addSectionHeader( Composite parent, String title )
    {
        Label header = new Label( parent, SWT.NONE );
        header.setText( title );
        header.setLayoutData( GridDataFactory.fillDefaults().grab( true, false ).create() );
        header.setBackground( AdacChrome.tasksBackground() );
        header.setForeground( AdacChrome.sectionForeground() );
        header.setFont( JFaceResources.getFontRegistry().getBold( JFaceResources.DEFAULT_FONT ) );
        header.setData( "org.eclipse.e4.ui.css.className", "adac-section" ); //$NON-NLS-1$ //$NON-NLS-2$
    }


    private Link addTaskLink( Composite parent, String text, Runnable action )
    {
        Link link = new Link( parent, SWT.NONE );
        link.setText( "<a>" + text + "</a>" ); //$NON-NLS-1$ //$NON-NLS-2$
        link.setLayoutData( GridDataFactory.fillDefaults().grab( true, false ).create() );
        link.setBackground( AdacChrome.tasksBackground() );
        link.addSelectionListener( new SelectionAdapter()
        {
            @Override
            public void widgetSelected( SelectionEvent e )
            {
                action.run();
            }
        } );
        return link;
    }


    @Override
    public void setFocus()
    {
        if ( propertiesLink != null )
        {
            propertiesLink.setFocus();
        }
    }


    @Override
    public void dispose()
    {
        AdacShellContext.getInstance().removeListener( this );
        super.dispose();
    }


    @Override
    public void containerChanged( IEntry container )
    {
        updateEnabledState( AdacShellContext.getInstance().getSelectedObject(), container );
    }


    @Override
    public void selectionChanged( IEntry selected )
    {
        updateEnabledState( selected, AdacShellContext.getInstance().getCurrentContainer() );
    }


    private void updateEnabledState( IEntry selected, IEntry container )
    {
        if ( selectionLabel == null || selectionLabel.isDisposed() )
        {
            return;
        }
        if ( selected != null )
        {
            selectionLabel.setText( AdacEntryLabels.getDisplayName( selected ) + " (" //$NON-NLS-1$
                + AdacEntryLabels.getObjectType( selected ) + ")" ); //$NON-NLS-1$
        }
        else if ( container != null )
        {
            selectionLabel.setText( "Container: " + AdacEntryLabels.getDisplayName( container ) ); //$NON-NLS-1$
        }
        else
        {
            selectionLabel.setText( "No selection" ); //$NON-NLS-1$
        }

        boolean hasObject = selected != null && !( selected instanceof IRootDSE );
        boolean canCreate = container != null && !( container instanceof IRootDSE );

        setLinkEnabled( propertiesLink, hasObject );
        setLinkEnabled( deleteLink, hasObject );
        setLinkEnabled( newUserLink, canCreate );
        setLinkEnabled( newGroupLink, canCreate );
        setLinkEnabled( newOuLink, canCreate );
    }


    private void setLinkEnabled( Link link, boolean enabled )
    {
        if ( link != null && !link.isDisposed() )
        {
            link.setEnabled( enabled );
        }
    }


    private void openProperties()
    {
        IEntry selected = AdacShellContext.getInstance().getSelectedObject();
        if ( selected == null )
        {
            return;
        }
        new AdacPropertyDialog( getSite().getShell(), selected ).open();
    }


    private void deleteSelected()
    {
        IEntry selected = AdacShellContext.getInstance().getSelectedObject();
        if ( selected == null || selected instanceof IRootDSE )
        {
            return;
        }
        boolean ok = MessageDialog.openConfirm( getSite().getShell(), "Delete", //$NON-NLS-1$
            "Delete entry " + selected.getDn().getName() + "?" ); //$NON-NLS-1$ //$NON-NLS-2$
        if ( !ok )
        {
            return;
        }
        Set<IEntry> entries = new HashSet<>();
        entries.add( selected );
        new StudioBrowserJob( new DeleteEntriesRunnable( entries, false ) ).execute();
        AdacShellContext.getInstance().setSelectedObject( null );
    }


    private void openCreateWizard( Kind kind )
    {
        IEntry container = AdacShellContext.getInstance().getCurrentContainer();
        if ( container == null || container instanceof IRootDSE )
        {
            MessageDialog.openInformation( getSite().getShell(), "Create", //$NON-NLS-1$
                "Select a container (OU) in Navigation first." ); //$NON-NLS-1$
            return;
        }
        NewAdacObjectWizard wizard = new NewAdacObjectWizard( kind, container );
        WizardDialog dialog = new WizardDialog( getSite().getShell(), wizard );
        dialog.open();
    }
}
