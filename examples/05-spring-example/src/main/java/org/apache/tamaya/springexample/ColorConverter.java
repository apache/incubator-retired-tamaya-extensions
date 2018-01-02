/*
 * Copyright 2012-2014 the original author or authors.
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
package org.apache.tamaya.springexample;

import javax.config.spi.Converter;
import java.awt.*;

/**
 * Simple demo converter for Color.
 */
public class ColorConverter implements Converter<Color> {

    @Override
    public Color convert(String value) {
        if(value.length()<7){
            return null;
        }
        if(!value.startsWith("#")){
            throw new IllegalArgumentException("Invalid color, format is: #RRGGBB");
        }
        int r = Integer.parseInt(value.substring(1,3),8);
        int g = Integer.parseInt(value.substring(3,5),8);
        int b = Integer.parseInt(value.substring(5,7),8);
        return new Color(r, g, b);
    }
}
