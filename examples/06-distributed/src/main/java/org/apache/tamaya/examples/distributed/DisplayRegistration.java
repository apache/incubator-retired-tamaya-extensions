/*
 * Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.tamaya.examples.distributed;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Objects;
import java.util.UUID;

import lombok.Data;

/**
 * Created by atsticks on 13.11.16.
 */
@Data
public class DisplayRegistration implements Serializable{

    private static final long serialVersionUID = 1L;

    private String id;
    private String displayName;
    private String host;
    private String displayModel;
    private boolean update;
    private long timestamp = System.currentTimeMillis();

    private DisplayRegistration(){}

    public DisplayRegistration(String displayName) {
        this.displayName = Objects.requireNonNull(displayName);
        this.displayModel = "fxDemo";

        this.id = UUID.randomUUID().toString();
    }

    public DisplayRegistration(String displayName, String displayModel) {
        this.displayModel = Objects.requireNonNull(displayModel);
        this.displayName = Objects.requireNonNull(displayName);
        this.id = UUID.randomUUID().toString();
        InetAddress adr = null;
        try {
            adr = InetAddress.getLocalHost();
            this.host = adr.getCanonicalHostName();
        } catch(Exception e){
            if (adr == null){
                this.host = "localhost";
            }else{
                this.host = adr.getHostName();
            }
        }
    }

    public DisplayRegistration update() {
        DisplayRegistration reg = new DisplayRegistration();
        reg.displayModel = this.displayModel;
        reg.displayName = this.displayName;
        reg.host = this.host;
        reg.id = this.id;
        reg.update = true;
        reg.timestamp = System.currentTimeMillis();
        return reg;
    }

}
