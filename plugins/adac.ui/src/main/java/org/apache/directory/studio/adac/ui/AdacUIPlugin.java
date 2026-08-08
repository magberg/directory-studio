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


import org.apache.directory.studio.connection.core.event.EventRunner;
import org.apache.directory.studio.connection.ui.UiThreadEventRunner;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


/**
 * Activator for the ADAC UI plugin.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AdacUIPlugin extends AbstractUIPlugin
{
    public static final String PLUGIN_ID = "org.apache.directory.studio.adac.ui"; //$NON-NLS-1$

    private static AdacUIPlugin plugin;

    private EventRunner eventRunner;


    public AdacUIPlugin()
    {
        plugin = this;
    }


    @Override
    public void start( BundleContext context ) throws Exception
    {
        super.start( context );
        if ( eventRunner == null )
        {
            eventRunner = new UiThreadEventRunner();
        }
    }


    @Override
    public void stop( BundleContext context ) throws Exception
    {
        plugin = null;
        eventRunner = null;
        super.stop( context );
    }


    public static AdacUIPlugin getDefault()
    {
        return plugin;
    }


    public EventRunner getEventRunner()
    {
        return eventRunner;
    }
}
