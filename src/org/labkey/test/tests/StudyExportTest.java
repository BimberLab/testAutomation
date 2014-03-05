/*
 * Copyright (c) 2007-2014 LabKey Corporation
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
import org.labkey.test.categories.DailyB;
import org.labkey.test.categories.FileBrowser;
import org.labkey.test.categories.Study;
import org.labkey.test.util.Ext4HelperWD;

import java.io.File;

import static org.junit.Assert.*;

/**
 * User: brittp
 * Date: Dec 8, 2006
 * Time: 4:30:24 PM
 */
@Category({DailyB.class, Study.class, FileBrowser.class})
public class StudyExportTest extends StudyManualTest
{
    private static final String SPECIMEN_ARCHIVE_B = "/sampledata/study/specimens/sample_b.specimens";
    private static final String DEMOGRAPHICS_DATASET = "DEM-1: Demographics";
    private static final String TEST_ADD_ENTRY = "999000000";

    private final String DATASET_DATA_FILE = getLabKeyRoot() + "/sampledata/dataLoading/excel/dataset_data.xls";
    private static final String HIDDEN_DATASET = "URS-1: Screening Urinalysis";
    private static final String MODIFIED_DATASET = "Quality Control Report"; // Empty dataset.
    private static final String REORDERED_DATASET1 = "LLS-1: Screening Local Lab Results (Page 1)";
    private static final String REORDERED_DATASET2 = "LLS-2: Screening Local Lab Results (Page 2)";
    private static final String CATEGORY = "Test Category";
    private static final String DATE_FORMAT = "dd/mm hh:mma";
    private static final String NUMBER_FORMAT = "00.00";
    private static final String MODIFIED_PARTICIPANT = "999321033";
    protected static final String GROUP_2 = "Group 2"; // protected so that CohortStudyExportTest can use it
    private static final String COLUMN_DESC = "Test Column Description";
    private static final String MODIFIED_VISIT = "Cycle 2";

    @Override
    protected void doCreateSteps()
    {
        // manually create a study and load a specimen archive
        log("Creating study manually");
        createStudyManually();

        // import the specimens and wait for both datasets & specimens to load
        SpecimenImporter specimenImporter = new SpecimenImporter(new File(getPipelinePath()), new File(getLabKeyRoot(), SPECIMEN_ARCHIVE_A), new File(getLabKeyRoot(), ARCHIVE_TEMP_DIR), getFolderName(), 2);
        specimenImporter.importAndWaitForComplete();

        // export manually created study to individual files using "legacy" formats
        exportStudy(false, false);

        // delete manually created study
        clickFolder(getFolderName());
        deleteStudy();

        log("Importing exported study (legacy formats)");
        clickButton("Import Study");
        clickButton("Import Study Using Pipeline");
        _fileBrowserHelper.importFile("export/study/study.xml", "Import Study");
        // wait for study & specimen load to complete
        waitForPipelineJobsToComplete(3, "study and specimen import (legacy formats)", false);

        // delete "export" directory
        deleteDir(new File(getPipelinePath() + "export"));

        // change settings that aren't roundtripped using "legacy" formats
        setDemographicsDescription();
        createCustomAssays();
        setFormatStrings();
        doCohortCreateSteps();
        modifyVisits();
        importCustomVisitMapping();
        changeDatasetOrder("16");
        setDatasetCategory(MODIFIED_DATASET, CATEGORY);
        hideDataset(HIDDEN_DATASET);
        modifyDatasetColumn(MODIFIED_DATASET);

        _listHelper.importListArchive(getFolderName(), new File(getLabKeyRoot(), "/sampledata/rlabkey/listArchive.zip"));

        // export new study to zip file using "xml" formats
        exportStudy(true, true);

        // delete the study
        clickFolder(getFolderName());
        deleteStudy();

        log("Importing exported study (xml formats)");
        clickButton("Import Study");
        clickButton("Import Study Using Pipeline");
        _fileBrowserHelper.selectFileBrowserItem("export/");
        // select the first exported zip archive file by row
        Locator.XPathLocator gridRow = Locator.tag("tr").withClass("x4-grid-data-row").withAttributeContaining("data-recordid", "My Study_");
        waitForElement(gridRow);
        click(gridRow);
        _fileBrowserHelper.selectImportDataAction("Import Study");

        // wait for study & specimen load
        waitForPipelineJobsToComplete(4, "study and specimen import (xml formats)", false);

        // TODO: Move this earlier (after legacy format import) once issue 10074 is resolved. 
        setDemographicsBit();
    }

