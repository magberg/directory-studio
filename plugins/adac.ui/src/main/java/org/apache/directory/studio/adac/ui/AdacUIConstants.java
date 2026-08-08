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


/**
 * Constants for the ADAC UI plugin.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class AdacUIConstants
{
    public static final String PERSPECTIVE_ADAC = "org.apache.directory.studio.adac.ui.perspective.AdacPerspective"; //$NON-NLS-1$

    public static final String VIEW_NAVIGATION = "org.apache.directory.studio.adac.ui.views.NavigationView"; //$NON-NLS-1$

    public static final String VIEW_OBJECT_LIST = "org.apache.directory.studio.adac.ui.views.ObjectListView"; //$NON-NLS-1$

    public static final String VIEW_TASKS = "org.apache.directory.studio.adac.ui.views.TasksView"; //$NON-NLS-1$

    /** Connections view from ldapbrowser.ui (docked under Navigation). */
    public static final String VIEW_CONNECTION = "org.apache.directory.studio.ldapbrowser.ui.views.connection.ConnectionView"; //$NON-NLS-1$

    public static final String PERSPECTIVE_LDAP = "org.apache.directory.studio.ldapbrowser.ui.perspective.BrowserPerspective"; //$NON-NLS-1$

    private AdacUIConstants()
    {
    }
}
