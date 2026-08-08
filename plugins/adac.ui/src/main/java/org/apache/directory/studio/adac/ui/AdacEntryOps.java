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
package org.apache.directory.studio.adac.ui;


import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.directory.api.ldap.model.constants.LdapSecurityConstants;
import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.name.Rdn;
import org.apache.directory.api.ldap.model.password.PasswordUtil;
import org.apache.directory.studio.connection.ui.RunnableContextRunner;
import org.apache.directory.studio.ldapbrowser.core.jobs.CreateEntryRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.InitializeAttributesRunnable;
import org.apache.directory.studio.ldapbrowser.core.jobs.StudioBrowserJob;
import org.apache.directory.studio.ldapbrowser.core.jobs.UpdateEntryRunnable;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;
import org.apache.directory.studio.ldapbrowser.core.model.impl.Attribute;
import org.apache.directory.studio.ldapbrowser.core.model.impl.DummyEntry;
import org.apache.directory.studio.ldapbrowser.core.model.impl.Value;
import org.apache.directory.studio.ldapbrowser.core.utils.CompoundModification;
import org.apache.directory.studio.ldapbrowser.core.utils.Utils;
import org.apache.directory.studio.ldifparser.LdifFormatParameters;
import org.apache.directory.studio.ldifparser.model.LdifFile;
import org.eclipse.core.runtime.IStatus;


/**
 * Attribute read/write and create/update helpers for ADAC.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class AdacEntryOps
{
    private AdacEntryOps()
    {
    }


    public static void ensureAttributes( IEntry entry )
    {
        if ( entry != null && !entry.isAttributesInitialized() )
        {
            RunnableContextRunner.execute( new InitializeAttributesRunnable( entry ), null, true );
        }
    }


    public static String getString( IEntry entry, String attributeDescription )
    {
        if ( entry == null || attributeDescription == null )
        {
            return ""; //$NON-NLS-1$
        }
        IAttribute attribute = entry.getAttribute( attributeDescription );
        if ( attribute == null || attribute.getValueSize() == 0 )
        {
            return ""; //$NON-NLS-1$
        }
        IValue value = attribute.getValues()[0];
        return value != null ? value.getStringValue() : ""; //$NON-NLS-1$
    }


    public static String[] getStrings( IEntry entry, String attributeDescription )
    {
        if ( entry == null || attributeDescription == null )
        {
            return new String[0];
        }
        IAttribute attribute = entry.getAttribute( attributeDescription );
        if ( attribute == null )
        {
            return new String[0];
        }
        return attribute.getStringValues();
    }


    public static void setString( IEntry entry, String attributeDescription, String newValue )
    {
        CompoundModification mod = new CompoundModification();
        IAttribute existing = entry.getAttribute( attributeDescription );
        String normalized = newValue == null ? "" : newValue.trim(); //$NON-NLS-1$
        if ( normalized.isEmpty() )
        {
            if ( existing != null && existing.getValueSize() > 0 )
            {
                mod.deleteValues( Arrays.asList( existing.getValues() ) );
            }
            return;
        }
        if ( existing == null || existing.getValueSize() == 0 )
        {
            mod.createValue( entry, attributeDescription, normalized );
        }
        else
        {
            mod.modifyValue( existing.getValues()[0], normalized );
        }
    }


    public static void setMultiString( IEntry entry, String attributeDescription, List<String> values )
    {
        CompoundModification mod = new CompoundModification();
        IAttribute existing = entry.getAttribute( attributeDescription );
        if ( existing != null && existing.getValueSize() > 0 )
        {
            mod.deleteValues( Arrays.asList( existing.getValues() ) );
        }
        if ( values == null )
        {
            return;
        }
        for ( String value : values )
        {
            if ( value != null && !value.trim().isEmpty() )
            {
                mod.createValue( entry, attributeDescription, value.trim() );
            }
        }
    }


    public static boolean saveDiff( IEntry original, IEntry working )
    {
        LdifFile diff = Utils.computeDiff( original, working );
        if ( diff == null )
        {
            return false;
        }
        UpdateEntryRunnable runnable = new UpdateEntryRunnable( original,
            diff.toFormattedString( LdifFormatParameters.DEFAULT ) );
        new StudioBrowserJob( runnable ).execute();
        return true;
    }


    public static DummyEntry newDummy( IBrowserConnection connection )
    {
        return new DummyEntry( Dn.EMPTY_DN, connection );
    }


    public static void addObjectClasses( IEntry entry, String... objectClasses )
    {
        IAttribute oc = entry.getAttribute( SchemaConstants.OBJECT_CLASS_AT );
        if ( oc == null )
        {
            oc = new Attribute( entry, SchemaConstants.OBJECT_CLASS_AT );
            entry.addAttribute( oc );
        }
        for ( String objectClass : objectClasses )
        {
            oc.addValue( new Value( oc, objectClass ) );
        }
    }


    public static void setDnWithRdn( DummyEntry entry, Dn parentDn, String rdnType, String rdnValue ) throws Exception
    {
        Rdn rdn = new Rdn( rdnType, rdnValue );
        Dn dn = parentDn.add( rdn );
        entry.setDn( dn );
        // Ensure RDN attribute exists
        setString( entry, rdnType, rdnValue );
    }


    public static IStatus createEntry( IEntry prototype, IBrowserConnection connection )
    {
        CreateEntryRunnable runnable = new CreateEntryRunnable( prototype, connection );
        return RunnableContextRunner.execute( runnable, null, true );
    }


    /**
     * Encode password for AD unicodePwd (quoted UTF-16LE) or generic userPassword (SSHA).
     */
    public static void setPassword( IEntry entry, String plainPassword, boolean activeDirectory )
    {
        if ( plainPassword == null || plainPassword.isEmpty() )
        {
            return;
        }
        if ( activeDirectory )
        {
            byte[] unicodePwd = ( "\"" + plainPassword + "\"" ).getBytes( StandardCharsets.UTF_16LE ); //$NON-NLS-1$ //$NON-NLS-2$
            CompoundModification mod = new CompoundModification();
            IAttribute existing = entry.getAttribute( "unicodePwd" ); //$NON-NLS-1$
            if ( existing != null && existing.getValueSize() > 0 )
            {
                mod.deleteValues( Arrays.asList( existing.getValues() ) );
            }
            mod.createValue( entry, "unicodePwd", unicodePwd ); //$NON-NLS-1$
        }
        else
        {
            byte[] hashed = PasswordUtil.createStoragePassword( plainPassword,
                LdapSecurityConstants.HASH_METHOD_SSHA );
            CompoundModification mod = new CompoundModification();
            IAttribute existing = entry.getAttribute( SchemaConstants.USER_PASSWORD_AT );
            if ( existing != null && existing.getValueSize() > 0 )
            {
                mod.deleteValues( Arrays.asList( existing.getValues() ) );
            }
            mod.createValue( entry, SchemaConstants.USER_PASSWORD_AT, hashed );
        }
    }


    public static List<String> linesToList( String text )
    {
        List<String> list = new ArrayList<>();
        if ( text == null || text.isBlank() )
        {
            return list;
        }
        for ( String line : text.split( "\\R" ) ) //$NON-NLS-1$
        {
            String trimmed = line.trim();
            if ( !trimmed.isEmpty() )
            {
                list.add( trimmed );
            }
        }
        return list;
    }


    public static String listToLines( String[] values )
    {
        if ( values == null || values.length == 0 )
        {
            return ""; //$NON-NLS-1$
        }
        return String.join( "\n", values ); //$NON-NLS-1$
    }
}
