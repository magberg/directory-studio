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


import org.apache.directory.studio.connection.core.ConnectionServerType;
import org.apache.directory.studio.ldapbrowser.core.jobs.ServerTypeDetector;
import org.apache.directory.studio.ldapbrowser.core.model.IBrowserConnection;
import org.apache.directory.studio.ldapbrowser.core.model.IEntry;
import org.apache.directory.studio.ldapbrowser.core.model.IRootDSE;


/**
 * AD vs generic LDAP helpers for ADAC forms/wizards.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class AdacServerSupport
{
    public enum Kind
    {
        USER,
        GROUP,
        OU,
        OTHER
    }

    private AdacServerSupport()
    {
    }


    public static boolean isActiveDirectory( IBrowserConnection connection )
    {
        if ( connection == null )
        {
            return false;
        }
        ConnectionServerType type = null;
        try
        {
            if ( connection.getConnection() != null
                && connection.getConnection().getDetectedConnectionProperties() != null )
            {
                type = connection.getConnection().getDetectedConnectionProperties().getServerType();
            }
        }
        catch ( Exception ignore )
        {
            // fall through to Root DSE detect
        }
        if ( type == null || type == ConnectionServerType.UNKNOWN )
        {
            IRootDSE rootDSE = connection.getRootDSE();
            if ( rootDSE != null )
            {
                type = ServerTypeDetector.detectServerType( rootDSE );
            }
        }
        return type == ConnectionServerType.MICROSOFT_ACTIVE_DIRECTORY_2000
            || type == ConnectionServerType.MICROSOFT_ACTIVE_DIRECTORY_2003;
    }


    public static boolean isActiveDirectory( IEntry entry )
    {
        return entry != null && isActiveDirectory( entry.getBrowserConnection() );
    }


    public static Kind detectKind( IEntry entry )
    {
        if ( entry == null )
        {
            return Kind.OTHER;
        }
        String type = AdacEntryLabels.getObjectType( entry );
        if ( "User".equals( type ) ) //$NON-NLS-1$
        {
            return Kind.USER;
        }
        if ( "Group".equals( type ) ) //$NON-NLS-1$
        {
            return Kind.GROUP;
        }
        if ( "Organizational Unit".equals( type ) ) //$NON-NLS-1$
        {
            return Kind.OU;
        }
        return Kind.OTHER;
    }
}
