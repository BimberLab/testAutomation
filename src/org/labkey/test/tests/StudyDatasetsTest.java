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

import junit.framework.Assert;
import org.labkey.test.Locator;
import org.labkey.test.util.DataRegionTable;
import org.labkey.test.util.LogMethod;
import org.labkey.test.util.PortalHelper;

import java.util.HashMap;
import java.util.Map;

public class StudyDatasetsTest extends StudyBaseTest
{
    private static final String CATEGORY1 = "Category1";
    private static final String GROUP1A = "Group1A";
    private static final String GROUP1B = "Group1B";
    private static final String CATEGORY2 = "Category2";
    private static final String GROUP2A = "Group2A";
    private static final String GROUP2B = "Group2B";
    private static final String EXTRA_GROUP = "Extra Group";
    private static final String[] PTIDS = {"999320016","999320518","999320529","999320533","999320557","999320565"};
    private static final String CUSTOM_VIEW_WITH_DATASET_JOINS = "Chemistry + Criteria + Demographics";
    private static final String CUSTOM_VIEW_PRIVATE = "My Private Custom View";
    private static final String TIME_CHART_REPORT_NAME = "Time Chart: Body Temp + Pulse For Group 2";
    private static final String SCATTER_PLOT_REPORT_NAME = "Scatter: Systolic vs Diastolic";
    private static final String PTID_REPORT_NAME = "Mouse Report: 2 Dem Vars + 3 Other Vars";
    private static Map<String, String> EXPECTED_REPORTS = new HashMap<>();
    private static Map<String, String> EXPECTED_CUSTOM_VIEWS = new HashMap<>();


    @Override
    @LogMethod(category = LogMethod.MethodType.SETUP)
    protected void doCreateSteps()
    {
        importStudy();
        // wait for study (but not specimens) to finish loading
        waitForPipelineJobsToComplete(1, "study import", false);

        _studyHelper.createCustomParticipantGroup(getProjectName(), getFolderName(), GROUP1A, "Mouse", CATEGORY1, true, null, PTIDS[0], PTIDS[1]);
        _studyHelper.createCustomParticipantGroup(getProjectName(), getFolderName(), GROUP1B, "Mouse", CATEGORY1, false, null, PTIDS[2], PTIDS[3]);
        _studyHelper.createCustomParticipantGroup(getProjectName(), getFolderName(), GROUP2A, "Mouse", CATEGORY2, true, null, PTIDS[1], PTIDS[3]);
        _studyHelper.createCustomParticipantGroup(getProjectName(), getFolderName(), GROUP2B, "Mouse", CATEGORY2, false, null, PTIDS[2], PTIDS[4]);
    }

    @Override
    @LogMethod(category = LogMethod.MethodType.VERIFICATION)
    protected void doVerifySteps()
    {
        createDataset("A");
        renameDataset("A", "Original A", "A", "Original A", "XTest", "YTest", "ZTest");
        createDataset("A");
        deleteFields("A");

        checkFieldsPresent("Original A", "YTest", "ZTest");

        verifySideFilter();

        verifyReportAndViewDatasetReferences();
    }

    protected void createDataset(String name)
    {
        goToManageDatasets();

        waitForText("Create New Dataset");
        click(Locator.xpath("//a[text()='Create New Dataset']"));
        waitForElement(Locator.xpath("//input[@name='typeName']"));
        setFormElement(Locator.xpath("//input[@name='typeName']"), name);
        clickButton("Next");

        waitForElement(Locator.xpath("//input[@id='name0-input']"));
        assertTextNotPresent("XTest");
        setFormElement(Locator.xpath("//input[@id='name0-input']"), "XTest");
        clickButtonContainingText("Add Field", 0);
        waitForElement(Locator.xpath("//input[@id='name1-input']"));
        assertTextNotPresent("YTest");
        setFormElement(Locator.xpath("//input[@id='name1-input']"), "YTest");
        clickButtonContainingText("Add Field", 0);
        waitForElement(Locator.xpath("//input[@id='name2-input']"));
        assertTextNotPresent("ZTest");
        setFormElement(Locator.xpath("//input[@id='name2-input']"), "ZTest");
        clickButton("Save");
 }

