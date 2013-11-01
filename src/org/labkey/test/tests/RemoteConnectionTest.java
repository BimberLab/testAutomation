/*
 * Copyright (c) 2013 LabKey Corporation
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
import org.junit.experimental.categories.Category;
import org.labkey.test.BaseWebDriverTest;
import org.labkey.test.Locator;
import org.labkey.test.TestTimeoutException;
import org.labkey.test.WebTestHelper;
import org.labkey.test.categories.DailyB;
import org.labkey.test.util.DataRegionTable;
import org.labkey.test.util.PasswordUtil;
import org.labkey.test.util.PortalHelper;
import org.labkey.test.util.RemoteConnectionHelperWD;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

/**
 * User: scary dax
 * Date: 10/31/13
  */
@Category({DailyB.class})
public class RemoteConnectionTest extends BaseWebDriverTest
{
    private static final String PROJECT_NAME = "RemoteConnectionsTestProject";
    private static final String WACKY_NAME = "$?\\/.\"";
    private static final String EMPTY_NAME = "";
    private static final String CONNECTION_VALID ="RemoteConnnectionsTest_Valid";
    private static final String CONNECTION_BAD_SERVER ="RemoteConnnectionsTest_BadServer";
    private static final String CONNECTION_BAD_CREDENTIALS ="RemoteConnnectionsTest_BadCredentials";
    private static final String CONNECTION_BAD_CONTAINER ="RemoteConnnectionsTest_BadContainer";
    private static final String CONNECTION_EDIT ="RemoteConnnectionsTest_Edit";

    //
    // expected results for transform UI testing
    //
    @Override
    protected String getProjectName()
    {
        return PROJECT_NAME;
    }

    @Override
    public String getAssociatedModuleDirectory()
    {
        return "server/modules/dataintegration";
    }

    /**
    * Verifies the Remote Connection Management UI accessed via the
    * schema browser.  Note that an encryption key must be setup in the labkey.xml file
    * for this test to work
    */

    @Override
    protected void doTestSteps() throws Exception
    {
        doSetup();
        RemoteConnectionHelperWD rconnHelper = new RemoteConnectionHelperWD(this);

        //
        // no remote connections should exist for this new container
        //
        rconnHelper.goToManageRemoteConnections();
        Assert.assertTrue(0 == rconnHelper.getNumConnections());

        // invalid name, url, container, etc

        // add this when the issue 18933 gets addressed, we should be doing some validation
        /*
        rconnHelper.createConnection(WACKY_NAME, WACKY_NAME, WACKY_NAME, WACKY_NAME, WACKY_NAME);
        assert(null != rconnHelper.findConnection(WACKY_NAME));
        rconnHelper.deleteConnection(WACKY_NAME);
        assert(0 == rconnHelper.getNumConnections());
        */

        // leave name field blank
        rconnHelper.createConnection(EMPTY_NAME, EMPTY_NAME, EMPTY_NAME, EMPTY_NAME, EMPTY_NAME,
                "Connection name may not be blank");
        Assert.assertTrue(0 == rconnHelper.getNumConnections());

        // leave other fields blank
        rconnHelper.createConnection(CONNECTION_VALID, EMPTY_NAME, EMPTY_NAME, EMPTY_NAME, EMPTY_NAME,
                "All fields must be filled in");
        Assert.assertTrue(0 == rconnHelper.getNumConnections());

        // create a valid connection
        rconnHelper.createConnection(CONNECTION_VALID, getBaseURL(), getProjectName());
        Assert.assertTrue(1 == rconnHelper.getNumConnections());

        // issue 18914: ensure we can't create a connection with the same name as an existing connection
        rconnHelper.createConnection(CONNECTION_VALID, getBaseURL(), getProjectName(), "USER", "PASSWORD",
                "There is already a remote connection with the name '" + CONNECTION_VALID + "'.");
        Assert.assertTrue(1 == rconnHelper.getNumConnections());

        // create a connection with a bad URL
        rconnHelper.createConnection(CONNECTION_BAD_SERVER, "hptt://localhost/doesnotexist", getProjectName());
        // create a connection with invalid credentials
        rconnHelper.createConnection(CONNECTION_BAD_CREDENTIALS, getBaseURL(), getProjectName(),
                PasswordUtil.getUsername(), "invalid password buddy");
        // create a connection with a non-existent container
        rconnHelper.createConnection(CONNECTION_BAD_CONTAINER, getBaseURL(), "Container Does Not Exist");

        Assert.assertTrue(4 == rconnHelper.getNumConnections());
        Assert.assertTrue(null != rconnHelper.findConnection(CONNECTION_VALID));
        Assert.assertTrue(null != rconnHelper.findConnection(CONNECTION_BAD_SERVER));
        Assert.assertTrue(null != rconnHelper.findConnection(CONNECTION_BAD_CREDENTIALS));
        Assert.assertTrue(null != rconnHelper.findConnection(CONNECTION_BAD_CONTAINER));

        Assert.assertTrue(rconnHelper.testConnection(CONNECTION_VALID));
        Assert.assertTrue(!rconnHelper.testConnection(CONNECTION_BAD_SERVER));
        Assert.assertTrue(!rconnHelper.testConnection(CONNECTION_BAD_CREDENTIALS));
        Assert.assertTrue(!rconnHelper.testConnection(CONNECTION_BAD_CONTAINER));

        // now edit and verify we can't rename to an existing name
        rconnHelper.editConnection(CONNECTION_BAD_SERVER, CONNECTION_VALID, null, null, null, null,
                "There is already a remote connection with the name '" + CONNECTION_VALID + "'.");

        // edit for real to correct server and test that conn is successful
        rconnHelper.editConnection(CONNECTION_BAD_SERVER, CONNECTION_EDIT, getBaseURL(), null, null, null);
        // make sure the rename happend and we have the same number of connections
        Assert.assertTrue(4 == rconnHelper.getNumConnections());
        // make sure rename happened
        Assert.assertTrue(null == rconnHelper.findConnection(CONNECTION_BAD_SERVER));
        // ensure connection test is successful
        Assert.assertTrue(rconnHelper.testConnection(CONNECTION_EDIT));

        // clean ourselves up
        rconnHelper.deleteConnection(CONNECTION_VALID);
        rconnHelper.deleteConnection(CONNECTION_EDIT);
        rconnHelper.deleteConnection(CONNECTION_BAD_CREDENTIALS);
        rconnHelper.deleteConnection(CONNECTION_BAD_CONTAINER);
        Assert.assertTrue(0 == rconnHelper.getNumConnections());
    }

    protected void doSetup()
    {
        log("running setup");
        _containerHelper.createProject(PROJECT_NAME, null);
    }
}
