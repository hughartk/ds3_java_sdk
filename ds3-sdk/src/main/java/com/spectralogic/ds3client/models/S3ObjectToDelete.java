/*
 * ******************************************************************************
 *   Copyright 2014-2016 Spectra Logic Corporation. All Rights Reserved.
 *   Licensed under the Apache License, Version 2.0 (the "License"). You may not use
 *   this file except in compliance with the License. A copy of the License is located at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file.
 *   This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 *   CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations under the License.
 * ****************************************************************************
 */

// This code is auto-generated, do not modify
package com.spectralogic.ds3client.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.lang.String;
import java.util.UUID;

@JacksonXmlRootElement(namespace = "Data")
public class S3ObjectToDelete {

    // Variables
    @JsonProperty("Key")
    private String key;

    // Variables
    @JsonProperty("VersionId")
    private UUID versionId;

    // Constructor
    public S3ObjectToDelete() {
        //pass
    }

    // Getters and Setters
    
    public String getKey() {
        return this.key;
    }

    public void setKey(final String key) {
        this.key = key;
    }
    
    
    public UUID getVersionId()
    {
        return versionId;
    }
    
    
    public void setVersionId( final UUID versionId )
    {
        this.versionId = versionId;
    }
}