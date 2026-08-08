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
package org.apache.directory.studio.adac.ui.perspective;


import org.apache.directory.studio.adac.ui.AdacUIConstants;
import org.apache.directory.studio.adac.ui.views.NavigationView;
import org.apache.directory.studio.adac.ui.views.ObjectListView;
import org.apache.directory.studio.adac.ui.views.TasksView;
import org.apache.directory.studio.common.ui.CommonUIUtils;
import org.apache.directory.studio.connection.ui.wizards.NewConnectionWizard;
import org.apache.directory.studio.ldapbrowser.common.wizards.NewEntryWizard;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;


/**
 * ADAC-like perspective: Navigation | Object list | Tasks.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AdacPerspective implements IPerspectiveFactory
{
    private static final String PROGRESS_VIEW_ID = "org.eclipse.ui.views.ProgressView"; //$NON-NLS-1$


    public static String getId()
    {
        return AdacUIConstants.PERSPECTIVE_ADAC;
    }


    @Override
    public void createInitialLayout( IPageLayout layout )
    {
        defineActions( layout );
        defineLayout( layout );

        layout.addPerspectiveShortcut( AdacUIConstants.PERSPECTIVE_ADAC );
        layout.addPerspectiveShortcut( AdacUIConstants.PERSPECTIVE_LDAP );
    }


    private void defineActions( IPageLayout layout )
    {
        layout.addNewWizardShortcut( NewConnectionWizard.getId() );
        layout.addNewWizardShortcut( NewEntryWizard.getId() );

        layout.addShowViewShortcut( NavigationView.getId() );
        layout.addShowViewShortcut( ObjectListView.getId() );
        layout.addShowViewShortcut( TasksView.getId() );
        layout.addShowViewShortcut( AdacUIConstants.VIEW_CONNECTION );
        layout.addShowViewShortcut( PROGRESS_VIEW_ID );
    }


    private void defineLayout( IPageLayout layout )
    {
        String editorArea = layout.getEditorArea();
        // Hide the empty classic editor strip so the shell reads as ADAC (Nav | List | Tasks).
        // Opening Advanced… still shows the editor area when an entry editor is opened.
        layout.setEditorAreaVisible( false );

        // Left ~22%: Navigation, Connections underneath
        IFolderLayout navFolder = layout.createFolder( "adacNavFolder", IPageLayout.LEFT, 0.22f, editorArea ); //$NON-NLS-1$
        navFolder.addView( NavigationView.getId() );

        IFolderLayout connectionFolder = layout.createFolder( "adacConnectionFolder", IPageLayout.BOTTOM, 0.72f, //$NON-NLS-1$
            "adacNavFolder" ); //$NON-NLS-1$
        connectionFolder.addView( AdacUIConstants.VIEW_CONNECTION );

        // Right ~22%: Tasks (+ Progress)
        IFolderLayout tasksFolder = layout.createFolder( "adacTasksFolder", IPageLayout.RIGHT, 0.78f, editorArea ); //$NON-NLS-1$
        tasksFolder.addView( TasksView.getId() );

        IFolderLayout progressFolder = layout.createFolder( "adacProgressFolder", IPageLayout.BOTTOM, 0.85f, //$NON-NLS-1$
            "adacTasksFolder" ); //$NON-NLS-1$
        progressFolder.addView( PROGRESS_VIEW_ID );

        // Center: object list fills the remaining work area
        IFolderLayout listFolder = layout.createFolder( "adacListFolder", IPageLayout.TOP, 0.99f, editorArea ); //$NON-NLS-1$
        listFolder.addView( ObjectListView.getId() );

        boolean isIDE = CommonUIUtils.isIDEEnvironment();
        if ( !isIDE )
        {
            layout.getViewLayout( NavigationView.getId() ).setCloseable( false );
            layout.getViewLayout( ObjectListView.getId() ).setCloseable( false );
            layout.getViewLayout( TasksView.getId() ).setCloseable( false );
            layout.getViewLayout( AdacUIConstants.VIEW_CONNECTION ).setCloseable( false );
        }
    }
}
