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


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.directory.api.ldap.model.schema.ObjectClass;
import org.apache.directory.studio.ldapbrowser.core.model.IAttribute;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IValue;


/**
 * Helpers for ADAC display labels (Name / Type / Description).
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class AdacEntryLabels
{
    private static final String[] NAME_ATTRS = new String[]
        {
            "cn", //$NON-NLS-1$
            "name", //$NON-NLS-1$
            "ou", //$NON-NLS-1$
            "dc", //$NON-NLS-1$
            "uid", //$NON-NLS-1$
            "sAMAccountName" //$NON-NLS-1$
        };

    private AdacEntryLabels()
    {
    }


    public static String getDisplayName( IEntry entry )
    {
        if ( entry == null )
        {
            return ""; //$NON-NLS-1$
        }
        for ( String attr : NAME_ATTRS )
        {
            String value = getFirstString( entry, attr );
            if ( value != null && !value.isEmpty() )
            {
                return value;
            }
        }
        if ( entry.getRdn() != null )
        {
            return entry.getRdn().getName();
        }
        return entry.getDn() != null ? entry.getDn().getName() : ""; //$NON-NLS-1$
    }


    public static String getDescription( IEntry entry )
    {
        return getFirstString( entry, "description" ); //$NON-NLS-1$
    }


    public static String getObjectType( IEntry entry )
    {
        if ( entry == null )
        {
            return ""; //$NON-NLS-1$
        }
        Collection<ObjectClass> ocs = entry.getObjectClassDescriptions();
        if ( ocs == null || ocs.isEmpty() )
        {
            IAttribute ocAttr = entry.getAttribute( "objectClass" ); //$NON-NLS-1$
            if ( ocAttr != null && ocAttr.getValueSize() > 0 )
            {
                return mapObjectClass( ocAttr.getStringValues() );
            }
            return "Entry"; //$NON-NLS-1$
        }
        List<String> names = new ArrayList<>();
        for ( ObjectClass oc : ocs )
        {
            if ( oc.getNames() != null && !oc.getNames().isEmpty() )
            {
                names.add( oc.getNames().get( 0 ) );
            }
        }
        return mapObjectClass( names.toArray( new String[0] ) );
    }


    public static boolean isContainerLike( IEntry entry )
    {
        if ( entry == null )
        {
            return false;
        }
        String type = getObjectType( entry ).toLowerCase( Locale.ROOT );
        if ( type.contains( "organizationalunit" ) //$NON-NLS-1$
            || type.contains( "container" ) //$NON-NLS-1$
            || type.contains( "domain" ) //$NON-NLS-1$
            || type.contains( "organization" ) //$NON-NLS-1$
            || type.contains( "country" ) //$NON-NLS-1$
            || type.contains( "locality" ) ) //$NON-NLS-1$
        {
            return true;
        }
        return entry.hasChildren();
    }


    private static String mapObjectClass( String[] classes )
    {
        if ( classes == null || classes.length == 0 )
        {
            return "Entry"; //$NON-NLS-1$
        }
        String chosen = null;
        for ( String oc : classes )
        {
            if ( oc == null )
            {
                continue;
            }
            String lower = oc.toLowerCase( Locale.ROOT );
            if ( "top".equals( lower ) || "person".equals( lower ) //$NON-NLS-1$ //$NON-NLS-2$
                || "organizationalperson".equals( lower ) //$NON-NLS-1$
                || "posixaccount".equals( lower ) ) //$NON-NLS-1$
            {
                continue;
            }
            chosen = oc;
            if ( "user".equals( lower ) || "inetorgperson".equals( lower ) //$NON-NLS-1$ //$NON-NLS-2$
                || "group".equals( lower ) || "groupofnames".equals( lower ) //$NON-NLS-1$ //$NON-NLS-2$
                || "groupofuniquenames".equals( lower ) //$NON-NLS-1$
                || "organizationalunit".equals( lower ) //$NON-NLS-1$
                || "computer".equals( lower ) //$NON-NLS-1$
                || "container".equals( lower ) ) //$NON-NLS-1$
            {
                break;
            }
        }
        if ( chosen == null )
        {
            chosen = classes[classes.length - 1];
        }
        String lower = chosen.toLowerCase( Locale.ROOT );
        if ( "user".equals( lower ) || "inetorgperson".equals( lower ) ) //$NON-NLS-1$ //$NON-NLS-2$
        {
            return "User"; //$NON-NLS-1$
        }
        if ( "group".equals( lower ) || "groupofnames".equals( lower ) //$NON-NLS-1$ //$NON-NLS-2$
            || "groupofuniquenames".equals( lower ) ) //$NON-NLS-1$
        {
            return "Group"; //$NON-NLS-1$
        }
        if ( "organizationalunit".equals( lower ) ) //$NON-NLS-1$
        {
            return "Organizational Unit"; //$NON-NLS-1$
        }
        if ( "computer".equals( lower ) ) //$NON-NLS-1$
        {
            return "Computer"; //$NON-NLS-1$
        }
        if ( "container".equals( lower ) ) //$NON-NLS-1$
        {
            return "Container"; //$NON-NLS-1$
        }
        return chosen;
    }


    private static String getFirstString( IEntry entry, String attributeDescription )
    {
        IAttribute attribute = entry.getAttribute( attributeDescription );
        if ( attribute == null || attribute.getValueSize() == 0 )
        {
            return ""; //$NON-NLS-1$
        }
        IValue value = attribute.getValues()[0];
        return value != null ? value.getStringValue() : ""; //$NON-NLS-1$
    }
}
