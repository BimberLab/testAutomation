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

import org.junit.experimental.categories.Category;
import org.labkey.test.Locator;
import org.labkey.test.TestTimeoutException;
import org.labkey.test.categories.DailyA;
import org.labkey.test.categories.Reports;
import org.labkey.test.util.LogMethod;

/**
 * User: tchadick
 * Date: 6/10/13
 */
@Category({DailyA.class, Reports.class})
public class ReportSecurityTest extends ReportTest
{
    private static final String TEST_GRID_VIEW = "Test Grid View";

    protected static final String TEST_GROUP = "firstGroup";
    protected static final String TEST_USER = "report_user1@report.test";

    @Override
    protected void doCleanup(boolean afterTest) throws TestTimeoutException
    {
        deleteUsers(afterTest, TEST_USER);
        super.doCleanup(afterTest);
    }

    @LogMethod(category = LogMethod.MethodType.SETUP)
    protected void doCreateSteps()
    {
        enableEmailRecorder();

        // import study and wait; no specimens needed
        importStudy();
        waitForPipelineJobsToComplete(1, "study import", false);

        // need this to turn off the demographic bit in the DEM-1 dataset
        clickFolder(getFolderName());
        setDemographicsBit("DEM-1: Demographics", false);
    }

    @Override
    @LogMethod(category = LogMethod.MethodType.VERIFICATION)
    public void doVerifySteps()
    {
        // additional report and security tests
        setupDatasetSecurity();
        doReportSecurity();
    }

    @LogMethod
    protected void setupDatasetSecurity()
    {
        clickProject(getProjectName());
        clickFolder("My Study");

        // create a test group and give it container read perms
        enterPermissionsUI();

        createPermissionsGroup(TEST_GROUP);

        // add user to the first test group
        clickManageGroup(TEST_GROUP);
        setFormElement("names", TEST_USER);
        uncheckCheckbox("sendEmail");
        clickButton("Update Group Membership");

        enterPermissionsUI();
        setPermissions(TEST_GROUP, "Reader");
        clickButton("Save and Finish");

        // give the test group read access to only the DEM-1 dataset
        clickFolder("My Study");
        enterStudySecurity();

        // enable advanced study security
        prepForPageLoad();
        selectOptionByValue(Locator.name("securityString"), "ADVANCED_READ");
        newWaitForPageToLoad(30000);

        click(Locator.xpath("//td[.='" + TEST_GROUP + "']/..//th/input[@value='READOWN']"));
        clickAndWait(Locator.id("groupUpdateButton"));

        selectOptionByText("dataset.1", "Read");
        clickAndWait(Locator.xpath("//form[@id='datasetSecurityForm']//a[@class='labkey-button']/span[text() = 'Save']"));
    }

    @LogMethod
    protected void doReportSecurity()
    {
        // create charts
        clickProject(getProjectName());
        clickFolder(getFolderName());

        clickAndWait(Locator.linkWithText("APX-1: Abbreviated Physical Exam"));
        clickMenuButton("Charts", "Create Chart View");
        waitForElement(Locator.xpath("//select[@name='columnsX']"), WAIT_FOR_JAVASCRIPT);
        selectOptionByText("columnsX", "1. Weight");
        selectOptionByText("columnsY", "4. Pulse");
        fireEvent(Locator.name("columnsY"), SeleniumEvent.change);
        checkCheckbox(Locator.name("participantChart"));
        clickButton("Save", 0);
        sleep(2000);

        setFormElement("reportName", "participant chart");
        clickButton("OK", 0);

        waitForElement(Locator.navButton("Views"), WAIT_FOR_JAVASCRIPT);

        clickMenuButton("Views", "default");
        waitForElement(Locator.navButton("Views"), WAIT_FOR_JAVASCRIPT);
        clickMenuButton("Charts", "Create Chart View");
        waitForElement(Locator.xpath("//select[@name='columnsX']"), WAIT_FOR_JAVASCRIPT);

        // create a non-participant chart
        selectOptionByText("columnsX", "1. Weight");
        selectOptionByText("columnsY", "4. Pulse");
        fireEvent(Locator.name("columnsY"), SeleniumEvent.change);
        clickButton("Save", 0);
        sleep(2000);

        setFormElement("reportName", "non participant chart");
        setFormElement("description", "a private chart");
        checkCheckbox("shareReport");
        clickButton("OK", 0);

        waitForElement(Locator.navButton("Views"), WAIT_FOR_JAVASCRIPT);

        // create grid view
        clickFolder(getFolderName());
        goToManageViews();

        clickAddReport("Grid View");
        setFormElement(Locator.name("label"), TEST_GRID_VIEW);
        selectOptionByText(Locator.id("datasetSelection"), "APX-1 (APX-1: Abbreviated Physical Exam)");
        clickButton("Create View");

        // test security
        clickProject(getProjectName());
        clickFolder("My Study");

        goToManageViews();
        clickReportPermissionsLink("participant chart");
        click(Locator.id("useCustom"));
        checkCheckbox(Locator.xpath("//td[.='" + TEST_GROUP + "']/..//td/input[@type='checkbox']"));
        clickButton("Save");

        goToManageViews();
        clickReportPermissionsLink(TEST_GRID_VIEW);
        click(Locator.id("useCustom"));
        checkCheckbox(Locator.xpath("//td[.='" + TEST_GROUP + "']/..//td/input[@type='checkbox']"));
        clickButton("Save");

        goToAdminConsole();
        impersonate(TEST_USER);
        clickProject(getProjectName());
        clickFolder("My Study");

        assertElementNotPresent(Locator.linkWithText("APX-1: Abbreviated Physical Exam"));
        clickAndWait(Locator.linkWithText("participant chart"));

        clickFolder(getFolderName());
        clickAndWait(Locator.linkWithText(TEST_GRID_VIEW));
        assertTextPresent("999320016");
        pushLocation();
        clickMenuButton("Views", "default");
        assertTextPresent("User does not have read permission on this dataset.");
/*
        no longer showing the query button by default.
        popLocation();
        clickMenuButton("Query", "Query:APX-1: Abbreviated Physical Exam");
        assertTextPresent("User does not have read permission on this dataset.");
*/
        stopImpersonating();
    }
}