    protected void doCohortCreateSteps()
    {
        setManualCohorts();
    }

    @Override
    protected void waitForSpecimenImport()
    {
        // specimen import is already complete
    }

    @Override
    protected void verifyStudyAndDatasets()
    {
        super.verifyStudyAndDatasets();

        clickFolder(getFolderName());

        // verify format strings
        goToFolderManagement();
        clickAndWait(Locator.linkWithText("Formats"));
        assertFormElementEquals(Locator.name("defaultDateFormat"), DATE_FORMAT);
        assertFormElementEquals(Locator.name("defaultNumberFormat"), NUMBER_FORMAT);

        clickFolder(getFolderName());

        // verify reordered, categorized, & hidden datasets.
        clickAndWait(Locator.linkWithText("47 datasets"));
        assertTextBefore(REORDERED_DATASET2, REORDERED_DATASET1);
        assertLinkNotPresentWithText(HIDDEN_DATASET);
        assertTextBefore(CATEGORY, MODIFIED_DATASET);

        // verify dataset category on dataset management page
        clickAndWait(Locator.linkWithText("Manage Datasets"));
        assertTextPresent(CATEGORY, 1);
        assertElementContains(Locator.xpath("//tr[./td/a[text() = '" + MODIFIED_DATASET + "']]/td[4]"), CATEGORY);

        // verify dataset columns
        clickAndWait(Locator.linkWithText(MODIFIED_DATASET));
        assertChecked(Locator.xpath("//tr[10]/td[6]/input"));
        assertElementContains(Locator.xpath("//tr[10]/td[7]"), COLUMN_DESC);
        assertTextPresent(CATEGORY);

        // TODO: verify lookup

        // verify manual cohorts
        clickFolder(getFolderName());
        clickTab("Manage");
        clickAndWait(Locator.linkWithText("Manage Cohorts"));
        assertRadioButtonSelected(Locator.radioButtonByNameAndValue("manualCohortAssignment", "true"));
        clickFolder(getFolderName());
        clickAndWait(Locator.linkWithText("47 datasets"));
        clickAndWait(Locator.linkWithText(DEMOGRAPHICS_DATASET));
        clickMenuButton("Mouse Groups", "Cohorts", GROUP_2);
        clickMenuButton("QC State", "All data");
        assertTextPresent(MODIFIED_PARTICIPANT);

        // verify visit display order
        clickFolder(getFolderName());
        clickTab("Manage");
        clickAndWait(Locator.linkWithText("Manage Visits"));
        assertTextBefore("Cycle 3", MODIFIED_VISIT);

        // verify visit modifications
        editVisit(MODIFIED_VISIT);
        assertFormElementEquals("dataSetStatus", "OPTIONAL");
        assertOptionEquals(Locator.name("cohortId"), GROUP_2);
    }