    protected void renameDataset(String orgName, String newName, String orgLabel, String newLabel, String... fieldNames)
    {
        goToManageDatasets();

        waitForElement(Locator.xpath("//a[text()='" + orgName + "']"));
        click(Locator.xpath("//a[text()='" + orgName + "']"));
        waitForText("Edit Definition");
        clickButton("Edit Definition");

        waitForElement(Locator.xpath("//input[@name='dsName']"));
        setFormElement(Locator.xpath("//input[@name='dsName']"), newName);
        setFormElement(Locator.xpath("//input[@name='dsLabel']"), newLabel);

        for (String fieldName : fieldNames)
        {
            assertTextPresent(fieldName);
        }

        clickButton("Save");

        // fix dataset label references in report and view mappings
        for (Map.Entry<String, String> entry : EXPECTED_REPORTS.entrySet())
        {
            if (orgLabel.equals(entry.getValue()))
                EXPECTED_REPORTS.put(entry.getKey(), newLabel);
        }
        for (Map.Entry<String, String> entry : EXPECTED_CUSTOM_VIEWS.entrySet())
        {
            if (orgLabel.equals(entry.getValue()))
                EXPECTED_CUSTOM_VIEWS.put(entry.getKey(), newLabel);
        }
}

    protected void deleteFields(String name)
    {
        goToManageDatasets();

        waitForElement(Locator.xpath("//a[text()='" + name + "']"));
        click(Locator.xpath("//a[text()='" + name + "']"));
        waitForText("Edit Definition");
        clickButton("Edit Definition");

        waitForElement(Locator.xpath("//div[@id='partdelete_2']"));
        mouseClick(Locator.id("partdelete_2").toString());
        clickButtonContainingText("OK", 0);
        waitForElement(Locator.xpath("//div[@id='partdelete_1']"));
        mouseClick(Locator.id("partdelete_1").toString());

        assertTextPresent("XTest");
        assertElementNotPresent(Locator.xpath("//input[@id='name1-input']"));
        assertElementNotPresent(Locator.xpath("//input[@id='name2-input']"));
        clickButton("Save");
}

    protected void checkFieldsPresent(String name, String... items)
    {
        goToManageDatasets();

        waitForElement(Locator.xpath("//a[text()='" + name + "']"));
        click(Locator.xpath("//a[text()='" + name + "']"));
        waitForText("Edit Definition");
        clickButton("Edit Definition");

        for(String item : items)
        {
            waitForText(item);
        }
    }

    @LogMethod(category = LogMethod.MethodType.VERIFICATION)
    private void verifySideFilter()
    {
        clickProject(getProjectName());
        clickFolder(getFolderName());
        clickAndWait(Locator.linkWithText("DEM-1: Demographics"));
        DataRegionTable dataregion = new DataRegionTable("Dataset", this);
        verifyFilterPanelOnDemographics(dataregion);

        _studyHelper.deleteCustomParticipantGroup(EXTRA_GROUP, "Mouse");

        clickProject(getProjectName());
        clickFolder(getFolderName());
        PortalHelper portalHelper = new PortalHelper(this);
        portalHelper.addQueryWebPart("Demographics", "study", "DEM-1 (DEM-1: Demographics)", null);
        dataregion = new DataRegionTable("qwp6", this);
        verifyFilterPanelOnDemographics(dataregion);
    }

