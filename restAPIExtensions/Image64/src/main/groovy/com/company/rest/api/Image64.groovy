package com.company.rest.api;

import groovy.json.JsonBuilder

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.apache.http.HttpHeaders
import org.bonitasoft.web.extension.ResourceProvider
import org.bonitasoft.web.extension.rest.RestApiResponse
import org.bonitasoft.web.extension.rest.RestApiResponseBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.bonitasoft.web.extension.rest.RestAPIContext
import org.bonitasoft.web.extension.rest.RestApiController

import java.time.LocalDate

import org.apache.commons.fileupload.FileItem
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.io.IOUtils

class Image64 implements RestApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(Image64.class)

    @Override
    RestApiResponse doHandle(HttpServletRequest request, RestApiResponseBuilder responseBuilder, RestAPIContext context) {
        List<FileItem> multiparts = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
		def result = []
		// Bonita send multiparts, that's why the loop
		for (FileItem item : multiparts) {
		// if it's not a form field, but a file
			if (!item.isFormField()) {
				// Get the file stream
				InputStream fileContent = item.getInputStream();
				// InputStream to byteArray
				def data = IOUtils.toByteArray(fileContent);
				result = ["code" : new String(Base64.getEncoder().encode(data)), "filename": item.getName()];
			}
		}	

        // Send the result as a JSON representation
        // You may use buildPagedResponse if your result is multiple
        return buildResponse(responseBuilder, HttpServletResponse.SC_OK, new JsonBuilder(result).toString())
    }

    /**
     * Build an HTTP response.
     *
     * @param  responseBuilder the Rest API response builder
     * @param  httpStatus the status of the response
     * @param  body the response body
     * @return a RestAPIResponse
     */
    RestApiResponse buildResponse(RestApiResponseBuilder responseBuilder, int httpStatus, Serializable body) {
        return responseBuilder.with {
            withResponseStatus(httpStatus)
            withResponse(body)
            build()
        }
    }

    /**
     * Returns a paged result like Bonita BPM REST APIs.
     * Build a response with a content-range.
     *
     * @param  responseBuilder the Rest API response builder
     * @param  body the response body
     * @param  p the page index
     * @param  c the number of result per page
     * @param  total the total number of results
     * @return a RestAPIResponse
     */
    RestApiResponse buildPagedResponse(RestApiResponseBuilder responseBuilder, Serializable body, int p, int c, long total) {
        return responseBuilder.with {
            withContentRange(p,c,total)
            withResponse(body)
            build()
        }
    }

    /**
     * Load a property file into a java.util.Properties
     */
    Properties loadProperties(String fileName, ResourceProvider resourceProvider) {
        Properties props = new Properties()
        resourceProvider.getResourceAsStream(fileName).withStream { InputStream s ->
            props.load s
        }
        props
    }

}
