/*
 * Copyright (c) 2007-2013 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.labkey.test.tests;

import org.junit.Assert;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONValue;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.labkey.test.BaseWebDriverTest;
import org.labkey.test.Runner;
import org.labkey.test.TestTimeoutException;
import org.labkey.test.WebTestHelper;
import org.labkey.test.categories.BVT;
import org.labkey.test.categories.DRT;
import org.labkey.test.categories.UnitTests;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * User: brittp
 * Date: Nov 30, 2005
 * Time: 10:53:59 PM
 */
@Category({DRT.class, BVT.class, UnitTests.class})
public class JUnitTest extends TestSuite
{
    private static final DecimalFormat commaf0 = new DecimalFormat("#,##0");

    public JUnitTest() throws Exception
    {
    }

    @Override
    public void run(TestResult testResult)
    {
        log("\n\n=============== Starting " + getClass().getSimpleName() + Runner.getProgress() + " =================");
        try
        {
            super.run(testResult);
        }
        finally
        {
            log("=============== Completed " + getClass().getSimpleName() + Runner.getProgress() + " =================");
        }
    }

    // used when writing JUnitTest class name to the remainingTests.txt log file
    public String toString()
    {
        return getClass().getName();
    }

    private static class JUnitSeleniumHelper extends BaseWebDriverTest
    {
        public void unfail()
        {
            _testFailed = false;
        }

        protected String getProjectName() {return null;}
        protected void doTestSteps() throws Exception { }
        protected void doCleanup(boolean afterTest) throws TestTimeoutException
        { }
        public String getAssociatedModuleDirectory() { return null; }

        @Override public BrowserType bestBrowser() {return BrowserType.CHROME;}
    }

    // Use WebDriver to ensure we're upgraded
    private static void upgradeHelper() throws Exception
    {
        // TODO: remove upgrade helper from JUnitTest and run before suite starts.
        JUnitSeleniumHelper helper = new JUnitSeleniumHelper();
        try
        {
            helper.setUp();
            // sign in performs upgrade if necessary
            helper.signIn();
            helper.unfail();
        }
        catch (Exception e)
        {
            helper.dumpPageSnapshot();
            throw e;
        }
        catch (AssertionError a)
        {
            helper.dumpPageSnapshot();
            throw a;
        }
        finally
        {
            helper.tearDown();
        }
    }

    public static TestSuite suite() throws Exception
    {
        return suite(false);
    }

