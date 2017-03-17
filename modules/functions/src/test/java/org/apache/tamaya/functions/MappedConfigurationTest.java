package org.apache.tamaya.functions;

import org.apache.tamaya.Configuration;
import org.apache.tamaya.spi.ConfigurationContext;
import org.apache.tamaya.spisupport.DefaultConfigurationContext;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.internal.matchers.Null;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class MappedConfigurationTest {
    /*
     * Tests for getOrDefault(String, String)
     */

    @Test(expected = NullPointerException.class)
    public void getOrDefaultWithTwoStringParametersThrowsNPEIfValueIsNull() throws Exception {
        MappedConfiguration mc = mock(MappedConfiguration.class);
        doReturn("z").when(mc).get(eq("a)"));
        doCallRealMethod().when(mc).getOrDefault(anyString(), anyString());

        mc.getOrDefault("a", (String)null);
    }

    @Test(expected = NullPointerException.class)
    public void getOrDefaultWithTwoStringParametersThrowsNPEIfKeyIsNull() throws Exception {
        MappedConfiguration mc = mock(MappedConfiguration.class);
        doCallRealMethod().when(mc).getOrDefault(anyString(), anyString());

        mc.getOrDefault(null, "z");
    }

}