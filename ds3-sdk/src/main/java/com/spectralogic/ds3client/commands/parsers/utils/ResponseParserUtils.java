/*
 * ******************************************************************************
 *   Copyright 2014-2019 Spectra Logic Corporation. All Rights Reserved.
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

package com.spectralogic.ds3client.commands.parsers.utils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.spectralogic.ds3client.models.ChecksumType;
import com.spectralogic.ds3client.models.Error;
import com.spectralogic.ds3client.networking.FailedRequestException;
import com.spectralogic.ds3client.networking.FailedRequestUsingMgmtPortException;
import com.spectralogic.ds3client.networking.Headers;
import com.spectralogic.ds3client.networking.WebResponse;
import com.spectralogic.ds3client.serializer.XmlOutput;
import com.spectralogic.ds3client.utils.Guard;
import com.spectralogic.ds3client.utils.ResponseUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public final class ResponseParserUtils {

    private final static Logger LOG = LoggerFactory.getLogger(ResponseParserUtils.class);
    final public static String UTF8 = "UTF-8";

    private static final String BLOB_CHECKSUM_TYPE_HEADER = "ds3-blob-checksum-type";
    private static final String BLOB_CHECKSUM_HEADER = "ds3-blob-checksum-offset-";
    private static final String VERSION_ID_HEADER = "version-id";
    private static final String CREATION_DATE_HEADER = "creation-date";

    private ResponseParserUtils() {
        // pass
    }

    public static boolean validateStatusCode(final int statusCode, final int ... expectedStatuses) {
        Preconditions.checkElementIndex(0, 1);
        final ImmutableSet<Integer> expectedSet = createExpectedSet(expectedStatuses);
        return expectedSet.contains(statusCode);
    }

    public static ChecksumType.Type determineChecksumType(final Headers headers) {
        for (final ChecksumType.Type type : ChecksumType.Type.values()) {
            if (getFirstHeaderValue(headers, "content-" + type.toString().replace("_", "").toLowerCase()) != null) {
                return type;
            }
        }
        LOG.debug("Did not find a content checksum header");
        return null;
    }

    protected static String getRequestId(final Headers headers) {
        final String requestId = getFirstHeaderValue(headers, "x-amz-request-id");
        if (Guard.isStringNullOrEmpty(requestId)) {
            LOG.error("Response headers does not contain x-amz-request-id");
            return null;
        }
        return requestId;
    }

    public static String getFirstHeaderValue(final Headers headers, final String key) {
        final List<String> valueList = headers.get(key);
        if (Guard.isNullOrEmpty(valueList)) {
            return null;
        } else {
            return valueList.get(0);
        }
    }

    public static long getSizeFromHeaders(final Headers headers) {
        if (headers == null) {
            LOG.debug("Could not get the headers to determine the content-length");
            return -1;
        }
        final List<String> contentLength = headers.get("Content-Length");
        if (Guard.isNullOrEmpty(contentLength) || contentLength.get(0) == null) {
            LOG.debug("Could not find the content-length header to determine the size of the request");
            return -1;
        }
        return Long.parseLong(contentLength.get(0));
    }

    public static boolean checkForManagementPortException(final int statusCode, final Headers headers) {
        return ((statusCode == FailedRequestUsingMgmtPortException.MGMT_PORT_STATUS_CODE)
                && (getFirstHeaderValue(headers, FailedRequestUsingMgmtPortException.MGMT_PORT_HEADER) != null));
    }

    public static Error parseErrorResponse(final String responseString) {
        if (Strings.isNullOrEmpty(responseString)) {
            return null;
        }
        try {
            return XmlOutput.fromXml(responseString, Error.class);
        } catch (final IOException e) {
            // It's likely the response string is not in a valid error format.
            LOG.error("Failed to parse error response: {}", e);
            return null;
        }
    }

    public static ImmutableSet<Integer> createExpectedSet(final int[] expectedStatuses) {
        final ImmutableSet.Builder<Integer> setBuilder = ImmutableSet.builder();
        for(final int status: expectedStatuses) {
            setBuilder.add(status);
        }
        return setBuilder.build();
    }

    public static FailedRequestException createFailedRequest(
            final WebResponse response,
            final int[] expectedStatusCodes) throws IOException {
        if (checkForManagementPortException(response.getStatusCode(), response.getHeaders())) {
            return new FailedRequestUsingMgmtPortException(
                    ResponseUtils.toImmutableIntList(expectedStatusCodes),
                    getRequestId(response.getHeaders()));
        }
        final String responseString = readResponseString(response);
        return new FailedRequestException(
                ResponseUtils.toImmutableIntList(expectedStatusCodes),
                response.getStatusCode(),
                parseErrorResponse(responseString),
                responseString,
                getRequestId(response.getHeaders()));
    }

    private static String readResponseString(final WebResponse response) throws IOException {
        if (response == null || response.getResponseStream() == null) {
            return "";
        }
        try(final StringWriter writer = new StringWriter();
            final InputStream content = response.getResponseStream()) {
            IOUtils.copy(content, writer, UTF8);
            return writer.toString();
        }
    }

    /**
     * Retrieves the version id from the response headers. If there is no version id header,
     * then null is returned. This is used in HeadObject response parsing.
     * @param headers The http response headers from the BP.
     * @return The version id if one exists.
     */
    public static UUID getVersionId(final Headers headers)  {
        final List<String> versions = headers.get(VERSION_ID_HEADER);
        switch (versions.size()) {
            case 0:
                return null;
            case 1:
                return UUID.fromString(versions.get(0));
            default:
                throw new IllegalArgumentException(String.format("Response has more than one header value for '%s'", VERSION_ID_HEADER));
        }
    }

    /**
     * Retrieves the creation date from the response headers. If there is no creation date header,
     * then null is returned. This is used in HeadObject response parsing.
     * @param headers The http response headers from the BP.
     * @return The creation date if one exists.
     */
    public static ZonedDateTime getCreationDate(final Headers headers) {
        final List<String> dates = headers.get(CREATION_DATE_HEADER);
        switch (dates.size()) {
            case 0:
                return null;
            case 1:
                return ZonedDateTime.parse(dates.get(0));
            default:
                throw new IllegalArgumentException(String.format("Response has more than one header value for '%s'", CREATION_DATE_HEADER));
        }
    }

    /**
     * Retrieves the blob checksum type from the response headers. If there is no blob checksum
     * type header, than NONE is returned. If there is more than one value for the blob checksum
     * type header, than an illegal argument exception is thrown.
     */
    public static ChecksumType.Type getBlobChecksumType(final Headers headers) {
        final List<String> types = headers.get(BLOB_CHECKSUM_TYPE_HEADER);
        switch (types.size()) {
            case 0:
                return ChecksumType.Type.NONE;
            case 1:
                return toBlobChecksumType(types.get(0));
            default:
                throw new IllegalArgumentException(String.format("Response has more than one header value for '%s'", BLOB_CHECKSUM_TYPE_HEADER));
        }
    }

    /**
     * Converts the response header value for {@code BLOB_CHECKSUM_TYPE_HEADER} into a {@code ChecksumType.Type}
     */
    static ChecksumType.Type toBlobChecksumType(final String checksumType) {
        if (Guard.isStringNullOrEmpty(checksumType)) {
            return ChecksumType.Type.NONE;
        }
        return ChecksumType.Type.valueOf(checksumType);
    }

    /**
     * Retrieves a map of blob offset to blob checksum from response headers.
     */
    public static ImmutableMap<Long, String> getBlobChecksumMap(final Headers headers) {
        if (headers.keys() == null) {
            return ImmutableMap.of();
        }

        final ImmutableMap.Builder<Long, String> builder = ImmutableMap.builder();
        headers.keys().stream()
                .filter(key -> key.startsWith(BLOB_CHECKSUM_HEADER))
                .forEach(key -> builder.put(getOffsetFromHeaderKey(key), getBlobChecksumValue(key, headers)));

        return builder.build();
    }

    /**
     * Retrieves the offset from the blob checksum header key. This assumes that the
     * provided key has the prefix {@code BLOB_CHECKSUM_HEADER} and that the postfix can be
     * converted into a Long.
     */
    private static Long getOffsetFromHeaderKey(final String key) {
        final String offset = key.substring(BLOB_CHECKSUM_HEADER.length());
        return Long.valueOf(offset);
    }

    /**
     * Retrieves the value from the specified header key assuming there is at most one value.
     * If there are no values for the key, then an empty string is returned.
     * If there is more than one value for the given key, an illegal argument exception is thrown.
     */
    private static String getBlobChecksumValue(final String key, final Headers headers) {
        final List<String> values = headers.get(key);
        switch (values.size()) {
            case 1:
                return values.get(0);
            default:
                throw new IllegalArgumentException(String.format("Expected response to have 1 value for blob checksum header '%s', but found '%d' values", key, values.size()));
        }
    }
}