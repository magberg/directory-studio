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
package org.apache.directory.studio.adac.ui.wizards;


import org.apache.directory.studio.adac.ui.AdacEntryOps;
import org.apache.directory.studio.adac.ui.AdacServerSupport;
import org.apache.directory.studio.adac.ui.AdacServerSupport.Kind;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.impl.DummyEntry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


/**
 * Typed New User / Group / OU wizard for the ADAC shell.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NewAdacObjectWizard extends Wizard
{
    private final Kind kind;
    private final IEntry parent;
    private final boolean activeDirectory;
    private NewAdacObjectPage page;


    public NewAdacObjectWizard( Kind kind, IEntry parent )
    {
        this.kind = kind;
        this.parent = parent;
        this.activeDirectory = AdacServerSupport.isActiveDirectory( parent );
        setWindowTitle( windowTitleFor( kind ) );
        setNeedsProgressMonitor( true );
    }


    private static String windowTitleFor( Kind kind )
    {
        switch ( kind )
        {
            case USER:
                return "New User"; //$NON-NLS-1$
            case GROUP:
                return "New Group"; //$NON-NLS-1$
            case OU:
                return "New Organizational Unit"; //$NON-NLS-1$
            default:
                return "New Object"; //$NON-NLS-1$
        }
    }


    @Override
    public void addPages()
    {
        page = new NewAdacObjectPage( kind, parent, activeDirectory );
        addPage( page );
    }


    @Override
    public boolean performFinish()
    {
        try
        {
            IBrowserConnection connection = parent.getBrowserConnection();
            DummyEntry entry = AdacEntryOps.newDummy( connection );

            if ( kind == Kind.USER )
            {
                if ( activeDirectory )
                {
                    AdacEntryOps.addObjectClasses( entry, "top", "person", "organizationalPerson", "user" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    String cn = page.getCn();
                    AdacEntryOps.setDnWithRdn( entry, parent.getDn(), "cn", cn ); //$NON-NLS-1$
                    AdacEntryOps.setString( entry, "sAMAccountName", page.getSamAccountName() ); //$NON-NLS-1$
                    if ( !page.getUpn().isEmpty() )
                    {
                        AdacEntryOps.setString( entry, "userPrincipalName", page.getUpn() ); //$NON-NLS-1$
                    }
                }
                else
                {
                    AdacEntryOps.addObjectClasses( entry, "top", "person", "organizationalPerson", "inetOrgPerson" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    String cn = page.getCn();
                    AdacEntryOps.setDnWithRdn( entry, parent.getDn(), "cn", cn ); //$NON-NLS-1$
                    if ( !page.getUid().isEmpty() )
                    {
                        AdacEntryOps.setString( entry, "uid", page.getUid() ); //$NON-NLS-1$
                    }
                }
                if ( !page.getGivenName().isEmpty() )
                {
                    AdacEntryOps.setString( entry, "givenName", page.getGivenName() ); //$NON-NLS-1$
                }
                if ( !page.getSn().isEmpty() )
                {
                    AdacEntryOps.setString( entry, "sn", page.getSn() ); //$NON-NLS-1$
                }
                else if ( !page.getCn().isEmpty() )
                {
                    // sn is often MUST for person/user
                    AdacEntryOps.setString( entry, "sn", page.getCn() ); //$NON-NLS-1$
                }
                if ( !page.getMail().isEmpty() )
                {
                    AdacEntryOps.setString( entry, "mail", page.getMail() ); //$NON-NLS-1$
                }
                if ( !page.getPassword().isEmpty() )
                {
                    AdacEntryOps.setPassword( entry, page.getPassword(), activeDirectory );
                }
            }
            else if ( kind == Kind.GROUP )
            {
                if ( activeDirectory )
                {
                    AdacEntryOps.addObjectClasses( entry, "top", "group" ); //$NON-NLS-1$ //$NON-NLS-2$
                    String cn = page.getCn();
                    AdacEntryOps.setDnWithRdn( entry, parent.getDn(), "cn", cn ); //$NON-NLS-1$
                    AdacEntryOps.setString( entry, "sAMAccountName", page.getSamAccountName() ); //$NON-NLS-1$
                    // Security Global group ≈ -2147483646
                    AdacEntryOps.setString( entry, "groupType", String.valueOf( page.getAdGroupType() ) ); //$NON-NLS-1$
                }
                else
                {
                    AdacEntryOps.addObjectClasses( entry, "top", "groupOfNames" ); //$NON-NLS-1$ //$NON-NLS-2$
                    String cn = page.getCn();
                    AdacEntryOps.setDnWithRdn( entry, parent.getDn(), "cn", cn ); //$NON-NLS-1$
                    // groupOfNames requires at least one member — use parent DN as placeholder
                    AdacEntryOps.setString( entry, "member", parent.getDn().getName() ); //$NON-NLS-1$
                }
                if ( !page.getDescriptionText().isEmpty() )
                {
                    AdacEntryOps.setString( entry, "description", page.getDescriptionText() ); //$NON-NLS-1$
                }
            }
            else if ( kind == Kind.OU )
            {
                AdacEntryOps.addObjectClasses( entry, "top", "organizationalUnit" ); //$NON-NLS-1$ //$NON-NLS-2$
                AdacEntryOps.setDnWithRdn( entry, parent.getDn(), "ou", page.getOuName() ); //$NON-NLS-1$
                if ( !page.getDescriptionText().isEmpty() )
                {
                    AdacEntryOps.setString( entry, "description", page.getDescriptionText() ); //$NON-NLS-1$
                }
            }

            IStatus status = AdacEntryOps.createEntry( entry, connection );
            if ( status != null && !status.isOK() )
            {
                page.setErrorMessage( status.getMessage() );
                return false;
            }
            return true;
        }
        catch ( Exception e )
        {
            page.setErrorMessage( e.getMessage() );
            return false;
        }
    }


    private static final class NewAdacObjectPage extends WizardPage
    {
        private final Kind kind;
        private final IEntry parent;
        private final boolean activeDirectory;

        private Text cnText;
        private Text ouText;
        private Text samText;
        private Text upnText;
        private Text uidText;
        private Text givenText;
        private Text snText;
        private Text mailText;
        private Text passwordText;
        private Text descriptionText;
        private Combo groupTypeCombo;


        NewAdacObjectPage( Kind kind, IEntry parent, boolean activeDirectory )
        {
            super( "NewAdacObjectPage" ); //$NON-NLS-1$
            this.kind = kind;
            this.parent = parent;
            this.activeDirectory = activeDirectory;
            setTitle( windowTitleFor( kind ) );
            setDescription( "Create under " + parent.getDn().getName() //$NON-NLS-1$
                + ( activeDirectory ? " (Active Directory mapping)" : " (generic LDAP mapping)" ) ); //$NON-NLS-1$ //$NON-NLS-2$
        }


        @Override
        public void createControl( Composite parentComposite )
        {
            Composite root = new Composite( parentComposite, SWT.NONE );
            root.setLayout( new GridLayout( 2, false ) );

            if ( kind == Kind.OU )
            {
                ouText = labeledText( root, "Name (ou)" ); //$NON-NLS-1$
                descriptionText = labeledText( root, "Description" ); //$NON-NLS-1$
            }
            else
            {
                cnText = labeledText( root, kind == Kind.GROUP ? "Group name (cn)" : "Full name (cn)" ); //$NON-NLS-1$ //$NON-NLS-2$
                if ( kind == Kind.USER )
                {
                    if ( activeDirectory )
                    {
                        samText = labeledText( root, "sAMAccountName" ); //$NON-NLS-1$
                        upnText = labeledText( root, "User principal name" ); //$NON-NLS-1$
                    }
                    else
                    {
                        uidText = labeledText( root, "uid" ); //$NON-NLS-1$
                    }
                    givenText = labeledText( root, "Given name" ); //$NON-NLS-1$
                    snText = labeledText( root, "Surname" ); //$NON-NLS-1$
                    mailText = labeledText( root, "E-mail" ); //$NON-NLS-1$
                    passwordText = labeledText( root, "Password", true ); //$NON-NLS-1$
                }
                else if ( kind == Kind.GROUP )
                {
                    if ( activeDirectory )
                    {
                        samText = labeledText( root, "sAMAccountName" ); //$NON-NLS-1$
                        Label typeLabel = new Label( root, SWT.NONE );
                        typeLabel.setText( "Group type" ); //$NON-NLS-1$
                        groupTypeCombo = new Combo( root, SWT.READ_ONLY );
                        groupTypeCombo.setItems( "Security / Global", "Security / Domain local", "Security / Universal", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            "Distribution / Global", "Distribution / Domain local", "Distribution / Universal" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        groupTypeCombo.select( 0 );
                        groupTypeCombo.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
                    }
                    descriptionText = labeledText( root, "Description" ); //$NON-NLS-1$
                }
            }

            setControl( root );
            setPageComplete( false );
            attachValidation();
        }


        private Text labeledText( Composite parent, String label )
        {
            return labeledText( parent, label, false );
        }


        private Text labeledText( Composite parent, String label, boolean password )
        {
            Label l = new Label( parent, SWT.NONE );
            l.setText( label );
            Text text = new Text( parent, password ? SWT.BORDER | SWT.PASSWORD : SWT.BORDER );
            text.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
            return text;
        }


        private void attachValidation()
        {
            org.eclipse.swt.widgets.Listener listener = e -> validate();
            if ( cnText != null )
            {
                cnText.addListener( SWT.Modify, listener );
            }
            if ( ouText != null )
            {
                ouText.addListener( SWT.Modify, listener );
            }
            if ( samText != null )
            {
                samText.addListener( SWT.Modify, listener );
            }
        }


        private void validate()
        {
            setErrorMessage( null );
            if ( kind == Kind.OU )
            {
                setPageComplete( ouText != null && !ouText.getText().trim().isEmpty() );
                return;
            }
            boolean ok = cnText != null && !cnText.getText().trim().isEmpty();
            if ( activeDirectory && ( kind == Kind.USER || kind == Kind.GROUP ) )
            {
                ok = ok && samText != null && !samText.getText().trim().isEmpty();
            }
            setPageComplete( ok );
        }


        String getCn()
        {
            return cnText != null ? cnText.getText().trim() : ""; //$NON-NLS-1$
        }


        String getOuName()
        {
            return ouText != null ? ouText.getText().trim() : ""; //$NON-NLS-1$
        }


        String getSamAccountName()
        {
            return samText != null ? samText.getText().trim() : ""; //$NON-NLS-1$
        }


        String getUpn()
        {
            return upnText != null ? upnText.getText().trim() : ""; //$NON-NLS-1$
        }


        String getUid()
        {
            return uidText != null ? uidText.getText().trim() : ""; //$NON-NLS-1$
        }


        String getGivenName()
        {
            return givenText != null ? givenText.getText().trim() : ""; //$NON-NLS-1$
        }


        String getSn()
        {
            return snText != null ? snText.getText().trim() : ""; //$NON-NLS-1$
        }


        String getMail()
        {
            return mailText != null ? mailText.getText().trim() : ""; //$NON-NLS-1$
        }


        String getPassword()
        {
            return passwordText != null ? passwordText.getText() : ""; //$NON-NLS-1$
        }


        String getDescriptionText()
        {
            return descriptionText != null ? descriptionText.getText().trim() : ""; //$NON-NLS-1$
        }


        int getAdGroupType()
        {
            // ADS_GROUP_TYPE_* flags (security bit 0x80000000)
            int index = groupTypeCombo != null ? groupTypeCombo.getSelectionIndex() : 0;
            switch ( index )
            {
                case 1:
                    return 0x80000004; // security domain local
                case 2:
                    return 0x80000008; // security universal
                case 3:
                    return 0x00000002; // distribution global
                case 4:
                    return 0x00000004; // distribution domain local
                case 5:
                    return 0x00000008; // distribution universal
                case 0:
                default:
                    return 0x80000002; // security global
            }
        }
    }
}