    private void verifyFilterPanelOnDemographics(DataRegionTable dataset)
    {
        dataset.openSideFilterPanel();

        waitForElement(Locator.paginationText(24));
        dataset.clickFacetLabel(CATEGORY1, GROUP1A); // Select only GROUP1A
        waitForElementToDisappear(Locator.css(".labkey-pagination"), WAIT_FOR_JAVASCRIPT);
        waitForElement(Locator.linkWithText(PTIDS[0]));
        assertElementPresent(Locator.linkWithText(PTIDS[1]));
        Assert.assertEquals("Wrong number of rows after filter", 2, dataset.getDataRowCount());

        dataset.clickFacetCheckbox(CATEGORY1, GROUP1B); // GROUP1A OR GROU1B
        waitForElement(Locator.linkWithText(PTIDS[2]));
        assertElementPresent(Locator.linkWithText(PTIDS[0]));
        assertElementPresent(Locator.linkWithText(PTIDS[1]));
        assertElementPresent(Locator.linkWithText(PTIDS[3]));
        Assert.assertEquals("Wrong number of rows after filter", 4, dataset.getDataRowCount());

        dataset.clickFacetLabel(CATEGORY2, GROUP2A); // (GROUP1A OR GROU1B) AND GROUP2A
        waitForElementToDisappear(Locator.linkWithText(PTIDS[2]));
        waitForElement(Locator.linkWithText(PTIDS[1]));
        assertElementPresent(Locator.linkWithText(PTIDS[3]));
        Assert.assertEquals("Wrong number of rows after filter", 2, dataset.getDataRowCount());

        dataset.clickFacetLabel(CATEGORY2, "Not in any group"); // (GROUP1A OR GROUP1B) AND (CATEGORY2 = NULL)
        waitForElementToDisappear(Locator.linkWithText(PTIDS[1]));
        waitForElement(Locator.linkWithText(PTIDS[0]));
        Assert.assertEquals("Wrong number of rows after filter", 1, dataset.getDataRowCount());

        dataset.clickFacetCheckbox(CATEGORY2); // (GROUP1A OR GROUP1B)
        waitForElement(Locator.linkWithText(PTIDS[2]));
        assertElementPresent(Locator.linkWithText(PTIDS[0]));
        assertElementPresent(Locator.linkWithText(PTIDS[1]));
        assertElementPresent(Locator.linkWithText(PTIDS[3]));
        Assert.assertEquals("Wrong number of rows after filter", 4, dataset.getDataRowCount());

        dataset.clickFacetCheckbox("Cohorts", "Group 1"); // (GROUP1A OR GROUP1B) AND (NOT(COHORT 1))
        waitForElementToDisappear(Locator.linkWithText(PTIDS[0]));
        waitForElement(Locator.linkWithText(PTIDS[1]));
        assertElementPresent(Locator.linkWithText(PTIDS[2]));
        Assert.assertEquals("Wrong number of rows after filter", 2, dataset.getDataRowCount());

        dataset.toggleAllFacetsCheckbox();
        waitForElement(Locator.linkWithText(PTIDS[5]));
        Assert.assertEquals("Wrong number of rows after filter", 24, dataset.getDataRowCount());

        _extHelper.clickMenuButton(false, "Mouse Groups", "Create Mouse Group", "From All Mice");
        _extHelper.waitForExtDialog("Define Mouse Group");
        setFormElement(Locator.id("groupLabel-inputEl"), EXTRA_GROUP);
        _extHelper.clickExtButton("Define Mouse Group", "Save", 0);
        waitForElement(DataRegionTable.Locators.facetRow(EXTRA_GROUP, EXTRA_GROUP));
    }

    // in 13.2 Sprint 1 we changed reports and views so that they are associated with query name instead of label (i.e. dataset name instead of label)
    // there is also a migration step that happens when importing study archives with version < 13.11 to fixup these report/view references
    // this method verifies that migration on import for a handful of reports and views
    private void verifyReportAndViewDatasetReferences()
    {
        clickProject(getProjectName());
        clickFolder(getFolderName());

        // verify the reports and views dataset label/name references after study import
        verifyExpectedReportsAndViewsExist();
        verifyCustomViewWithDatasetJoins("CPS-1: Screening Chemistry Panel", CUSTOM_VIEW_WITH_DATASET_JOINS, true, true, "DataSets/DEM-1/DEMbdt", "DataSets/DEM-1/DEMsex");
        verifyTimeChart("APX-1", "APX-1: Abbreviated Physical Exam");
        verifyScatterPlot("APX-1: Abbreviated Physical Exam");
        verifyParticipantReport("DEM-1: 1.Date of Birth", "DEM-1: 2.What is your sex?", "APX-1: 1. Weight", "APX-1: 2. Body Temp", "ECI-1: 1.Meet eligible criteria?");

        // create a private custom view with dataset joins
        createPrivateCustomView();
        verifyCustomViewWithDatasetJoins("CPS-1: Screening Chemistry Panel", CUSTOM_VIEW_PRIVATE, false, false, "DataSets/DEM-1/DEMbdt", "DataSets/DEM-1/DEMsex");

        // rename and relabel the datasets related to these reports and views
        renameDataset("DEM-1", "demo", "DEM-1: Demographics", "Demographics");
        renameDataset("APX-1", "abbrphy", "APX-1: Abbreviated Physical Exam", "Abbreviated Physical Exam");
        renameDataset("ECI-1", "eligcrit", "ECI-1: Eligibility Criteria", "Eligibility Criteria");
        renameDataset("CPS-1", "scrchem", "CPS-1: Screening Chemistry Panel", "Screening Chemistry Panel");

        // verify the reports and views dataset label/name references after dataset rename and relabel
        verifyExpectedReportsAndViewsExist();
        verifyCustomViewWithDatasetJoins("Screening Chemistry Panel", CUSTOM_VIEW_WITH_DATASET_JOINS, true, true, "DataSets/eligcrit/ECIelig", "DataSets/demo/DEMbdt", "DataSets/demo/DEMsex");
        verifyCustomViewWithDatasetJoins("Screening Chemistry Panel", CUSTOM_VIEW_PRIVATE, false, false, "DataSets/eligcrit/ECIelig", "DataSets/demo/DEMbdt", "DataSets/demo/DEMsex");
        verifyTimeChart("abbrphy", "Abbreviated Physical Exam");
        verifyScatterPlot("Abbreviated Physical Exam");
        verifyParticipantReport("demo: 1.Date of Birth", "demo: 2.What is your sex?", "abbrphy: 1. Weight", "abbrphy: 2. Body Temp", "eligcrit: 1.Meet eligible criteria?");
    }

