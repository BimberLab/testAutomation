/*
 * Copyright (c) 2007-2012 LabKey Corporation
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

import org.labkey.test.BaseWebDriverTest;
import org.labkey.test.Locator;
import org.labkey.test.util.PasswordUtil;

/**
 * Created by IntelliJ IDEA.
 * User: Mark Griffith
 * Date: Jan 18, 2006
 */
public class UserPermissionsTest extends BaseWebDriverTest
{
    protected static final String PERM_PROJECT_NAME = "PermissionCheckProject";
    protected static final String DENIED_SUB_FOLDER_NAME = "UnlinkedFolder";
    protected static final String GAMMA_SUB_FOLDER_NAME = "GammaFolder";
    protected static final String GAMMA_EDITOR_GROUP_NAME = "GammaEditor";
    protected static final String GAMMA_AUTHOR_GROUP_NAME = "GammaAuthor";
    protected static final String GAMMA_READER_GROUP_NAME = "GammaReader";
//    protected static final String GAMMA_RESTRICTED_READER_GROUP_NAME = "GammaRestrictedReader";
    protected static final String GAMMA_SUBMITTER_GROUP_NAME = "GammaSubmitter";
    protected static final String GAMMA_ADMIN_GROUP_NAME = "GammaAdmin";
    //permissions
    //editor, author, reader, restricted_reader, submitter, Admin
    protected static final String GAMMA_EDITOR_USER = "gammaeditor@security.test";
    protected static final String GAMMA_EDITOR_PAGE_TITLE = "This is a Test Message from : " + GAMMA_EDITOR_USER;
    protected static final String GAMMA_AUTHOR_USER = "gammaauthor@security.test";
    protected static final String GAMMA_AUTHOR_PAGE_TITLE = "This is a Test Message from : " + GAMMA_AUTHOR_USER;
    protected static final String GAMMA_READER_USER = "gammareader@security.test";
    protected static final String GAMMA_PROJECT_ADMIN_USER = "gammaadmin@security.test";

    //I can't really find any docs on what this is exactly?
//    protected static final String GAMMA_RESTRICTED_READER_USER = "gammarestricted@security.test";
//    protected static final String GAMMA_SUBMITTER_USER = "gammasubmitter@security.test";

    public String getAssociatedModuleDirectory()
    {
        return "server/modules/core";
    }

    @Override
    protected String getProjectName()
    {
        return PERM_PROJECT_NAME;
    }

    protected void doCleanup(boolean afterTest)
    {
        log(this.getClass().getName() + " Cleaning Up");
        deleteProject(PERM_PROJECT_NAME, afterTest);

        deleteUsers(afterTest, GAMMA_EDITOR_USER);
        deleteUsers(afterTest, GAMMA_AUTHOR_USER);
        deleteUsers(afterTest, GAMMA_READER_USER);
        deleteUsers(afterTest, GAMMA_PROJECT_ADMIN_USER);
    }

    protected void doTestSteps()
    {
        enableEmailRecorder();
        userPermissionRightsTest();
    }

