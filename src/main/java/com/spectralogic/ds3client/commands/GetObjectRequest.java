/*
 * ******************************************************************************
 *   Copyright 2014 Spectra Logic Corporation. All Rights Reserved.
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

package com.spectralogic.ds3client.commands;

import com.spectralogic.ds3client.HttpVerb;
import org.apache.http.entity.ContentType;

import java.util.UUID;

/**
 * Retrieves an object from DS3.  This should always be used within the context of a BulkGet command.
 * If not performance will be impacted.
 */
public class GetObjectRequest extends AbstractRequest {
    public static class Range {
        private final long start;
        private final long end;

        public Range(final long start, final long end) {
            this.start = start;
            this.end = end;
        }

        public long getStart() {
            return start;
        }

        public long getEnd() {
            return end;
        }
    }

    private final String bucketName;
    private final String objectName;
    private final UUID jobId;
    private Range byteRange = null;

    @Deprecated
    public GetObjectRequest(final String bucketName, final String objectName) {
        this(bucketName, objectName, null);
    }

    public GetObjectRequest(final String bucketName, final String objectName, final UUID jobId) {
        this.bucketName = bucketName;
        this.objectName = objectName;
        this.jobId = jobId;

        if(jobId != null) {
            this.getQueryParams().put("job", jobId.toString());
        }
    }

    /**
     * Sets a Range of bytes that should be retrieved from the object.
     */
    public GetObjectRequest withByteRange(final Range byteRange) {
        this.byteRange = byteRange;
        if (byteRange == null) {
            this.getHeaders().remove("Range");
        } else {
            this.getHeaders().put("Range", buildRangeHeaderText(byteRange));
        }
        return this;
    }
    
    private static String buildRangeHeaderText(Range byteRange) {
        return String.format("bytes=%d-%d", byteRange.getStart(), byteRange.getEnd());
    }

    public Range getByteRange() {
        return byteRange;
    }

    @Override
    public String getPath() {
        return "/" + bucketName + "/" + objectName;
    }

    @Override
    public ContentType getContentType() {
        return ContentType.APPLICATION_OCTET_STREAM;
    }

    @Override
    public HttpVerb getVerb() {
        return HttpVerb.GET;
    }

    public UUID getJobId() {
        return this.jobId;
    }
}
