/*
 * Copyright (c) 2012-2013 LabKey Corporation
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
import org.labkey.test.Locator;
import org.labkey.test.util.LogMethod;

import java.io.File;

/**
 * User: jeckels
 * Date: Nov 20, 2007
 */
// TODO : TEMPORARY CLASS FOR INCREMENTAL WEBDRIVER UPGRADE : MERGE BACK TO NON-WD CLASS WHEN COMPLETE//
public abstract class AbstractAssayTestWD extends SimpleApiTestWD
{
    // protected final static int WAIT_FOR_JAVASCRIPT = 5000;  uncomment to override base class

    //constants added for security tests
    protected final static String TEST_ASSAY_PERMS_READER = "Reader";                 //name of built-in reader role
    private final static String TEST_ASSAY_PERMS_EDITOR = "Editor";                 //name of built-in editor role
    private final static String TEST_ASSAY_PERMS_NONE = "No Permissions";           //name of built-in no perms role

    private final static String TEST_ASSAY_GRP_USERS = "Users";                     //name of built-in Users group
    private final static String TEST_ASSAY_GRP_PIS = "PIs";                         //name of PI group to add

    protected final static String TEST_ASSAY_USR_PI1 = "pi1@security.test";           //user within that group
    protected final static String TEST_ASSAY_USR_TECH1 = "labtech1@security.test";    //a typical lab tech user

    protected final static String TEST_ASSAY_PRJ_SECURITY = "Assay Security Test";    //test project (kept separate from previous tests)
    protected final static String TEST_ASSAY_FLDR_LABS = "Labs";                      //sub-folder of test project
    protected final static String TEST_ASSAY_FLDR_LAB1 = "Lab 1";                     //sub-folder of Labs
    protected final static String TEST_ASSAY_FLDR_STUDIES = "Studies";                //sub-folder of test proj
    protected final static String TEST_ASSAY_FLDR_STUDY1 = "Study 1";                 //sub-folder of Studies
    protected final static String TEST_ASSAY_FLDR_STUDY2 = "Study 2";                 //another sub of Studies
    protected final static String TEST_ASSAY_FLDR_STUDY3 = "Study 3";                 //another sub of Studies
    protected final static String TEST_ASSAY_PERMS_STUDY_READALL = "READ";

    /**
     * Sets up the data pipeline for the specified project. This can be called from any page.
     * @param project name of project for which the pipeline should be setup
     */
    @LogMethod
    protected void setupPipeline(String project)
    {
        log("Setting up data pipeline for project " + project);
        clickProject(project);
        addWebPart("Data Pipeline");
        clickButton("Setup");
        File dir = getTestTempDir();
        dir.mkdirs();

        setPipelineRoot(dir.getAbsolutePath());

        //make sure it was set
        assertTextPresent("The pipeline root was set to '" + dir.getAbsolutePath() + "'");
    } //setupPipeline

