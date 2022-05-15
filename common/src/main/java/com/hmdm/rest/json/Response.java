/*
 *
 * Headwind MDM: Open Source Android MDM Software
 * https://h-mdm.com
 *
 * Copyright (C) 2019 Headwind Solutions LLC (http://h-sms.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.hmdm.rest.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "A response from the application to request from client. The actual type of 'data' is specific to request.")
public class Response implements Serializable {

    private static final long serialVersionUID = 3268801711912541479L;
    
    @ApiModelProperty("A status of the server response.")
    private Response.ResponseStatus status;

    @ApiModelProperty("An optional message related to status.")
    private String message;

    @ApiModelProperty("A data requested by client.")
    private Object data;

    public Response() {
    }

    private Response(Response.ResponseStatus status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public Response.ResponseStatus getStatus() {
        return this.status;
    }

    public void setStatus(Response.ResponseStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return this.data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public static Response OK(String message, Object data) {
        return new Response(Response.ResponseStatus.OK, message, data);
    }

    public static Response OK(Object data) {
        return OK(null, data);
    }

    public static Response OK(String message) {
        return OK(message, null);
    }

    public static Response OK() {
        return OK(null, null);
    }

    public static Response WARNING(String message, Object data) {
        return new Response(Response.ResponseStatus.WARNING, message, data);
    }

    public static Response WARNING(Object data) {
        return WARNING(null, data);
    }

    public static Response WARNING(String message) {
        return WARNING(message, null);
    }

    public static Response WARNING() {
        return WARNING(null, null);
    }

    public static Response ERROR(String message, Object data) {
        return new Response(Response.ResponseStatus.ERROR, message, data);
    }

    public static Response ERROR(Object data) {
        return ERROR(null, data);
    }

    public static Response ERROR(String message) {
        return ERROR(message, null);
    }

    public static Response INTERNAL_ERROR() {
        return ERROR("error.internal.server", null);
    }

    public static Response PLUGIN_DISABLED() {
        return ERROR("error.resource.disabled", null);
    }

    public static Response PERMISSION_DENIED() {
        return ERROR("error.permission.denied", null);
    }

    public static Response OBJECT_NOT_FOUND_ERROR() {
        return ERROR("error.notfound.object", null);
    }

    public static Response DEVICE_NOT_FOUND_ERROR() {
        return ERROR("error.notfound.device", null);
    }

    public static Response DUPLICATE_ENTITY(String message) {
        return ERROR(message, null);
    }

    public static Response DUPLICATE_APPLICATION() {
        return ERROR("error.duplicate.application", null);
    }

    public static Response RECENT_APPLICATION_VERSION_EXISTS() {
        return ERROR("error.recent.application.version.exists", null);
    }

    public static Response APPLICATION_CONFIG_REFERENCE_EXISTS() {
        return ERROR("error.application.config.reference.exists", null);
    }

    public static Response APPLICATION_NOT_FOUND_ERROR() {
        return ERROR("error.application.not.found", null);
    }

    public static Response CONFIGURATION_DEVICE_REFERENCE_EXISTS() {
        return ERROR("error.configuration.device.use", null);
    }

    public static Response COMMON_APPLICATION_ACCESS_PROHIBITED() {
        return ERROR("error.common.application.access.prohibited", null);
    }

    public static Response DEVICE_EXISTS() {
        return ERROR("error.duplicate.device", null);
    }

    public static Response FILE_EXISTS() {
        return ERROR("error.duplicate.file", null);
    }

    public static Response FILE_USED() {
        return ERROR("error.used.file", null);
    }

    public static Response ERROR() {
        return ERROR(null, null);
    }

    public enum ResponseStatus {
        OK,
        WARNING,
        ERROR;

        ResponseStatus() {
        }
    }
}
