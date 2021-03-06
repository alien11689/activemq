/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.activemq.transport.ws;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class WSTransportHttpTraceTest extends WSTransportTest {

    private String enableTraceParam;
    private int expectedStatus;

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                //value is empty
                {"http.enableTrace=", HttpStatus.FORBIDDEN_403},
                //default, trace method not specified
                {null, HttpStatus.FORBIDDEN_403},
                // enable http trace method
                {"http.enableTrace=true", HttpStatus.OK_200},
                // disable trace method
                {"http.enableTrace=false", HttpStatus.FORBIDDEN_403}
        });
    }

    public WSTransportHttpTraceTest(final String enableTraceParam, final int expectedStatus) {
        this.enableTraceParam = enableTraceParam;
        this.expectedStatus = expectedStatus;
    }

    @Override
    protected String getWSConnectorURI() {
        String uri = "ws://127.0.0.1:61623?websocket.maxTextMessageSize=99999&transport.maxIdleTime=1001";
        uri = enableTraceParam != null ? uri + "&" + enableTraceParam : uri;
        return uri;
    }

    /**
     * This tests whether the TRACE method is enabled or not
     * @throws Exception
     */
    @Test(timeout=10000)
    public void testHttpTraceEnabled() throws Exception {
        HttpClient httpClient = new HttpClient();
        httpClient.start();

        final CountDownLatch latch = new CountDownLatch(1);
        Request request = httpClient.newRequest("http://127.0.0.1:61623").method(HttpMethod.TRACE);
        final AtomicInteger status = new AtomicInteger();
        request.send(new BufferingResponseListener() {
            @Override
            public void onComplete(Result result) {
                status.set(result.getResponse().getStatus());
                latch.countDown();
            }
        });
        latch.await();
        assertEquals(expectedStatus, status.get());
    }

    @Override
    @Ignore
    @Test
    public void testBrokerStart() throws Exception {
    }
}