    public static TestSuite suite(boolean secondAttempt) throws Exception
    {
        HttpClient client = WebTestHelper.getHttpClient();
        HttpContext context = WebTestHelper.getBasicHttpContext();
        HttpGet method = null;
        HttpResponse response = null;
        try
        {
            String url = WebTestHelper.getBaseURL() + "/junit/testlist.view?";
            method = new HttpGet(url);
            response = client.execute(method, context);
            int status = response.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_OK)
            {
                TestSuite remotesuite = new JUnitTest();

                String responseBody = WebTestHelper.getHttpResponseBody(response);
                if (responseBody.isEmpty())
                    throw new AssertionFailedError("Failed to fetch remote junit test list: empty response");

                Object json = JSONValue.parse(responseBody);
                if (json == null &&
                        responseBody.contains("<title>Upgrade Status</title>") ||
                        responseBody.contains("<title>Upgrade Modules</title>") ||
                        responseBody.contains("<title>Account Setup</title>") ||
                        responseBody.contains("This server is being upgraded to a new version of LabKey Server."))
                {
                    if (secondAttempt)
                        throw new AssertionFailedError("Failed to update or bootstrap on second attempt: " + responseBody);

                    // perform upgrade then try to fetch the list again
                    upgradeHelper();
                    return suite(true);
                }

                if (json == null || !(json instanceof Map))
                    throw new AssertionFailedError("Can't parse or cast json response: " + responseBody);

                Map<String, List<Map<String, Object>>> obj = (Map<String, List<Map<String, Object>>>)json;
                for (Map.Entry<String, List<Map<String, Object>>> entry : obj.entrySet())
                {
                    String suiteName = entry.getKey();
                    TestSuite testsuite = new TestSuite(suiteName);
                    // Individual tests include both the class name and the requested timeout
                    for (Map<String, Object> testClass : entry.getValue())
                    {
                        String className = (String)testClass.get("className");
                        // Timeout is represented in seconds
                        int timeout = ((Number)testClass.get("timeout")).intValue();
                        testsuite.addTest(new RemoteTest(className, timeout));
                    }
                    remotesuite.addTest(testsuite);
                }

                return remotesuite;
            }
            else
            {
                System.err.println("Getting unit test list from server failed with error code " + status + ". Error page content is:");
                response.getEntity().writeTo(System.err);
                throw new AssertionFailedError("Failed to fetch remote junit test list (" + status + " - " + response.getStatusLine() + "): " + url);
            }
        }
        finally
        {
            if (response != null)
                EntityUtils.consume(response.getEntity());
            if (client != null)
                client.getConnectionManager().shutdown();
        }
    }

    public static class RemoteTest extends TestCase
    {
        String _remoteClass;
        /** Timeout in seconds to wait for the whole testcase to finish on the server */
        private final int _timeout;

        public RemoteTest(String remoteClass, int timeout)
        {
            super(remoteClass);
            _remoteClass = remoteClass;
            _timeout = timeout;
        }

        @Override
        protected void runTest() throws Throwable
        {
            HttpClient client = WebTestHelper.getHttpClient();
            HttpContext context = WebTestHelper.getBasicHttpContext();
            HttpResponse response = null;
            try
            {
                String url = WebTestHelper.getBaseURL() + "/junit/go.view?testCase=" + _remoteClass;
                HttpGet method = new HttpGet(url);
                client.getParams().setParameter("http.socket.timeout", _timeout * 1000);
                long startTime = System.currentTimeMillis();
                response = client.execute(method, context);
                int status = response.getStatusLine().getStatusCode();
                String responseBody = WebTestHelper.getHttpResponseBody(response);

                WebTestHelper.logToServer(getLogTestString("successful", startTime));
                WebTestHelper.logToServer(dump(responseBody, false));
                if (status == HttpStatus.SC_OK)
                {
                    log(getLogTestString("successful", startTime));
                    log(dump(responseBody, true));
                }
                else
                {
                    log(getLogTestString("failed", startTime));
                    Assert.fail("remote junit failed: " + _remoteClass + "\n" + dump(responseBody, true));
                }
            }
            catch (IOException ioe)
            {
                Assert.fail("failed to run remote junit test " + _remoteClass + ": " + ioe.getMessage());
            }
            finally
            {
                if (response != null)
                    EntityUtils.consume(response.getEntity());
                if (client != null)
                    client.getConnectionManager().shutdown();
            }
        }

        private String getLogTestString(String message, long startTime)
        {
            return "remote junit " + message + ": " + _remoteClass + " [" + commaf0.format(System.currentTimeMillis() - startTime) + " ms]";
        }

        static String dump(String response, boolean dumpFailures)
        {
            Map<String, Object> json = null;
            try
            {
                json = (Map<String, Object>)JSONValue.parse(response);
            }
            catch (Exception e)
            {
                // ignore
            }

            if (json == null)
                return response;

            StringBuilder sb = new StringBuilder();
            sb.append("ran: ").append(json.get("runCount"));
//            sb.append(", errors: ").append(json.get("errorCount"));
            sb.append(", failed: ").append(json.get("failureCount")).append("\n");
//            dumpFailures(sb, (List<Map<String, Object>>) json.get("errors"));
            if(dumpFailures) dumpFailures(sb, (List<Map<String, Object>>) json.get("failures"));
            return sb.toString();
        }

        static void dumpFailures(StringBuilder sb, List<Map<String, Object>> failures)
        {
            for (Map<String, Object> failure : failures)
            {
                if (failure.get("failedTest") != null)
                    sb.append(failure.get("failedTest")).append("\n");
                if (failure.get("exceptionMesage") != null)
                    sb.append("  ").append(failure.get("exceptionMessage")).append("\n");
                if (failure.get("trace") != null)
                    sb.append("  ").append(failure.get("trace")).append("\n");
                sb.append("\n");
            }
        }

    }

    static void log(String str)
    {
        if (str == null || str.length() == 0)
            return;
        String d = new SimpleDateFormat("HH:mm:ss,SSS").format(new Date());      // Include time with log entry.  Use format that matches labkey log.
        System.out.println(d + " " + str);
    }
}