    @Override
    protected void verifySpecimens()
    {
        super.verifySpecimens();

        // configure specimen tracking
        clickFolder(getFolderName());
        clickTab("Manage");
        clickAndWait(Locator.linkWithText("Manage Request Statuses"));
        setFormElement("newLabel", "New Request");
        clickButton("Save");
        setFormElement("newLabel", "Pending Approval");
        clickButton("Save");
        setFormElement("newLabel", "Complete");
        clickButton("Done");
        clickAndWait(Locator.linkWithText("Manage Actors and Groups"));
        setFormElement("newLabel", "Institutional Review Board");
        selectOptionByText("newPerSite", "Multiple Per Study (Location Affiliated)");
        clickButton("Save");
        setFormElement("newLabel", "Scientific Leadership Group");
        selectOptionByText("newPerSite", "One Per Study");
        clickButton("Save");
        clickAndWait(Locator.linkWithText("Update Members"));
        clickAndWait(Locator.linkWithText("FHCRC - Seattle (Endpoint Lab, Site Affiliated Lab, Clinic)"));
        assertTextPresent("Institutional Review Board, FHCRC - Seattle");
        assertTextPresent("This group currently has no members.");
        clickTab("Manage");
        clickAndWait(Locator.linkWithText("Manage Default Requirements"));
        selectOptionByText("providerActor", "Institutional Review Board");
        setFormElement("providerDescription", "To be deleted");
        clickButtonByIndex("Add Requirement", 1);
        assertTextPresent("To be deleted");
        clickAndWait(Locator.linkWithText("Delete"));
        assertTextNotPresent("To be deleted");
        selectOptionByText("providerActor", "Institutional Review Board");
        setFormElement("providerDescription", "Providing lab approval");
        clickButtonByIndex("Add Requirement", 1);
        selectOptionByText("receiverActor", "Institutional Review Board");
        setFormElement("receiverDescription", "Receiving lab approval");
        clickButtonByIndex("Add Requirement", 2);
        selectOptionByText("generalActor", "Scientific Leadership Group");
        setFormElement("generalDescription", "SLG Request Approval");
        clickButtonByIndex("Add Requirement", 3);
        clickTab("Manage");

        // create specimen request
        clickFolder(getFolderName());
        clickAndWait(Locator.linkWithText("Study Navigator"));

        assertLinkNotPresentWithText("24");
        selectOptionByText("QCState", "All data");

        waitAndClickAndWait(Locator.linkWithText("24"));
        checkCheckbox(Locator.checkboxByName(".toggle"));
        clickButton("View Specimens");
        assertLinkPresentWithText("999320016");
        assertLinkPresentWithText("999320518");
        clickAndWait(Locator.linkWithText("Show individual vials"));
        assertLinkPresentWithText("999320016");
        checkCheckbox(Locator.checkboxByName(".toggle"));
        clickMenuButton("Request Options", "Create New Request");
        assertTextPresent("HAQ0003Y-09");
        assertTextPresent("BAQ00051-09");
        assertTextNotPresent("KAQ0003Q-01");
        selectOptionByText("destinationLocation", "Duke University (Repository, Site Affiliated Lab, Clinic)");
        setFormElements("textarea", "inputs", new String[] { "An Assay Plan", "Duke University, NC", "My comments" });
        clickButton("Create and View Details");

        assertTextPresent("This request has not been submitted");
        assertNavButtonPresent("Cancel Request");
        assertNavButtonPresent("Submit Request");
        clickAndWait(Locator.linkWithText("Specimen Requests"));

        assertNavButtonPresent("Submit");
        assertNavButtonPresent("Cancel");
        assertNavButtonPresent("Details");
        assertTextPresent("Not Yet Submitted");
        clickButton("Submit", 0);
        getAlert(); // TODO: add check for expected alert text
        waitAndClickAndWait(Locator.linkWithText("Specimen Requests"));
        assertNavButtonNotPresent("Submit");
        assertNavButtonPresent("Details");
        assertTextPresent("New Request");

        // test auto-fill:
        clickButton("Create New Request");
        assertNotEquals(getFormElement(Locator.id("input1")), "Duke University, NC");
        selectOptionByText("destinationLocation", "Duke University (Repository, Site Affiliated Lab, Clinic)");
        assertEquals(getFormElement(Locator.id("input1")), "Duke University, NC");
        clickButton("Cancel");

        // manage new request
        clickButton("Details");
        assertTextNotPresent("Complete");
        assertTextNotPresent("WARNING: Missing Specimens");
        assertTextPresent("New Request");
        assertTextNotPresent("Pending Approval");
        clickAndWait(Locator.linkWithText("Update Request"));
        selectOptionByText("status", "Pending Approval");
        setFormElement("comments", "Request is now pending.");
        clickButton("Save Changes and Send Notifications");
        assertTextNotPresent("New Request");
        assertTextPresent("Pending Approval");
        clickAndWait(Locator.linkWithText("Details", 0));
        assertTextPresent("Duke University");
        assertTextPresent("Providing lab approval");
        checkCheckbox("complete");
        setFormElement("comment", "Approval granted.");
        if (isFileUploadAvailable())
            setFormElement("formFiles[0]", new File(getLabKeyRoot() + VISIT_MAP).getPath());
        else
            log("File upload skipped.");
        clickButton("Save Changes and Send Notifications");
        assertTextPresent("Complete");

        clickAndWait(Locator.linkWithText("Details", 1));
        clickButton("Delete Requirement");
        assertTextNotPresent("Receiving lab approval");

        clickAndWait(Locator.linkWithText("Originating Location Specimen Lists"));
        assertTextPresent("WARNING: The requirements for this request are incomplete");
        assertTextPresent("KCMC, Moshi, Tanzania");
        clickButton("Cancel");

        clickAndWait(Locator.linkWithText("View History"));
        assertTextPresent("Request is now pending.");
        assertTextPresent("Approval granted.");
        assertTextPresent("Institutional Review Board (Duke University), Receiving lab approval");
        if (isFileUploadAvailable())
            assertTextPresent(VISIT_MAP.substring(VISIT_MAP.lastIndexOf("/") + 1));

        clickProject(getProjectName());
        clickFolder(getFolderName());
        enterStudySecurity();

        // enable advanced study security
        selectOptionByValue(Locator.name("securityString"), "ADVANCED_READ");
        waitForElements(Locator.tagWithName("table", "webpart"), 3);

        click(Locator.xpath("//td[.='Users']/..//input[@value='READ']"));
        clickAndWait(Locator.id("groupUpdateButton"));

        // set the QC state 
        clickFolder(getFolderName());
        clickAndWait(Locator.linkWithText("47 datasets"));
        clickAndWait(Locator.linkWithText(DEMOGRAPHICS_DATASET));
        clickMenuButton("QC State", "All data");
        checkAllOnPage("Dataset");
        clickMenuButton("QC State", "Update state of selected rows");
        selectOptionByText("newState", "clean");
        setFormElement("comments", "This data is clean.");
        clickButton("Update Status");
        clickMenuButton("QC State", "clean");

        // test specimen comments
        clickFolder(getFolderName());
        waitAndClick(Locator.linkWithText("Vials by Derivative Type"));
        waitForText("Plasma, Unknown Processing");
        clickAndWait(Locator.linkWithText("Plasma, Unknown Processing"));
        clickButton("Enable Comments/QC");
        checkAllOnPage("SpecimenDetail");
        clickMenuButton("Comments and QC", "Set Vial Comment or QC State for Selected");
        setFormElement("comments", "These vials are very important.");
        clickButton("Save Changes");
        assertTextPresent("These vials are very important.", 25);
        setFilter("SpecimenDetail", "MouseId", "Equals", "999320824");
        checkAllOnPage("SpecimenDetail");
        clickMenuButtonAndContinue("Comments and QC", "Clear Vial Comments for Selected");
        getAlert(); // TODO: add check for expected alert text
        assertTextNotPresent("These vials are very important.");
        clearFilter("SpecimenDetail", "MouseId");
        assertTextPresent("These vials are very important.", 23);
        clickMenuButton("Comments and QC", "Exit Comments and QC mode");

        // import second archive, verify that that data is merged:
        SpecimenImporter importer = new SpecimenImporter(new File(getPipelinePath()), new File(getLabKeyRoot(), SPECIMEN_ARCHIVE_B), new File(getLabKeyRoot(), ARCHIVE_TEMP_DIR), getFolderName(), 5);
        importer.importAndWaitForComplete();

        // verify that comments remain after second specimen load
        clickFolder(getFolderName());
        waitAndClick(Locator.linkWithText("Vials by Derivative Type"));
        waitForText("Plasma, Unknown Processing");
        clickAndWait(Locator.linkWithText("Plasma, Unknown Processing"));
        assertTextPresent("These vials are very important.", 2);

        // check to see that data in the specimen archive was merged correctly:
        clickFolder(getFolderName());
        clickAndWait(Locator.linkWithText("By Individual Vial"));
        clickMenuButton("Page Size", "Show All");
        assertTextPresent("DRT000XX-01");
        clickAndWait(Locator.linkWithText("Search"));
        waitForTextToDisappear("Loading");
        waitForText("Additive Type");
        _ext4Helper.selectRadioButton("Search Type:", "Grouped Vials");

//        WARNING: Using getFormElementNameByTableCaption() is dangerous... if muliple values are returned their
//        order is unpredictable, since they come back in keyset order.  The code below breaks under Java 6.
//
//        String[] globalUniqueIDCompareElems = getFormElementNameByTableCaption("Specimen Number", 0, 1);
//        String[] globalUniqueIDValueElems = getFormElementNameByTableCaption("Specimen Number", 0, 2);
//        String[] participantIDFormElems = getFormElementNameByTableCaption("Participant Id", 0, 1);
//        setFormElement(globalUniqueIDCompareElems[1], "CONTAINS");
//        setFormElement(globalUniqueIDValueElems[0], "1416");
//        setFormElement(participantIDFormElems[2], "999320528");

        _ext4Helper.selectComboBoxItem(Ext4HelperWD.Locators.formItemWithLabel("Mouse:"), true, "999320528");
        _ext4Helper.selectComboBoxItem(Ext4HelperWD.Locators.formItemWithLabel("Visit:"), true, "Enroll/Vacc #1 (201)");

        clickButton("Search");
        assertTextPresent("999320528");
        clickAndWait(Locator.linkWithText("Show individual vials"));
        // if our search worked, we'll only have six vials:
        assertTextPresent("[history]", 6);
        assertLinkPresentWithTextCount("999320528", 6);
        assertTextNotPresent("DRT000XX-01");
        clickAndWait(Locator.linkWithText("[history]"));
        assertTextPresent("GAA082NH-01");
        assertTextPresent("BAD");
        //assertTextPresent("1.0&nbsp;ML");
        assertTextPresent("Added Comments");
        assertTextPresent("Johannesburg, South Africa");

        clickFolder(getFolderName());
        click(Locator.linkWithText("Specimen Requests"));
        clickAndWait(Locator.linkWithText("View Current Requests"));
        clickButton("Details");
        assertTextPresent("WARNING: Missing Specimens");
        clickButton("Delete missing specimens", 0);
        getAlert(); // TODO: add check for expected alert text
        assertTextNotPresent("WARNING: Missing Specimens");
        assertTextPresent("Duke University");
        assertTextPresent("An Assay Plan");
        assertTextPresent("Providing lab approval");
        assertTextPresent("HAQ0003Y-09");
        assertTextPresent("BAQ00051-09");
        assertTextNotPresent("BAQ00051-10");
        assertTextPresent("BAQ00051-11");

        log("Test editing rows in a dataset");
        clickProject(getProjectName());
        clickFolder(getFolderName());

        enterStudySecurity();

        prepForPageLoad();
        selectOptionByValue(Locator.name("securityString"), "BASIC_WRITE");
        newWaitForPageToLoad();

        clickFolder(getFolderName());
        clickAndWait(Locator.linkWithText("47 datasets"));
        clickAndWait(Locator.linkWithText("DEM-1: Demographics"));

        clickAndWait(Locator.linkWithText("edit"));
        setFormElement("quf_DEMbdt", "2001-11-11");
        clickButton("Submit");
        clickMenuButton("QC State", "unknown QC");
        assertTextPresent("2001-11-11");

        log("Test adding a row to a dataset");
        clickButton("Insert New");
        clickButton("Submit");
        assertTextPresent("This field is required");
        setFormElement("quf_MouseId", TEST_ADD_ENTRY);
        setFormElement("quf_SequenceNum", "123");
        clickButton("Submit");
        clickMenuButton("QC State", "All data");
        assertTextPresent(TEST_ADD_ENTRY);

        // Make sure that we can view its participant page immediately
        pushLocation();
        clickAndWait(Locator.linkWithText(TEST_ADD_ENTRY));
        assertTextPresent("Mouse - " + TEST_ADD_ENTRY);
        assertTextPresent("DEM-1: Demographics");
        popLocation();

        log("Test deleting rows in a dataset");
        checkCheckbox(Locator.xpath("//input[contains(@value, '999320529')]"));
        clickButton("Delete", 0);
        getAlert(); // TODO: add check for expected alert text
        assertTextNotPresent("999320529");

        // configure QC state management to show all data by default so the next steps don't have to keep changing the state:
        clickFolder(getFolderName());
        clickTab("Manage");
        clickAndWait(Locator.linkWithText("Manage Dataset QC States"));
        selectOptionByText("showPrivateDataByDefault", "All data");
        clickButton("Save");

        // Test creating and importing a dataset from an excel file
        doTestDatasetImport();
    }

