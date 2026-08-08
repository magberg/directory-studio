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


import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;


/**
 * Shared ADAC-inspired colors (SWT; more reliable than CSS on macOS).
 */
public final class AdacChrome
{
    private static Color headerBg;
    private static Color headerFg;
    private static Color tasksBg;
    private static Color sectionFg;


    private AdacChrome()
    {
    }


    public static Color headerBackground()
    {
        return headerBg != null ? headerBg : ( headerBg = new Color( Display.getDefault(), 0, 90, 158 ) );
    }


    public static Color headerForeground()
    {
        return headerFg != null ? headerFg : ( headerFg = new Color( Display.getDefault(), 255, 255, 255 ) );
    }


    public static Color tasksBackground()
    {
        return tasksBg != null ? tasksBg : ( tasksBg = new Color( Display.getDefault(), 243, 243, 243 ) );
    }


    public static Color sectionForeground()
    {
        return sectionFg != null ? sectionFg : ( sectionFg = new Color( Display.getDefault(), 0, 90, 158 ) );
    }
}