    private void verifyExpectedReportsAndViewsExist()
    {
        if (EXPECTED_REPORTS.size() == 0)
        {
            EXPECTED_REPORTS.put("Chart View: Systolic vs Diastolic", "APX-1: Abbreviated Physical Exam");
            EXPECTED_REPORTS.put("Crosstab: MouseId Counts", "APX-1: Abbreviated Physical Exam");
            EXPECTED_REPORTS.put("R Report: Dataset Column Names", "APX-1: Abbreviated Physical Exam");
            EXPECTED_REPORTS.put(SCATTER_PLOT_REPORT_NAME, "APX-1: Abbreviated Physical Exam");
            EXPECTED_REPORTS.put(TIME_CHART_REPORT_NAME, "APX-1: Abbreviated Physical Exam");
            EXPECTED_REPORTS.put(PTID_REPORT_NAME, "Stand-alone views");
        }

        if (EXPECTED_CUSTOM_VIEWS.size() == 0)
        {
            EXPECTED_CUSTOM_VIEWS.put(CUSTOM_VIEW_WITH_DATASET_JOINS, "CPS-1: Screening Chemistry Panel");
            EXPECTED_CUSTOM_VIEWS.put("Abbreviated Demographics", "DEM-1: Demographics");
        }

        goToManageViews();

        log("Verify that all reports were imported");
        for (Map.Entry<String, String> entry : EXPECTED_REPORTS.entrySet())
        {
            expandManageViewsRow(entry.getKey(), entry.getValue());
        }
        log("Verify that all custom views were imported");
        for (Map.Entry<String, String> entry : EXPECTED_CUSTOM_VIEWS.entrySet())
        {
            expandManageViewsRow(entry.getKey(), entry.getValue());
        }
    }

    private void createPrivateCustomView()
    {
        log("Create private custom view");
        clickFolder(getFolderName());
        clickAndWait(Locator.linkWithText("CPS-1: Screening Chemistry Panel"));
        _customizeViewsHelper.openCustomizeViewPanel();
        _customizeViewsHelper.addCustomizeViewColumn("DataSets/ECI-1/ECIelig");
        _customizeViewsHelper.addCustomizeViewColumn("DataSets/DEM-1/DEMbdt");
        _customizeViewsHelper.addCustomizeViewColumn("DataSets/DEM-1/DEMsex");
        _customizeViewsHelper.saveCustomView(CUSTOM_VIEW_PRIVATE, false);

        EXPECTED_CUSTOM_VIEWS.put(CUSTOM_VIEW_PRIVATE, "CPS-1: Screening Chemistry Panel");
    }

