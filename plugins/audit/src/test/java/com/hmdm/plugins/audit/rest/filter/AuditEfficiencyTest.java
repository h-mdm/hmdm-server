package com.hmdm.plugins.audit.rest.filter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class AuditEfficiencyTest {
    @Test
    void forwardsBulkWritesWithoutPerByteCalls() throws Exception {
        OutputStream delegate = mock(OutputStream.class);
        var wrapper = new ServletOutputStreamWrapper(delegate);
        byte[] bytes = {0, 1, 2, 3, 4};
        wrapper.write(bytes, 1, 3);
        wrapper.write(bytes);
        wrapper.write(255);
        verify(delegate).write(bytes, 1, 3);
        verify(delegate).write(bytes, 0, 5);
        verify(delegate).write(255);
        verifyNoMoreInteractions(delegate);
        assertArrayEquals(new byte[] {1, 2, 3, 0, 1, 2, 3, 4, (byte) 255}, wrapper.getContent());
    }

    @Test
    void failedBulkWriteIsNotCapturedAsSuccessful() throws Exception {
        OutputStream delegate = mock(OutputStream.class);
        byte[] bytes = {1, 2};
        IOException failure = new IOException("disconnected");
        doThrow(failure).when(delegate).write(bytes, 0, 2);
        var wrapper = new ServletOutputStreamWrapper(delegate);
        assertSame(failure, assertThrows(IOException.class, () -> wrapper.write(bytes)));
        assertEquals(0, wrapper.getContent().length);
    }

    @Test
    void statusOnlyAuditDoesNotWrapOrParseResponse() throws Exception {
        var request = request();
        var response = mock(HttpServletResponse.class);
        when(response.getStatus()).thenReturn(200);
        // Passing through the exact response means no audit buffer, stream or writer is introduced.
        FilterChain chain = (req, resp) -> assertSame(response, resp);
        var auditor = new ResourceAuditor("action", request, response, chain, false, false, "", "");
        auditor.doProcess();
        assertEquals(0, auditor.getAuditLogRecord().getErrorCode());
        verify(response, never()).getOutputStream();
        verify(response, never()).getWriter();
        when(response.getStatus()).thenReturn(500);
        assertEquals(2, auditor.getAuditLogRecord().getErrorCode());
    }

    @Test
    void checkedAuditPreservesSuccessApplicationErrorAndHttpError() throws Exception {
        assertChecked("{\"status\":\"OK\"}", 200, 0);
        assertChecked("{\"status\":null}", 200, 1);
        assertChecked("null", 200, 1);
        assertChecked("not JSON", 500, 2);
    }

    @Test
    void checkedAuditStillRejectsMalformedJson() {
        assertThrows(IOException.class, () -> assertChecked("not JSON", 200, 0));
    }

    private void assertChecked(String body, int status, int expected) throws Exception {
        var response = mock(HttpServletResponse.class);
        var output = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(output);
        ByteArrayOutputStream sent = new ByteArrayOutputStream();
        doAnswer(call -> {
            sent.write(call.getArgument(0, byte[].class), call.getArgument(1), call.getArgument(2));
            return null;
        }).when(output).write(any(byte[].class), anyInt(), anyInt());
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        FilterChain chain = (req, resp) -> {
            assertInstanceOf(ServletResponseAuditWrapper.class, resp);
            ((HttpServletResponse) resp).setStatus(status);
            resp.getOutputStream().write(bytes);
        };
        var auditor = new ResourceAuditor("action", request(), response, chain, false, true, "", "");
        auditor.doProcess();
        assertArrayEquals(bytes, sent.toByteArray());
        assertEquals(expected, auditor.getAuditLogRecord().getErrorCode());
    }

    private HttpServletRequest request() {
        var request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        return request;
    }
}
