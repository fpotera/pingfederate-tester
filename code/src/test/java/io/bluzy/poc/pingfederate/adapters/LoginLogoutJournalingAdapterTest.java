package io.bluzy.poc.pingfederate.adapters;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.JsonObject;
import io.bluzy.poc.pingfederate.utils.HTTPRequester;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class LoginLogoutJournalingAdapterTest {

    @Mock
    HTTPRequester httpRequester;

    @Test
    void testSendRetry() throws Exception {
        LoginLogoutJournalingAdapter adapter = new LoginLogoutJournalingAdapter();

        int DEFAULT_RETRY_COUNT = (int) ReflectionTestUtils.getField(adapter, "DEFAULT_RETRY_COUNT");

        ReflectionTestUtils.setField(adapter, "m_httpRequester", httpRequester);
        when(httpRequester.sendHTTPRequest(any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () -> ReflectionTestUtils.invokeMethod(adapter, "send", null, null, null, null, null, null));
        verify(httpRequester, times(DEFAULT_RETRY_COUNT)).sendHTTPRequest(null, null, null, null, null, null);

        reset(httpRequester);
        when(httpRequester.sendHTTPRequest(any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException())
                .thenReturn(new JsonObject());
        assertEquals(new JsonObject(), ReflectionTestUtils.invokeMethod(adapter, "send", null, null, null, null, null, null));
        verify(httpRequester, times(2)).sendHTTPRequest(null, null, null, null, null, null);
    }
}