    /**
     * Sets up the users, groups, folders and permissions for the security tests.
     * This creates the following:
     *  - a new project
     *  - a group called PIs in the project, with a user called pi1@security.test
     *  - a user called labtech1@security.test in the Users group
     *  - a folder structure like this:
     *      project
     *          Labs
     *              Lab 1 (PIs and Users are editors here, but Users are only readers elsewhere)
     *          Studies
     *              Study 1 (PIs are editors here but nowhere else)
     *              Study 2
     *              Study 3 (no one has read permissions here)
     *
     * This allow us to test the following scenarios:
     *  - an assay may be defined at the project level, and restricted users can upload data for it at a lower level (Lab 1)
     *  - the labtech should be able to set the target study to any study in which the labtech is a reader.
     *  - the PI may only publish to studies where the PI has editor permissions.
     *  - if the target study was set to a folder where the PI does not have editor perms, the system will
     *     warn the PI of this when publishing and force the PI to select one in which the PI does have editor perms.
     */
    @LogMethod
    protected void setupEnvironment()
    {
        //create a new project for the security tests
        log("Creating security test project");
        _containerHelper.createProject(TEST_ASSAY_PRJ_SECURITY, null);

        log("Setting up groups, users and initial permissions");

        //we should now be sitting on the new project security page
        //create a group in the project for PIs and make them readers by default
        createPermissionsGroup(TEST_ASSAY_GRP_PIS);
        setPermissions(TEST_ASSAY_GRP_PIS, TEST_ASSAY_PERMS_READER);

        //set Users group to be Readers by default
        setPermissions(TEST_ASSAY_GRP_USERS, TEST_ASSAY_PERMS_READER);

        //add a PI user to that group
        addUserToProjGroup(TEST_ASSAY_USR_PI1, TEST_ASSAY_PRJ_SECURITY,TEST_ASSAY_GRP_PIS);

        //add a lab tech user to the Users group
        addUserToProjGroup(TEST_ASSAY_USR_TECH1, TEST_ASSAY_PRJ_SECURITY, TEST_ASSAY_GRP_USERS);

        //add folder structure
        log("Setting up folder structure and folder permissions");
        createSubfolder(TEST_ASSAY_PRJ_SECURITY, TEST_ASSAY_PRJ_SECURITY, TEST_ASSAY_FLDR_LABS, "None", null, true);
        createSubfolder(TEST_ASSAY_PRJ_SECURITY, TEST_ASSAY_PRJ_SECURITY, TEST_ASSAY_FLDR_STUDIES, "None", null, true);
        createSubfolder(TEST_ASSAY_PRJ_SECURITY, TEST_ASSAY_FLDR_LABS, TEST_ASSAY_FLDR_LAB1, "None", null, true);
        createSubfolder(TEST_ASSAY_PRJ_SECURITY, TEST_ASSAY_FLDR_STUDIES, TEST_ASSAY_FLDR_STUDY1, "Study", null, true);
        createDefaultStudy();
        createSubfolder(TEST_ASSAY_PRJ_SECURITY, TEST_ASSAY_FLDR_STUDIES, TEST_ASSAY_FLDR_STUDY2, "Study", null, true);
        createDefaultStudy();
        createSubfolder(TEST_ASSAY_PRJ_SECURITY, TEST_ASSAY_FLDR_STUDIES, TEST_ASSAY_FLDR_STUDY3, "Study", null, true);
        clickButton("Create Study");
        //use date-based study
        click(Locator.xpath("(//input[@name='timepointType'])[1]"));
        setFormElement(Locator.xpath("//input[@name='startDate']"), "2000-01-01");
        clickButton("Create Study");

        clickAndWait(Locator.linkWithText("Manage Timepoints"));
        setFormElement(Locator.xpath("//input[@name='defaultTimepointDuration']"), "8");
        clickButton("Update");

        //setup security on sub-folders:
        // PIs should be Editors on Lab1 and Study1, but not Study2 or Study3
        // Users should be Editors on Lab1, readers on Study2, and nothing on Study3
        setSubfolderSecurity(TEST_ASSAY_PRJ_SECURITY, TEST_ASSAY_FLDR_LAB1, TEST_ASSAY_GRP_PIS, TEST_ASSAY_PERMS_EDITOR);
        setSubfolderSecurity(TEST_ASSAY_PRJ_SECURITY, TEST_ASSAY_FLDR_LAB1, TEST_ASSAY_GRP_USERS, TEST_ASSAY_PERMS_EDITOR);
        setSubfolderSecurity(TEST_ASSAY_PRJ_SECURITY, TEST_ASSAY_FLDR_STUDY1, TEST_ASSAY_GRP_PIS, TEST_ASSAY_PERMS_EDITOR);
        setSubfolderSecurity(TEST_ASSAY_PRJ_SECURITY, TEST_ASSAY_FLDR_STUDY3, TEST_ASSAY_GRP_USERS, TEST_ASSAY_PERMS_NONE);

        //setup study-level security:
        // TODO: due to bug 3625, the PIs group may not have study-level read permissions
        // in the Study1 folder by default. This is required in order to publish, so
        // we need to explicitly grant that permission for now. When this bug is fixed,
        // this code should be altered accordingly.
        // https://www.labkey.org/Issues/home/Developer/issues/details.view?issueId=3625
        // NOTE: it turns out that because we created the group and granted it read permissions
        // at the project level before creating the study folders, they did end up inheriting
        // study-level read permissions. However, we'll still do this just to be safe and it
        // ends up testing the study-level security anyway.
        log("Setting study-level permissions");
        setStudyPerms(TEST_ASSAY_PRJ_SECURITY, TEST_ASSAY_FLDR_STUDY1,
                        TEST_ASSAY_GRP_PIS, TEST_ASSAY_PERMS_STUDY_READALL);

        setStudyQCStates(TEST_ASSAY_PRJ_SECURITY, TEST_ASSAY_FLDR_STUDY1);

        //add the Assay List web part to the lab1 folder so we can upload data later as a labtech
        log("Adding assay list web part to lab1 folder");
        clickProject(TEST_ASSAY_PRJ_SECURITY);
        clickFolder(TEST_ASSAY_FLDR_LAB1);
        addWebPart("Assay List");
    } //setupEnvironment()