    /**
     * Create some projects, create some groups, permissions for those groups
     * Create some users, assign to groups and validate the permissions by
     * impersonating the user.
     */
    private void userPermissionRightsTest()
    {
        _containerHelper.createProject(PERM_PROJECT_NAME, null);
        createPermissionsGroup(GAMMA_EDITOR_GROUP_NAME);
        assertPermissionSetting(GAMMA_EDITOR_GROUP_NAME, "No Permissions");
        setPermissions(GAMMA_EDITOR_GROUP_NAME, "Editor");
        createUserInProjectForGroup(GAMMA_EDITOR_USER, PERM_PROJECT_NAME, GAMMA_EDITOR_GROUP_NAME, false);

        createSubfolder(PERM_PROJECT_NAME, PERM_PROJECT_NAME, DENIED_SUB_FOLDER_NAME, "None", new String[] {"Messages", "Wiki"}, true);
        createSubfolder(PERM_PROJECT_NAME, DENIED_SUB_FOLDER_NAME, GAMMA_SUB_FOLDER_NAME, "None", new String[] {"Messages", "Wiki"}, true);
        addWebPart("Messages");
        assertLinkPresentWithText("Messages");
        addWebPart("Wiki");
        assertTextPresent("Wiki");
        assertLinkPresentWithText("Create a new wiki page");
        addWebPart("Wiki Table of Contents");

        //Create Reader User
        clickLinkWithText(PERM_PROJECT_NAME);
        enterPermissionsUI();
        createPermissionsGroup(GAMMA_READER_GROUP_NAME);
        assertPermissionSetting(GAMMA_READER_GROUP_NAME, "No Permissions");
        setPermissions(GAMMA_READER_GROUP_NAME, "Reader");
        createUserInProjectForGroup(GAMMA_READER_USER, PERM_PROJECT_NAME, GAMMA_READER_GROUP_NAME, false);
        //Create Author User
        clickLinkWithText(PERM_PROJECT_NAME);
        enterPermissionsUI();
        createPermissionsGroup(GAMMA_AUTHOR_GROUP_NAME);
        assertPermissionSetting(GAMMA_AUTHOR_GROUP_NAME, "No Permissions");
        setPermissions(GAMMA_AUTHOR_GROUP_NAME, "Author");
        createUserInProjectForGroup(GAMMA_AUTHOR_USER, PERM_PROJECT_NAME, GAMMA_AUTHOR_GROUP_NAME, false);
        //Create the Submitter User
        clickLinkWithText(PERM_PROJECT_NAME);
        enterPermissionsUI();
        createPermissionsGroup(GAMMA_SUBMITTER_GROUP_NAME);
        assertPermissionSetting(GAMMA_SUBMITTER_GROUP_NAME, "No Permissions");
        setPermissions(GAMMA_SUBMITTER_GROUP_NAME, "Submitter");
        // TODO: Add submitter to a group
        /*
         * I need a way to test submitter, I can't even view a folder where submitter has permissions when
         * impersonating on my local labkey, so may require special page?
         */

        //Make sure the Editor can edit
        impersonate(GAMMA_EDITOR_USER);
        clickFolder(PERM_PROJECT_NAME);
        clickFolder(GAMMA_SUB_FOLDER_NAME);
        clickWebpartMenuItem("Messages", "Email", "Preferences");
        checkRadioButton("emailPreference", "0");
        clickButton("Update");
        clickFolder(GAMMA_SUB_FOLDER_NAME);

        clickWebpartMenuItem("Messages", "New");
        setFormElement(Locator.id("title"), GAMMA_EDITOR_PAGE_TITLE);
        setFormElement(Locator.id("body"), "This is a secret message that was generated by " + GAMMA_EDITOR_USER);
        selectOptionByValue(Locator.name("rendererType"), "RADEOX");
        clickButton("Submit");
        stopImpersonating();

        //Make sure that the Author can read as well, edit his own but not edit the Edtiors
        impersonate(GAMMA_AUTHOR_USER);
        clickFolder(PERM_PROJECT_NAME);
        clickFolder(GAMMA_SUB_FOLDER_NAME);
        clickWebpartMenuItem("Messages", "Email", "Preferences");
        checkRadioButton("emailPreference", "0");
        clickButton("Update");
        clickFolder(GAMMA_SUB_FOLDER_NAME);

        clickWebpartMenuItem("Messages", "New");
        setFormElement(Locator.id("title"), GAMMA_AUTHOR_PAGE_TITLE);
        setFormElement(Locator.id("body"), "This is a secret message that was generated by " + GAMMA_AUTHOR_USER);
        selectOptionByValue(Locator.name("rendererType"), "RADEOX");
        clickButton("Submit");
        //Can't edit the editor's
        clickLinkWithText(GAMMA_SUB_FOLDER_NAME);
        clickLinkContainingText("view message", 1);
        assertTextPresent(GAMMA_EDITOR_PAGE_TITLE);
        assertLinkNotPresentWithText("Edit");
        stopImpersonating();

        //Make sure that the Reader can read but not edit
        impersonate(GAMMA_READER_USER);
        clickFolder(PERM_PROJECT_NAME);
        clickFolder(GAMMA_SUB_FOLDER_NAME);

        clickLinkContainingText("view message", 0);
        assertTextPresent(GAMMA_AUTHOR_PAGE_TITLE);
        assertLinkNotPresentWithText("Edit");

        clickLinkWithText(GAMMA_SUB_FOLDER_NAME);
        clickLinkContainingText("view message", 1);
        assertTextPresent(GAMMA_EDITOR_PAGE_TITLE);
        assertLinkNotPresentWithText("Edit");
        stopImpersonating();

        //switch back to Editor and edit
        impersonate(GAMMA_EDITOR_USER);
        clickFolder(PERM_PROJECT_NAME);
        clickFolder(GAMMA_SUB_FOLDER_NAME);
        //Go back and Edit
        clickLinkContainingText("view message", 1);
        assertTextPresent(GAMMA_EDITOR_PAGE_TITLE);
        clickLinkWithText("edit");
        setFormElement(Locator.id("body"), "This is a secret message that was generated by " + GAMMA_EDITOR_USER + "\nAnd I have edited it");

        //Remove permission from folder to verify unviewability
        log("Check for disallowed folder links");
        stopImpersonating();
        clickFolder(PERM_PROJECT_NAME);
        clickFolder(GAMMA_SUB_FOLDER_NAME);
        enterPermissionsUI();
        uncheckInheritedPermissions();
        waitAndClickButton("Save and Finish");
        clickFolder(DENIED_SUB_FOLDER_NAME);
        enterPermissionsUI();
        uncheckInheritedPermissions();
        removePermission(GAMMA_READER_GROUP_NAME, "Reader");
        clickButton("Save and Finish");

        // Test that a project admin is confined to a single project when impersonating a project user. Site admins
        // are not restricted in this way, so we need to create and login as a new user with project admin permissions.
        clickLinkWithText(PERM_PROJECT_NAME);
        createPermissionsGroup(GAMMA_ADMIN_GROUP_NAME);
        setPermissions(GAMMA_ADMIN_GROUP_NAME, "Project Administrator");
        createUserInProjectForGroup(GAMMA_PROJECT_ADMIN_USER, PERM_PROJECT_NAME, GAMMA_ADMIN_GROUP_NAME, true);
        clickLinkWithTextNoTarget("here");
        clickLinkContainingText("setPassword.view");
        setFormElement(Locator.id("password"), PasswordUtil.getPassword());
        setFormElement(Locator.id("password2"), PasswordUtil.getPassword());
        clickButton("Set Password");
        signOut();
        signIn(GAMMA_PROJECT_ADMIN_USER, PasswordUtil.getPassword(), true);
        clickFolder(PERM_PROJECT_NAME);
        projectImpersonate(GAMMA_READER_USER);
        clickFolder(PERM_PROJECT_NAME);
        expandFolder(DENIED_SUB_FOLDER_NAME);
        assertLinkNotPresentWithText(DENIED_SUB_FOLDER_NAME);
        // Ensure only one project visible during project impersonation. Regression test 13346
        assertElementPresent(Locator.xpath("//table[@class='labkey-expandable-nav' and .//a[text()='Projects']]//td[@class='labkey-nav-tree-text']"), 1);
        expandFolder(GAMMA_SUB_FOLDER_NAME);
        assertLinkPresentWithText(GAMMA_SUB_FOLDER_NAME);

        //Reset ourselves to the global user so we can do cleanup
        stopImpersonating();
        signOut();
        signIn();
    }

    private void clickLinkWithTextNoTarget(String text)
    {
        String href = getAttribute(Locator.linkWithText(text), "href");
        beginAt(href);
    }

    /**
     * Create a User in a Project for a Specific group
     *
     * @param userName
     * @param projectName
     * @param groupName
     */
    private void createUserInProjectForGroup(String userName, String projectName, String groupName, boolean sendEmail)
    {
        if (isElementPresent(Locator.permissionRendered()))
        {
            exitPermissionsUI();
            clickLinkWithText(projectName);
        }
        enterPermissionsUI();
        clickManageGroup(groupName);
        setFormElement(Locator.name("names"), userName);
        if (!sendEmail)
            uncheckCheckbox("sendEmail");
        clickButton("Update Group Membership");
    }
}