    private void verifyCustomViewWithDatasetJoins(String datasetLabel, String viewName, boolean checkSort, boolean checkAggregates, String... colFieldKeys)
    {
        log("Verify dataset label to name fixup for custom view import");
        goToManageViews();
        expandManageViewsRow(viewName, datasetLabel);
        clickAndWait(Locator.linkWithText("view"));
        waitForElement(Locator.tagWithText("span", viewName));

        if (checkSort)
        {
            assertTextPresentInThisOrder("Male", "Female"); // verify joined fields in sort
        }
        if (checkAggregates)
        {
            DataRegionTable drt = new DataRegionTable("Dataset", this); // verify joined fields in filter
            Assert.assertEquals("Unexpected number of rows, filter was not applied correctly", 4, drt.getDataRowCount()); // 3 data rows + aggregates
            assertTextPresentInThisOrder("Avg Cre:", "Agg Count:"); // verify joined fields in aggregates
        }
        for (String colFieldKey : colFieldKeys) // verify joined fields in column select
        {
            assertElementPresent(Locator.xpath("//td[@title = '" + colFieldKey + "']"));
        }
        _customizeViewsHelper.openCustomizeViewPanel();
        assertTextNotPresent("not found", "Field not found");
    }

    private void verifyTimeChart(String datasetName, String datasetLabel)
    {
        log("Verfiy dataset label to name fixup for Time Chart");
        goToManageViews();
        expandManageViewsRow(TIME_CHART_REPORT_NAME, datasetLabel);
        clickAndWait(Locator.linkWithText("edit"));
        waitForText("APX Main Title");
        assertTextNotPresent("Error: Could not find query"); // error message from 13.1 when dataset label was changed
        waitAndClick(Locator.css("svg a>path")); // click first data point
        _extHelper.waitForExtDialog("Data Point Information");
        assertTextPresent("Query Name:" + datasetName);
        assertTextNotPresent("Query Name:" + datasetLabel);
        clickButton("OK", 0);
        waitAndClick(Locator.css("svg text:contains('APX Main Title')"));
        waitAndClick(Locator.xpath("//span[contains(@class, 'iconReload')]"));
        Assert.assertEquals(datasetLabel, getFormElement(Locator.name("chart-title-textfield")));
        clickButton("Cancel", 0);
    }

    private void verifyScatterPlot(String datasetLabel)
    {
        log("Verify dataset label to name fixup for Scatter Plot");
        goToManageViews();
        expandManageViewsRow(SCATTER_PLOT_REPORT_NAME, datasetLabel);
        clickAndWait(Locator.linkWithText("edit"));
        _ext4Helper.waitForMaskToDisappear();
        assertTextNotPresent("An unexpected error occurred while retrieving data", "doesn't exist", "may have been deleted");
        // verify that the main title reset goes back to the dataset label - measue name
        waitAndClick(Locator.css("svg text:contains('APX Main Title')"));
        setFormElement(Locator.name("chart-title-textfield"), "test");
        waitForElementToDisappear(Locator.xpath("//div[contains(@class, 'x4-btn-disabled')]//span[contains(@class, 'iconReload')]"));
        click(Locator.xpath("//span[contains(@class, 'iconReload')]"));
        Assert.assertEquals(datasetLabel + " - 3. BP systolic xxx/", getFormElement(Locator.name("chart-title-textfield")));
        clickButton("Cancel", 0);
    }

    private void verifyParticipantReport(String... measureKeys)
    {
        log("Verify dataset label to name fixup for Participant Report");
        goToManageViews();
        expandManageViewsRow(PTID_REPORT_NAME, "Stand-alone views");
        clickAndWait(Locator.linkWithText("view"));
        waitForText("999320016");
        assertTextPresent("Showing 10 Results");
        for (String measureKey : measureKeys)
        {
            // element will be 'td' for demographic measures and 'th' for others
            if (!isElementPresent(Locator.xpath("//td[contains(@data-qtip, '" + measureKey + "')]")) && !isElementPresent(Locator.xpath("//th[contains(@data-qtip, '" + measureKey + "')]")))
            {
                Assert.fail("Unable to find measure with key: " + measureKey);
            }
        }
    }

    private void expandManageViewsRow(String name, String category)
    {
        String categoryXpath = "//div[contains(@class, 'x-grid-group-title') and text() = '" + category + "']/../../";
        waitForElement(Locator.xpath(categoryXpath + "/div[text() = '" + name + "']"));
        click(Locator.tagWithText("div", name));
    }

}
