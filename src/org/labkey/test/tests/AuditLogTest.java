/*
 * Copyright (c) 2008-2013 LabKey Corporation
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
import org.labkey.test.BaseSeleniumWebTest;
import org.labkey.test.BaseWebDriverTest;
import org.labkey.test.Locator;
import org.labkey.test.TestTimeoutException;
import org.labkey.test.util.DataRegionTable;

/**
 * User: Karl Lum
 * Date: Mar 13, 2008
 */
public class AuditLogTest extends BaseWebDriverTest
{
    public static final String USER_AUDIT_EVENT = "User events";
    public static final String GROUP_AUDIT_EVENT = "Group events";
    public static final String PROJECT_AUDIT_EVENT = "Project and Folder events";
    public static final String ASSAY_AUDIT_EVENT = "Copy-to-Study Assay events";

    private static final String AUDIT_TEST_USER = "audit_user1@auditlog.test";
    private static final String AUDIT_TEST_PROJECT = "AuditVerifyTest";

    public static final String COMMENT_COLUMN = "Comment";

    public String getAssociatedModuleDirectory()
    {
        return "server/modules/audit";
    }

    @Override
    protected String getProjectName()
    {
        return AUDIT_TEST_PROJECT;
    }

    @Override
    protected void checkQueries(){} // Skip.  Project is deleted as part of test

    @Override
    protected void checkViews(){} // Skip.  Project is deleted as part of test

    protected void doCleanup(boolean afterTest) throws TestTimeoutException
    {
        // Needed for pre-clean only. User & project are deleted during test.
        if (!afterTest)
        {
            deleteUsers(false, AUDIT_TEST_USER);
            deleteProject(getProjectName(), false);
        }
    }

    protected void doTestSteps() throws Exception
    {
        userAuditTest();
        groupAuditTest();
    }

    protected void userAuditTest() throws Exception
    {
        log("testing user audit events");
        createUser(AUDIT_TEST_USER, null);
        impersonate(AUDIT_TEST_USER);
        stopImpersonating();
        signOut();
        signIn(AUDIT_TEST_USER, "asdf", false); // Bad login.  Existing User
        signIn(AUDIT_TEST_USER + "fail", "asdf", false); // Bad login.  Non-existent User
        simpleSignIn();
        deleteUser(AUDIT_TEST_USER);

        verifyAuditEvent(this, USER_AUDIT_EVENT, COMMENT_COLUMN, AUDIT_TEST_USER + " was added to the system", 10);
        verifyAuditEvent(this, USER_AUDIT_EVENT, COMMENT_COLUMN, AUDIT_TEST_USER + " was impersonated by", 10);
        verifyAuditEvent(this, USER_AUDIT_EVENT, COMMENT_COLUMN, "impersonated " + AUDIT_TEST_USER, 10);
        verifyAuditEvent(this, USER_AUDIT_EVENT, COMMENT_COLUMN, AUDIT_TEST_USER + " was no longer impersonated by", 10);
        verifyAuditEvent(this, USER_AUDIT_EVENT, COMMENT_COLUMN, "stopped impersonating " + AUDIT_TEST_USER, 10);
        verifyAuditEvent(this, USER_AUDIT_EVENT, COMMENT_COLUMN, AUDIT_TEST_USER + " failed to login: incorrect password.", 10);
        verifyAuditEvent(this, USER_AUDIT_EVENT, COMMENT_COLUMN, AUDIT_TEST_USER + "fail failed to login: user does not exist.", 10);
        verifyAuditEvent(this, USER_AUDIT_EVENT, COMMENT_COLUMN, AUDIT_TEST_USER + " was deleted from the system", 10);
    }

    protected void groupAuditTest() throws Exception
    {
        log("testing group audit events");

        _containerHelper.createProject(AUDIT_TEST_PROJECT, null);
        createPermissionsGroup("Testers");
        assertPermissionSetting("Testers", "No Permissions");
        setPermissions("Testers", "Editor");

        clickManageGroup("Testers");
        setFormElement(Locator.name("names"), AUDIT_TEST_USER);
        uncheckCheckbox("sendEmail");
        clickButton("Update Group Membership");
        deleteUser(AUDIT_TEST_USER);
        deleteProject(AUDIT_TEST_PROJECT, true);

        verifyAuditEvent(this, GROUP_AUDIT_EVENT, COMMENT_COLUMN, "A new security group named Testers was created", 10);
        verifyAuditEvent(this, GROUP_AUDIT_EVENT, COMMENT_COLUMN, "The user/group Testers was assigned to the security role Editor.", 10);
        verifyAuditEvent(this, GROUP_AUDIT_EVENT, COMMENT_COLUMN, "User: " + AUDIT_TEST_USER + " was added as a member to Group: Testers", 10);
        verifyAuditEvent(this, USER_AUDIT_EVENT, COMMENT_COLUMN, AUDIT_TEST_USER + " was deleted from the system", 10);

        log("testing project audit events");
        verifyAuditEvent(this, PROJECT_AUDIT_EVENT, COMMENT_COLUMN, AUDIT_TEST_PROJECT + " was created", 5);
        verifyAuditEvent(this, PROJECT_AUDIT_EVENT, COMMENT_COLUMN, AUDIT_TEST_PROJECT + " was deleted", 5);
    }

    public static void verifyAuditEvent(BaseSeleniumWebTest instance, String eventType, String column, String msg, int rowsToSearch)
    {
        if (!instance.isTextPresent("Audit Log"))
        {
            instance.ensureAdminMode();

            instance.goToAdminConsole();
            instance.clickAndWait(Locator.linkWithText("audit log"));
        }

        if (!instance.getSelectedOptionText(Locator.name("view")).equals(eventType))
        {
            instance.selectOptionByText(Locator.name("view"), eventType);
            instance.waitForPageToLoad();
        }
        instance.log("searching for audit entry: " + msg);
        DataRegionTable table = new DataRegionTable("audit", instance, false);
        int i = table.getColumn(column);
        Assert.assertTrue("Text '" + msg + "' was not present", findTextInDataRegion(table, i, msg, rowsToSearch + 2));
    }

    public static boolean findTextInDataRegion(DataRegionTable table, int column, String txt, int rowsToSearch)
    {
        for (int row = 0; row < rowsToSearch; row++)
        {
            String value = table.getDataAsText(row, column);
            if (value.contains(txt))
                return true;
        }
        return false;
    }
}