    /**
     * Sets the permissions for an existing group on an existing subfolder
     *
     * @param project   name of the existing project
     * @param subfolder name of the existing subfolder
     * @param group     name of the existing group
     * @param perms     permissions role to set (e.g., Editor, Reader, Author, No Permissions, etc.)
     */
    @LogMethod
    protected void setSubfolderSecurity(String project, String subfolder, String group, String perms)
    {
        log("Setting permissions for group '" + group + "' on subfolder '" + project + "/" + subfolder + "' to '" + perms + "'");
        if (isElementPresent(Locator.permissionRendered()) && isNavButtonPresent("Save and Finish"))
            clickButton("Save and Finish");
        clickProject(project);
        clickFolder(subfolder);
        enterPermissionsUI();
        uncheckInheritedPermissions();
        waitAndClickButton("Save", 0);
        waitForElement(Locator.permissionRendered());
        if (TEST_ASSAY_PERMS_NONE.equals(perms))
        {
            removePermission(group, "Editor");
            removePermission(group, "Reader");
        }
        else
            setPermissions(group, perms);
    } //setSubFolderSecurity()

    /**
     * Sets up study-level permissions on a given folder within a given project for a given group
     *
     * @param project   name of project
     * @param folder    name of the sub-folder
     * @param group     name of the group
     * @param perms     read permissions to set (use TEST_STUDY_PERMS_STUDY_* constants)
     */
    @LogMethod
    protected void setStudyPerms(String project, String folder, String group, String perms)
    {
        log("Setting study-level read permissions for group " + group + " in project " + project + " to " + perms);
        if (isElementPresent(Locator.permissionRendered()) && isNavButtonPresent("Save and Finish"))
            clickButton("Save and Finish");
        clickProject(project);
        clickFolder(folder);
        enterStudySecurity();

        prepForPageLoad();
        selectOptionByValue(Locator.name("securityString"), "ADVANCED_READ");
        newWaitForPageToLoad(30000);

        click(Locator.xpath("//td[.='" + group + "']/..//input[@value='" + perms + "']"));

        clickAndWait(Locator.id("groupUpdateButton"));
    } //setStudyPerms

    @LogMethod
    private void setStudyQCStates(String project, String folder)
    {
        log("Setting QC states in study " + folder + ".");
        clickProject(project);
        clickFolder(folder);
        clickTab("Manage");
        clickAndWait(Locator.linkWithText("Manage Dataset QC States"));
        setFormElement("newLabel", "Approved");
        setFormElement("newDescription", "We all like approval.");
        clickButton("Save");
        setFormElement("newLabel", "Pending Review");
        setFormElement("newDescription", "No one likes to be reviewed.");
        clickCheckbox("newPublicData");
        clickButton("Save");
        selectOptionByText("defaultAssayQCState", "Pending Review");
        clickButton("Save");
    }
    /**
     * Reverts to the admin account after impersonating a different user.
     * This will also work if the current user happens to be the admin,
     * though it will go through a sign out and sign in, requiring re-selection
     * of the project
     */
    protected void revertToAdmin()
    {
        log("reverting to admin");
        signOut();
        signIn();
    } //revertToAdmin();

    protected File[] getTestFiles()
    {
        return new File[0];
    }

    protected void setRequired(String where, int index)
    {
        String prefix = getPropertyXPath(where);
        _listHelper.clickRow(prefix, index);
        click(Locator.xpath(prefix + "//span[contains(@class,'x-tab-strip-text') and text()='Validators']"));
        waitAndClick(WAIT_FOR_JAVASCRIPT, Locator.xpath(prefix + "//span/input[@name='required']"), 0);
    }

    protected void clickEditAssayDesign(Boolean confirmEditInOtherContainer)
    {
        prepForPageLoad();
        click(Locator.linkWithText("manage assay design"));
        waitAndClick(Locator.linkWithText("edit assay design"));
        if (confirmEditInOtherContainer)
        {
            String alertText = getAlert();
            Assert.assertTrue("Alert did not contain expected text\nExpected: This assay is defined in the\nActual: " + alertText,
                    alertText.contains("This assay is defined in the"));
            Assert.assertTrue("Alert did not contain expected text\nExpected: Would you still like to edit it?\nActual: " + alertText,
                    alertText.contains("Would you still like to edit it?"));
        }
        newWaitForPageToLoad(WAIT_FOR_PAGE);
    }

    protected void enterStudySecurity()
    {
        enterPermissionsUI();
        _ext4Helper.clickTabContainingText("Study Security");
        waitAndClickButton("Study Security");
    }
}
