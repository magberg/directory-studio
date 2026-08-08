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
package org.apache.directory.studio.adac.ui.dialogs;


import java.util.HashMap;
import java.util.Map;

import org.apache.directory.studio.adac.ui.AdacEntryLabels;
import org.apache.directory.studio.adac.ui.AdacEntryOps;
import org.apache.directory.studio.adac.ui.AdacServerSupport;
import org.apache.directory.studio.adac.ui.AdacServerSupport.Kind;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.utils.CompoundModification;
import org.apache.directory.studio.ldapbrowser.ui.BrowserUIPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


/**
 * ADAC-style sectioned property dialog for user / group / OU.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AdacPropertyDialog extends TitleAreaDialog
{
    private final IEntry entry;
    private final Kind kind;
    private final boolean activeDirectory;
    private final Map<String, Text> fields = new HashMap<>();
    private Text membersText;
    private Text passwordText;


    public AdacPropertyDialog( Shell parentShell, IEntry entry )
    {
        super( parentShell );
        this.entry = entry;
        this.kind = AdacServerSupport.detectKind( entry );
        this.activeDirectory = AdacServerSupport.isActiveDirectory( entry );
        setShellStyle( getShellStyle() | SWT.RESIZE | SWT.MAX );
    }


    @Override
    protected void configureShell( Shell newShell )
    {
        super.configureShell( newShell );
        newShell.setText( "Properties — " + AdacEntryLabels.getDisplayName( entry ) ); //$NON-NLS-1$
    }


    @Override
    protected Control createDialogArea( Composite parent )
    {
        AdacEntryOps.ensureAttributes( entry );

        setTitle( AdacEntryLabels.getDisplayName( entry ) );
        setMessage( entry.getDn().getName() + "  ·  " + AdacEntryLabels.getObjectType( entry ) //$NON-NLS-1$
            + ( activeDirectory ? " (Active Directory)" : " (LDAP)" ) ); //$NON-NLS-1$ //$NON-NLS-2$

        Composite area = ( Composite ) super.createDialogArea( parent );
        ScrolledComposite scrolled = new ScrolledComposite( area, SWT.V_SCROLL | SWT.H_SCROLL );
        scrolled.setExpandHorizontal( true );
        scrolled.setExpandVertical( true );
        scrolled.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

        Composite body = new Composite( scrolled, SWT.NONE );
        body.setLayout( GridLayoutFactory.fillDefaults().numColumns( 1 ).margins( 8, 8 ).create() );
        scrolled.setContent( body );

        ExpandBar bar = new ExpandBar( body, SWT.V_SCROLL );
        bar.setLayoutData( GridDataFactory.fillDefaults().grab( true, true ).hint( 520, 420 ).create() );

        if ( kind == Kind.USER )
        {
            createUserSections( bar );
        }
        else if ( kind == Kind.GROUP )
        {
            createGroupSections( bar );
        }
        else if ( kind == Kind.OU )
        {
            createOuSections( bar );
        }
        else
        {
            createGenericSection( bar );
        }

        body.setSize( body.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
        scrolled.setMinSize( body.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
        return area;
    }


    private void createUserSections( ExpandBar bar )
    {
        Composite account = sectionComposite( bar );
        if ( activeDirectory )
        {
            addField( account, "sAMAccountName", "sAMAccountName", false ); //$NON-NLS-1$ //$NON-NLS-2$
            addField( account, "userPrincipalName", "User principal name", false ); //$NON-NLS-1$ //$NON-NLS-2$
        }
        else
        {
            addField( account, "uid", "uid", false ); //$NON-NLS-1$ //$NON-NLS-2$
            addField( account, "cn", "cn", false ); //$NON-NLS-1$ //$NON-NLS-2$
        }
        addField( account, "displayName", "Display name", false ); //$NON-NLS-1$ //$NON-NLS-2$
        addField( account, "givenName", "Given name", false ); //$NON-NLS-1$ //$NON-NLS-2$
        addField( account, "sn", "Surname", false ); //$NON-NLS-1$ //$NON-NLS-2$
        addField( account, "mail", "E-mail", false ); //$NON-NLS-1$ //$NON-NLS-2$
        addField( account, "telephoneNumber", "Telephone", false ); //$NON-NLS-1$ //$NON-NLS-2$
        Label pwdLabel = new Label( account, SWT.NONE );
        pwdLabel.setText( activeDirectory ? "New password (unicodePwd)" : "New password (userPassword)" ); //$NON-NLS-1$ //$NON-NLS-2$
        passwordText = new Text( account, SWT.BORDER | SWT.PASSWORD );
        passwordText.setLayoutData( GridDataFactory.fillDefaults().grab( true, false ).create() );
        passwordText.setMessage( "Leave blank to keep current password" ); //$NON-NLS-1$
        expand( bar, "Account", account, true ); //$NON-NLS-1$

        Composite org = sectionComposite( bar );
        addField( org, "title", "Title", false ); //$NON-NLS-1$ //$NON-NLS-2$
        addField( org, "department", "Department", false ); //$NON-NLS-1$ //$NON-NLS-2$
        addField( org, "company", "Company", false ); //$NON-NLS-1$ //$NON-NLS-2$
        expand( bar, "Organization", org, false ); //$NON-NLS-1$

        Composite memberOf = sectionComposite( bar );
        Label ro = new Label( memberOf, SWT.NONE );
        ro.setText( "Member Of (read-only)" ); //$NON-NLS-1$
        List list = new List( memberOf, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL );
        list.setLayoutData( GridDataFactory.fillDefaults().grab( true, true ).hint( SWT.DEFAULT, 100 ).create() );
        for ( String value : AdacEntryOps.getStrings( entry, "memberOf" ) ) //$NON-NLS-1$
        {
            list.add( value );
        }
        expand( bar, "Member Of", memberOf, false ); //$NON-NLS-1$
    }


    private void createGroupSections( ExpandBar bar )
    {
        Composite general = sectionComposite( bar );
        addField( general, "cn", "Name (cn)", false ); //$NON-NLS-1$ //$NON-NLS-2$
        if ( activeDirectory )
        {
            addField( general, "sAMAccountName", "sAMAccountName", false ); //$NON-NLS-1$ //$NON-NLS-2$
            addField( general, "groupType", "groupType", true ); //$NON-NLS-1$ //$NON-NLS-2$
        }
        addField( general, "description", "Description", false ); //$NON-NLS-1$ //$NON-NLS-2$
        expand( bar, "General", general, true ); //$NON-NLS-1$

        Composite members = sectionComposite( bar );
        Label label = new Label( members, SWT.NONE );
        label.setText( "Members (one DN per line)" ); //$NON-NLS-1$
        membersText = new Text( members, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL );
        membersText.setLayoutData( GridDataFactory.fillDefaults().grab( true, true ).hint( SWT.DEFAULT, 140 ).create() );
        membersText.setText( AdacEntryOps.listToLines( AdacEntryOps.getStrings( entry, "member" ) ) ); //$NON-NLS-1$
        expand( bar, "Members", members, true ); //$NON-NLS-1$
    }


    private void createOuSections( ExpandBar bar )
    {
        Composite general = sectionComposite( bar );
        addField( general, "ou", "Name (ou)", false ); //$NON-NLS-1$ //$NON-NLS-2$
        addField( general, "description", "Description", false ); //$NON-NLS-1$ //$NON-NLS-2$
        addField( general, "managedBy", "Managed by", false ); //$NON-NLS-1$ //$NON-NLS-2$
        expand( bar, "General", general, true ); //$NON-NLS-1$
    }


    private void createGenericSection( ExpandBar bar )
    {
        Composite general = sectionComposite( bar );
        addField( general, "cn", "cn", false ); //$NON-NLS-1$ //$NON-NLS-2$
        addField( general, "ou", "ou", false ); //$NON-NLS-1$ //$NON-NLS-2$
        addField( general, "description", "Description", false ); //$NON-NLS-1$ //$NON-NLS-2$
        expand( bar, "Attributes", general, true ); //$NON-NLS-1$
    }


    private Composite sectionComposite( ExpandBar bar )
    {
        Composite composite = new Composite( bar, SWT.NONE );
        composite.setLayout( GridLayoutFactory.swtDefaults().numColumns( 1 ).create() );
        return composite;
    }


    private void expand( ExpandBar bar, String title, Composite content, boolean expanded )
    {
        ExpandItem item = new ExpandItem( bar, SWT.NONE );
        item.setText( title );
        item.setControl( content );
        item.setHeight( content.computeSize( SWT.DEFAULT, SWT.DEFAULT ).y );
        item.setExpanded( expanded );
    }


    private void addField( Composite parent, String attribute, String labelText, boolean readOnly )
    {
        Label label = new Label( parent, SWT.NONE );
        label.setText( labelText );
        int style = SWT.BORDER;
        if ( readOnly )
        {
            style |= SWT.READ_ONLY;
        }
        Text text = new Text( parent, style );
        text.setLayoutData( GridDataFactory.fillDefaults().grab( true, false ).create() );
        text.setText( AdacEntryOps.getString( entry, attribute ) );
        if ( !readOnly )
        {
            fields.put( attribute, text );
        }
    }


    @Override
    protected void createButtonsForButtonBar( Composite parent )
    {
        createButton( parent, IDialogConstants.OK_ID, "Save", true ); //$NON-NLS-1$
        createButton( parent, 1001, "Advanced…", false ); //$NON-NLS-1$
        createButton( parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false );
    }


    @Override
    protected void buttonPressed( int buttonId )
    {
        if ( buttonId == 1001 )
        {
            openAdvanced();
            return;
        }
        super.buttonPressed( buttonId );
    }


    private void openAdvanced()
    {
        try
        {
            BrowserUIPlugin.getDefault().getEntryEditorManager()
                .openEntryEditor( new IEntry[]
                    { entry }, new org.apache.directory.studio.ldapbrowser.core.model.ISearchResult[0],
                    new org.apache.directory.studio.ldapbrowser.core.model.IBookmark[0] );
            close();
        }
        catch ( Exception e )
        {
            setErrorMessage( "Could not open classic entry editor: " + e.getMessage() ); //$NON-NLS-1$
        }
    }


    @Override
    protected void okPressed()
    {
        try
        {
            IEntry working = new CompoundModification().cloneEntry( entry );
            for ( Map.Entry<String, Text> field : fields.entrySet() )
            {
                AdacEntryOps.setString( working, field.getKey(), field.getValue().getText() );
            }
            if ( membersText != null )
            {
                AdacEntryOps.setMultiString( working, "member", AdacEntryOps.linesToList( membersText.getText() ) ); //$NON-NLS-1$
            }
            if ( passwordText != null && passwordText.getText() != null && !passwordText.getText().isEmpty() )
            {
                AdacEntryOps.setPassword( working, passwordText.getText(), activeDirectory );
            }
            AdacEntryOps.saveDiff( entry, working );
            super.okPressed();
        }
        catch ( Exception e )
        {
            setErrorMessage( e.getMessage() );
        }
    }
}