    protected boolean comparePaths(String path1, String path2)
    {
        String[] parseWith = { "/", "\\\\" };
        for (String parser1 : parseWith)
        {
            String[] path1Split = path1.split(parser1);
            for  (String parser2 : parseWith)
            {
                String[] path2Split = path2.split(parser2);
                if (path1Split.length == path2Split.length)
                {
                    int index = 0;
                    while (path1Split[index].compareTo(path2Split[index]) == 0)
                    {
                        index++;
                        if (index > path2Split.length - 1)
                            return true;
                    }
                }
            }
        }
        return false;
    }

    private void changeDatasetOrder(String value)
    {
        clickFolder(getFolderName());
        clickTab("Manage");
        clickAndWait(Locator.linkWithText("Manage Datasets"));
        clickAndWait(Locator.linkWithText("Change Display Order"));
        selectOptionByValue("items", value);
        clickButton("Move Down", 0);
        clickButton("Save");
    }

    protected void hideDataset(String dataset)
    {
        clickFolder(getFolderName());
        setVisibleBit(dataset, false);
    }

    protected void setDatasetCategory(String dataset, String category)
    {
        clickFolder(getFolderName());
        clickTab("Manage");
        clickAndWait(Locator.linkWithText("Manage Datasets"));
        clickAndWait(Locator.linkWithText(dataset));
        clickButton("Edit Definition");
        waitForElement(Locator.name("dsCategory"), WAIT_FOR_PAGE);
        setFormElement("dsCategory", category);
        clickButton("Save");
    }

