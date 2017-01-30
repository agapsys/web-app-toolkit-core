/*
 * Copyright 2016 Agapsys Tecnologia Ltda-ME.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.agapsys.web.toolkit.utils;

import java.io.File;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;


public class SettingsTest {

    @Test
    public void testContains() {
        ApplicationSettings settings = new ApplicationSettings();

        Assert.assertFalse(settings.containsSection(null));
        Assert.assertFalse(settings.containsSection("mySection"));
        Assert.assertFalse(settings.containsProperty("myRootKey"));
        Assert.assertFalse(settings.containsProperty("mySection, myKey"));

        settings.setProperty("myRootKey", "myRootValue");
        Assert.assertTrue(settings.containsSection(null));
        Assert.assertFalse(settings.containsSection("mySection"));
        Assert.assertTrue(settings.containsProperty("myRootKey"));
        Assert.assertTrue(settings.containsProperty(null, "myRootKey"));

        settings.setProperty("mySection", "myKey", "myValue");
        Assert.assertTrue(settings.containsSection(null));
        Assert.assertTrue(settings.containsSection("mySection"));
        Assert.assertTrue(settings.containsProperty("myRootKey"));
        Assert.assertTrue(settings.containsProperty(null, "myRootKey"));
        Assert.assertTrue(settings.containsProperty("mySection", "myKey"));
        Assert.assertTrue(settings.containsProperty("mySection", "myKey"));
    }

    @Test
    public void testSetIfAbsent() {
        ApplicationSettings settings = new ApplicationSettings();

        Assert.assertTrue(settings.setPropertyIfAbsent("myRootKey", "myRootValue"));
        Assert.assertFalse(settings.setPropertyIfAbsent("myRootKey", "myRootValue"));

        Assert.assertTrue(settings.setPropertyIfAbsent("mySection", "myKey", "myValue"));
        Assert.assertFalse(settings.setPropertyIfAbsent("mySection", "myKey", "myValue"));
    }

    @Test
    public void testRemove() {
        ApplicationSettings settings = new ApplicationSettings();

        Assert.assertFalse(settings.removeSection(null));
        Assert.assertFalse(settings.removeSection("mySection"));
        Assert.assertFalse(settings.removeProperty("myKey"));
        Assert.assertFalse(settings.removeProperty("mySection", "myKey"));

        Assert.assertTrue(settings.setProperty("myRootKey1", "myRootValue1"));
        Assert.assertTrue(settings.setProperty("myRootKey2", "myRootValue2"));
        Assert.assertTrue(settings.setProperty("mySection", "myKey1", "myValue1"));
        Assert.assertTrue(settings.setProperty("mySection", "myKey2", "myValue2"));

        Assert.assertTrue(settings.removeProperty("myRootKey1"));
        Assert.assertFalse(settings.removeProperty("myRootKey1"));

        Assert.assertTrue(settings.removeProperty("myRootKey2")); // <-- section will be empty after this!
        Assert.assertFalse(settings.removeProperty("myRootKey2"));

        Assert.assertFalse(settings.containsProperty("myRootKey1"));
        Assert.assertFalse(settings.containsProperty("myRootKey2"));
        Assert.assertFalse(settings.containsSection(null));

        Assert.assertTrue(settings.removeProperty("mySection", "myKey1"));
        Assert.assertFalse(settings.removeProperty("mySection", "myKey1"));

        Assert.assertTrue(settings.removeProperty("mySection", "myKey2")); // <-- section will be empty after this!
        Assert.assertFalse(settings.removeProperty("mySection", "myKey2"));
        Assert.assertFalse(settings.containsSection("mySection"));

        Assert.assertTrue(settings.setProperty("myRootKey1", "myRootValue1"));
        Assert.assertTrue(settings.setProperty("myRootKey2", "myRootValue2"));
        Assert.assertTrue(settings.setProperty("mySection", "myKey1", "myValue1"));
        Assert.assertTrue(settings.setProperty("mySection", "myKey2", "myValue2"));

        Assert.assertTrue(settings.removeSection(null));
        Assert.assertFalse(settings.removeProperty("myRootKey1"));
        Assert.assertFalse(settings.removeProperty("myRootKey2"));

        Assert.assertTrue(settings.removeSection("mySection"));
        Assert.assertFalse(settings.removeProperty("mySection", "myKey1"));
        Assert.assertFalse(settings.removeProperty("mySection", "myKey2"));
    }

    @Test
    public void testSet() {
        ApplicationSettings settings = new ApplicationSettings();

        // Valid keys...
        Assert.assertTrue(settings.setProperty("myRootKey1", "myRootValue1"));
        Assert.assertTrue(settings.setProperty("myRootKey2", "myRootValue2"));
        Assert.assertTrue(settings.setProperty("mySection", "myKey1", "myValue1"));
        Assert.assertTrue(settings.setProperty("mySection", "myKey2", "myValue2"));

        // Same values...
        Assert.assertFalse(settings.setProperty("myRootKey1", "myRootValue1"));
        Assert.assertFalse(settings.setProperty("myRootKey2", "myRootValue2"));
        Assert.assertFalse(settings.setProperty("mySection", "myKey1", "myValue1"));
        Assert.assertFalse(settings.setProperty("mySection", "myKey2", "myValue2"));

        // Invalid keys...
        Throwable error;

        error = null; try { settings.setProperty(null, "0-1", "lalala"); } catch (IllegalArgumentException ex) { error = ex; }
        Assert.assertTrue(error.getMessage().toLowerCase().contains("invalid key"));
        Assert.assertNotNull(error);

        error = null; try { settings.setProperty("mySection", "0-1", "lalala"); } catch (IllegalArgumentException ex) { error = ex; }
        Assert.assertTrue(error.getMessage().toLowerCase().contains("invalid key"));
        Assert.assertNotNull(error);

        // Invalid section...
        error = null; try { settings.setProperty("0-1", "myKey", "lalala"); } catch (IllegalArgumentException ex) { error = ex; }
        Assert.assertTrue(error.getMessage().toLowerCase().contains("invalid section"));
        Assert.assertNotNull(error);

        // Invald values...
        error = null; try { settings.setProperty(null, "myKey", null); } catch (IllegalArgumentException ex) { error = ex; }
        Assert.assertTrue(error.getMessage().toLowerCase().contains("null value"));
        Assert.assertNotNull(error);

    }

    @Test
    public void testGet() {
        ApplicationSettings settings = new ApplicationSettings();

        Assert.assertNull(settings.getProperty("myRootKey", null));
        Assert.assertNull(settings.getProperty("mySection", "myKey", null));

        settings.setProperty("myRootKey", "myRootValue");
        settings.setProperty("mySection", "myKey", "myValue");

        Assert.assertEquals("myRootValue", settings.getProperty("myRootKey", null));
        Assert.assertEquals("myRootValue", settings.getProperty(null, "myRootKey", null));

        Assert.assertEquals("myValue", settings.getProperty("mySection", "myKey", null));

        settings.clear();
        Assert.assertNull(settings.getProperty("myRootKey", null));
        Assert.assertNull(settings.getProperty("mySection", "myKey", null));
    }

    @Test
    public void testStore() throws IOException {
        File tmpFile = File.createTempFile("settings", ".ini");

        ApplicationSettings settings = new ApplicationSettings();
        settings.setProperty("myRootKey1", "myRootValue1");
        settings.setProperty("myRootKey2", "myRootValue2");
        settings.setProperty("mySection1", "myKey1", "myValue1");
        settings.setProperty("mySection1", "myKey2", "myValue2");
        settings.setProperty("myRootKey3", "myRootValue3");
        settings.setProperty("mySection1", "myKey3", "myValue3");
        settings.setProperty("mySection2", "mySection2.myKey1", "myValue");

        settings.store(tmpFile);

        Throwable error = null; try { settings.store(tmpFile, false); } catch(IOException ex) { error = ex; }
        Assert.assertNotNull(error);
        Assert.assertTrue(error.getMessage().toLowerCase().contains("already exists"));

        ApplicationSettings loadedSettings = ApplicationSettings.load(tmpFile);
        Assert.assertEquals(settings, loadedSettings);

        tmpFile.delete();

    }
}
