package org.apache.tamaya.functions;

import org.apache.tamaya.spi.ConfigurationContext;
import org.apache.tamaya.spisupport.DefaultConfiguration;

class InMemoryConfiguration extends DefaultConfiguration {
    public InMemoryConfiguration(ConfigurationContext configurationContext) {
        super(configurationContext);
    }
    //        private Map<String, String> entries = new TreeMap<>();

//        public InMemoryConfiguration addEntry(String key, String value) {
//            entries.put(key, value);
//
//            return this;
//        }
}