    private void modifyVisits()
    {
        hideSceeningVisit();
        clickAndWait(Locator.linkWithText("Change Visit Order"));
        checkCheckbox("explicitDisplayOrder");
        selectOptionByText("displayOrderItems", MODIFIED_VISIT);
        clickButton("Move Down", 0);
        clickButton("Save");
        editVisit(MODIFIED_VISIT);
        selectOption("dataSetStatus", 0, "OPTIONAL");
        selectOptionByText("cohortId", GROUP_2);
        clickButton("Save");
        
    }

    private void modifyDatasetColumn(String dataset)
    {
        clickFolder(getFolderName());
        clickTab("Manage");
        clickAndWait(Locator.linkWithText("Manage Datasets"));
        clickAndWait(Locator.linkWithText(dataset));
        clickButton("Edit Definition");
        waitForElement(Locator.name("ff_name0"), WAIT_FOR_PAGE);
        click(Locator.name("ff_name0"));
        click(Locator.xpath("//span[contains(@class,'x-tab-strip-text') and text()='Advanced']"));
        waitForElement(Locator.name("mvEnabled"), WAIT_FOR_JAVASCRIPT);
        checkCheckbox("mvEnabled");
        click(Locator.xpath("//span[contains(@class,'x-tab-strip-text') and text()='Display']"));
        setFormElement(Locator.id("propertyDescription"), COLUMN_DESC);
        // TODO: add lookups for current & other folders
        clickButton("Save");
    }

