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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by atsticks on 13.11.16.
 */
public class DisplayContent {
    private String displayId;
    private String title = "UNKNOWN";
    private Map<String,String> content = new HashMap<>();
    private long timestamp = System.currentTimeMillis();
    private String displayName;

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        if (!(o instanceof DisplayContent)){
            return false;
        }
        DisplayContent that = (DisplayContent) o;
        return getTimestamp() == that.getTimestamp() &&
                Objects.equals(getDisplayId(), that.getDisplayId()) &&
                Objects.equals(getTitle(), that.getTitle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDisplayId(), getTitle(), getTimestamp());
    }

    @Override
    public String toString() {
        return "DisplayContent{" +
                "displayId='" + getDisplayId() + '\'' +
                ", title='" + getTitle() + '\'' +
                ", content=" + getContent() +
                '}';
    }

    /**
     * @return the displayId
     */
    public String getDisplayId() {
        return displayId;
    }

    /**
     * @param displayId the displayId to set
     */
    public void setDisplayId(String displayId) {
        this.displayId = displayId;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the content
     */
    public Map<String,String> getContent() {
        return content;
    }

    /**
     * @param content the content to set
     */
    public void setContent(Map<String,String> content) {
        this.content = content;
    }

    /**
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return the displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param displayName the displayName to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
