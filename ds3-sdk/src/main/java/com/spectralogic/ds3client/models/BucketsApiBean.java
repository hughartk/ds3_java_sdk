/*
 * ******************************************************************************
 *   Copyright 2014-2015 Spectra Logic Corporation. All Rights Reserved.
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
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.spectralogic.ds3client.models.BucketApiBean;
import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.spectralogic.ds3client.models.UserApiBean;

@JacksonXmlRootElement(namespace = "ListAllMyBucketsResult")
public class BucketsApiBean {

    // Variables
    @JsonProperty("Buckets")
    @JacksonXmlElementWrapper(useWrapping = true)
    private List<BucketApiBean> buckets = new ArrayList<>();

    @JsonProperty("Owner")
    private UserApiBean owner;

    // Constructor
    public BucketsApiBean() {
        //pass
    }

    // Getters and Setters
    
    public List<BucketApiBean> getBuckets() {
        return this.buckets;
    }

    public void setBuckets(final List<BucketApiBean> buckets) {
        this.buckets = buckets;
    }


    public UserApiBean getOwner() {
        return this.owner;
    }

    public void setOwner(final UserApiBean owner) {
        this.owner = owner;
    }

}