    private void setFormatStrings()
    {
        clickFolder(getFolderName());
        goToFolderManagement();
        clickAndWait(Locator.linkWithText("Formats"));
        setFormElement(Locator.name("defaultDateFormat"), DATE_FORMAT);
        setFormElement(Locator.name("defaultNumberFormat"), NUMBER_FORMAT);
        clickButton("Save");
    }

    private void setManualCohorts()
    {
        clickFolder(getFolderName());
        clickTab("Manage");
        clickAndWait(Locator.linkWithText("Manage Cohorts"));
        clickRadioButtonById("manualCohortAssignmentEnabled");
        waitForText("Mouse-Cohort Assignments");
        setParticipantCohort(MODIFIED_PARTICIPANT, GROUP_2);
        clickButton("Save");
    }

    private void setParticipantCohort(String ptid, String cohort)
    {
        selectOptionByText(Locator.xpath("//tr[./td = '" + ptid + "']//select"), cohort);
    }

    protected void editVisit(String visit)
    {
        clickAndWait(Locator.xpath("//table[@id='visits']//tr[./th[text() = '" + visit + "']]/td/a[text() = 'edit']"));
    }

    protected void doTestDatasetImport()
    {
        if (!isFileUploadAvailable())
            return;

        clickProject(getProjectName());
        clickFolder(getFolderName());
        clickTab("Manage");
        clickAndWait(Locator.linkWithText("Manage Datasets"));
        clickAndWait(Locator.linkWithText("Create New Dataset"));
        setFormElement("typeName", "fileImportDataset");
        clickCheckbox("fileImport");
        clickButton("Next");

        waitForElement(Locator.xpath("//input[@name='uploadFormElement']"), WAIT_FOR_JAVASCRIPT);

        File datasetFile = new File(DATASET_DATA_FILE);
        setFormElement("uploadFormElement", datasetFile);

        waitForElement(Locator.xpath("//span[@id='button_Import']"), WAIT_FOR_JAVASCRIPT);

        Locator.XPathLocator mouseId = Locator.xpath("//label[contains(@class, 'x-form-item-label') and text() ='MouseId:']/../div/div");
        _extHelper.selectGWTComboBoxItem(mouseId, "name");
        Locator.XPathLocator sequenceNum = Locator.xpath("//label[contains(@class, 'x-form-item-label') and text() ='Sequence Num:']/../div/div");
        _extHelper.selectGWTComboBoxItem(sequenceNum, "visit number");

        waitAndClickButton("Import");
        waitForElement(Locator.paginationText(9));
        assertTextPresent("kevin");
        assertTextPresent("chimpanzee");
    }

    @Override
    protected void doCleanup(boolean afterTest) throws TestTimeoutException
    {
        super.doCleanup(afterTest);

        deleteDir(new File(getPipelinePath() + "export"));
    }
}
