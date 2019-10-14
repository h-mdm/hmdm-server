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

package com.hmdm.plugins.audit.rest.filter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * <p>A wrapper around the {@link HttpServletResponse} object whose main purpose is to capture the status and content of
 * the response for audit logging purposes.</p>
 *
 * @author isv
 */
public class ServletResponseAuditWrapper extends HttpServletResponseWrapper {

    /**
     * <p>A status set for the response.</p>
     */
    private int status;

    /**
     * <p>An original response output stream.</p>
     */
    private ServletOutputStream outputStream;

    /**
     * <p>An original response writer.</p>
     */
    private PrintWriter writer;

    /**
     * <p>A wrapper around the response stream/writer used for captruing the content of the response.</p>
     */
    private ServletOutputStreamWrapper copier;

    /**
     * <p>Constructs new <code>ServletResponseAuditWrapper</code> instance. This implementation does nothing.</p>
     */
    public ServletResponseAuditWrapper(HttpServletResponse original) {
        super(original);
    }

    // Intercepted method.
    @Override
    public void sendError(int sc, String msg) throws IOException {
        this.status = sc;
        super.sendError(sc, msg);
    }

    // Intercepted method.
    @Override
    public void sendError(int sc) throws IOException {
        this.status = sc;
        super.sendError(sc);
    }

    // Intercepted method.
    @Override
    public void setStatus(int sc) {
        this.status = sc;
        super.setStatus(sc);
    }

    // Intercepted method.
    @Override
    public void setStatus(int sc, String sm) {
        this.status = sc;
        super.setStatus(sc, sm);
    }

    // Intercepted method.
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (writer != null) {
            throw new IllegalStateException("getWriter() has already been called on this response.");
        }

        if (outputStream == null) {
            outputStream = getResponse().getOutputStream();
            copier = new ServletOutputStreamWrapper(outputStream);
        }

        return copier;
    }

    // Intercepted method.
    @Override
    public PrintWriter getWriter() throws IOException {
        if (outputStream != null) {
            throw new IllegalStateException("getOutputStream() has already been called on this response.");
        }

        if (writer == null) {
            copier = new ServletOutputStreamWrapper(getResponse().getOutputStream());
            writer = new PrintWriter(new OutputStreamWriter(copier, getResponse().getCharacterEncoding()), true);
        }

        return writer;
    }

    // Intercepted method.
    @Override
    public void flushBuffer() throws IOException {
        if (writer != null) {
            writer.flush();
        } else if (outputStream != null) {
            copier.flush();
        }
    }

    /**
     * <p>Gets the content of the response.</p>
     *
     * @return a response content.
     */
    public byte[] getContent() {
        if (copier != null) {
            return copier.getContent();
        } else {
            return new byte[0];
        }
    }


    /**
     * <p>Gets the status set for the response.</p>
     *
     * @return a status set for the response.
     */
    public int getStatus() {
        return status;
    }
}
