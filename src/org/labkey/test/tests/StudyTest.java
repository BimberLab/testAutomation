/*
 * Copyright (c) 2009-2013 LabKey Corporation
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

import com.thoughtworks.selenium.SeleniumException;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.experimental.categories.Category;
import org.labkey.remoteapi.Connection;
import org.labkey.remoteapi.query.ContainerFilter;
import org.labkey.remoteapi.query.SelectRowsCommand;
import org.labkey.remoteapi.query.SelectRowsResponse;
import org.labkey.remoteapi.query.Sort;
import org.labkey.test.Locator;
import org.labkey.test.SortDirection;
import org.labkey.test.TestTimeoutException;
import org.labkey.test.categories.Specimen;
import org.labkey.test.categories.Study;
import org.labkey.test.util.ChartHelper;
import org.labkey.test.util.DataRegionTable;
import org.labkey.test.util.ListHelper;
import org.labkey.test.util.LogMethod;
import org.labkey.test.util.PasswordUtil;
import org.labkey.test.util.SearchHelper;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.labkey.test.util.PasswordUtil.getUsername;

/**
 * User: adam
 * Date: Apr 3, 2009
 * Time: 9:18:32 AM
 */
@Category({Study.class, Specimen.class})
public class StudyTest extends StudyBaseTest
{
    public String datasetLink = datasetCount + " datasets";
    protected boolean quickTest = true;
    protected static final String DEMOGRAPHICS_DESCRIPTION = "This is the demographics dataset, dammit. Here are some \u2018special symbols\u2019 - they help test that we're roundtripping in UTF-8.";
    protected static final String DEMOGRAPHICS_TITLE = "DEM-1: Demographics";

    protected String _tsv = "participantid\tsequencenum\tvisitdate\tSampleId\tDateField\tNumberField\tTextField\treplace\taliasedColumn\n" +
        "1234\t1\t1/1/2006\t1234_A\t2/1/2006\t1.2\ttext\t\taliasedData\n" +
        "1234\t1\t1/1/2006\t1234_B\t2/1/2006\t1.2\ttext\t\taliasedData\n";

    // specimen comment constants
    private static final String PARTICIPANT_CMT_DATASET = "Mouse Comments";
    private static final String PARTICIPANT_VISIT_CMT_DATASET = "Mouse Visit Comments";
    private static final String COMMENT_FIELD_NAME = "comment";
    private static final String PARTICIPANT_COMMENT_LABEL = "mouse comment";
    private static final String PARTICIPANT_VISIT_COMMENT_LABEL = "mouse visit comment";

    protected static final String VISIT_IMPORT_MAPPING = "Name\tSequenceNum\n" +
        "Cycle 10\t10\n" +
        "Vaccine 1\t201\n" +
        "Vaccination 1\t201\n" +
        "Soc Imp Log #%{S.3.2}\t5500\n" +
        "ConMeds Log #%{S.3.2}\t9002\n" +
        "All Done\t9999";

    public static final String APPEARS_AFTER_PICKER_LOAD = "Add Selected";


    //lists created in participant picker tests must be cleaned up afterwards
    LinkedList<String> persistingLists  = new LinkedList<>();
    private String Study001 = "Study 001";
    private String authorUser = "author@study.test";
    private String specimenUrl = null;

    protected void setDatasetLink(int datasetCount)
    {
        datasetLink =  datasetCount + " datasets";
    }

    protected boolean isManualTest = false;

    protected void triggerManualTest()
    {
        setDatasetLink(47);
        isManualTest = true;
    }

    protected File[] getTestFiles()
    {
        return new File[]{new File(getLabKeyRoot() + "/server/test/data/api/study-api.xml")};
    }

    protected void doCreateSteps()
    {
        pauseSearchCrawler(); //necessary for the alternate ID testing
        enableEmailRecorder();

        importStudy();
        startSpecimenImport(2);

        // wait for study (but not specimens) to finish loading
        waitForPipelineJobsToComplete(1, "study import", false);
    }

    protected void doCleanup(boolean afterTest) throws TestTimeoutException //child class cleanup method throws Exception
    {
        super.doCleanup(afterTest);
        deleteUsers(false, authorUser); // Subclasses may not have created this user
        unpauseSearchCrawler();
    }

    protected void emptyParticipantPickerList()
    {
        goToManageParticipantClassificationPage(PROJECT_NAME, STUDY_NAME, SUBJECT_NOUN);
        while(persistingLists.size()!=0)
        {
            deleteListTest(persistingLists.pop());
        }
    }

    protected void doVerifySteps()
    {
        doVerifyStepsSetDepth(false);
    }

    @LogMethod
    protected void doVerifyStepsSetDepth(boolean quickTest)
    {
        this.quickTest = quickTest;
        manageSubjectClassificationTest();
        emptyParticipantPickerList(); // Delete participant lists to avoid interfering with api test.
        verifyStudyAndDatasets();

        if (!quickTest)
        {
            waitForSpecimenImport();
            verifySpecimens();
            verifyParticipantComments();
            verifyParticipantReports();
            verifyPermissionsRestrictions();
        }
    }

    @LogMethod
    private void verifyPermissionsRestrictions()
    {
        clickProject(getProjectName());
        createUser(authorUser, null, true);
        setUserPermissions(authorUser, "Author");
        impersonate(authorUser);
        beginAt(specimenUrl);
        clickButton("Request Options", 0);
        assertElementNotPresent(Locator.tagWithText("span", "Create New Request"));
        stopImpersonating();
    }

    @LogMethod
    private void verifyParticipantReports()
    {
        clickFolder(getFolderName());
        addWebPart("Study Data Tools");
        clickAndWait(Locator.linkWithImage("/labkey/study/tools/participant_report.png"));
        clickButton("Choose Measures", 0);
        _extHelper.waitForExtDialog("Add Measure");
        _extHelper.waitForLoadingMaskToDisappear(WAIT_FOR_JAVASCRIPT);

        String textToFilter = "AE-1:(VTN) AE Log";
        waitForText(textToFilter);
        assertTextPresent(textToFilter, 27);
        assertTextPresent("Abbrevi", 79);

        log("filter participant results down");
        Locator filterSearchText = Locator.xpath("//input[@name='filterSearch']");
        setFormElement(filterSearchText, "a");
        setFormElement(filterSearchText, "abbrev");
        setFormElement(Locator.xpath("//input[@type='text']"), "abbrevi");
        fireEvent(filterSearchText, SeleniumEvent.change);
        sleep(1000);
        assertTextPresent("Abbrevi", 79);
        assertTextNotPresent(textToFilter);

        log("select some records and include them in a report");
        _extHelper.clickX4GridPanelCheckbox(4, "measuresGridPanel", true);
        _extHelper.clickX4GridPanelCheckbox(40, "measuresGridPanel", true);
        _extHelper.clickX4GridPanelCheckbox(20, "measuresGridPanel", true);
        _extHelper.clickExtButton("Select", 0);
        waitForExtMaskToDisappear();

        log("Verify report page looks as expected");
        String reportName = "MouseFooReport";
        String reportDescription = "Desc";
        _extHelper.setExtFormElementByLabel("Report Name", reportName);
        _extHelper.setExtFormElementByLabel("Report Description", reportDescription);
        clickButton("Save", 0);
        waitForText(reportName);
        assertTextPresent(reportName, 1);
        _ext4Helper.waitForComponentNotDirty("participant-report-panel-1");
    }

    protected static final String SUBJECT_NOUN = "Mouse";
    protected static final String SUBJECT_NOUN_PLURAL = "Mice";
    protected static final String PROJECT_NAME = "StudyVerifyProject";
    protected static final String STUDY_NAME = "My Study";
    protected static final String LABEL_FIELD = "groupLabel";
    protected static final String ID_FIELD = "participantIdentifiers";


    /**
     * This is a test of the participant picker/classification creation UI.
     */
    @LogMethod
    protected void manageSubjectClassificationTest()
    {

        if(!quickTest)
        {
            //verify/create the right data
            goToManageParticipantClassificationPage(PROJECT_NAME, STUDY_NAME, SUBJECT_NOUN);

            //issue 12487
            assertTextPresent("Manage " + SUBJECT_NOUN + " Groups");

            //nav trail check
            assertTextNotPresent("Manage Study > ");

            String allList = "all list12345";
            String filteredList = "Filtered list";

            cancelCreateClassificationList();

            String pIDs = createListWithAddAll(allList, false);
            persistingLists.add(allList);

            refresh();
            editClassificationList(allList, pIDs);

            //Issue 12485
            createListWithAddAll(filteredList, true);
            persistingLists.add(filteredList);

            String changedList = changeListName(filteredList);
            persistingLists.add(changedList);
            persistingLists.remove(filteredList);
            deleteListTest(allList);
            persistingLists.remove(allList);

            attemptCreateExpectError("1", "does not exist in this study.", "bad List ");
            String id = pIDs.substring(0, pIDs.indexOf(","));
            attemptCreateExpectError(id + ", " + id, "Duplicates are not allowed in a group", "Bad List 2");
        }

        // test creating a participant group directly from a data grid
        clickFolder(STUDY_NAME);
        waitForText(datasetLink);
        clickAndWait(Locator.linkWithText(datasetLink));
        clickAndWait(Locator.linkWithText("DEM-1: Demographics"));


        // verify warn on no selection
        if(!isQuickTest)
        {
            //nav trail check
            clickAndWait(Locator.linkContainingText("999320016"));
            assertTextPresent("Dataset: DEM-1: Demographics, All Visits");
            clickAndWait(Locator.linkContainingText("Dataset:"));

            _extHelper.clickMenuButton(false, SUBJECT_NOUN + " Groups", "Create " + SUBJECT_NOUN + " Group", "From Selected " + SUBJECT_NOUN_PLURAL);
            _extHelper.waitForExtDialog("Selection Error");
            assertTextPresent("At least one " + SUBJECT_NOUN + " must be selected");
            clickButtonContainingText("OK", 0);
            waitForExtMaskToDisappear();

        }

        DataRegionTable table = new DataRegionTable("Dataset", this, true, true);
        for (int i=0; i < 5; i++)
            table.checkCheckbox(i);

        // verify the selected list of identifiers is passed to the participant group wizard
        String[] selectedIDs = new String[]{"999320016","999320518","999320529","999320541","999320533"};
        _extHelper.clickMenuButton(false, SUBJECT_NOUN + " Groups", "Create " + SUBJECT_NOUN + " Group", "From Selected " + SUBJECT_NOUN_PLURAL);
        _extHelper.waitForExtDialog("Define " + SUBJECT_NOUN + " Group");
        verifySubjectIDsInWizard(selectedIDs);

        // save the new group and use it
        setFormElement(Locator.name(LABEL_FIELD), "Participant Group from Grid");
        clickButtonContainingText("Save", 0);
        waitForExtMaskToDisappear();

        if(!quickTest)
        {
            // the dataregion get's ajaxed into place, wait until the new group appears in the menu
            Locator menu = Locator.navButton(SUBJECT_NOUN + " Groups");
            waitForElement(menu, WAIT_FOR_JAVASCRIPT);
            Locator menuItem = Locator.menuItem("Participant Group from Grid");
            for (int i = 0; i < 10; i++)
            {
                try{
                    click(menu);
                }
                catch(SeleniumException e){
                    /* Ignore. This button is unpredictable. */
                }
                if (isElementPresent(menuItem))
                    break;
                else
                    sleep(1000);
            }
            clickAndWait(menuItem);
            for (String identifier : selectedIDs)
                assertTextPresent(identifier);
        }
    }

    private void verifySubjectIDsInWizard(String[] ids)
    {
        Locator textArea = Locator.xpath("//table[@id='participantIdentifiers']//textarea");
        waitForElement(textArea, WAIT_FOR_JAVASCRIPT);
        sleep(1000);
        String subjectIDs = getFormElement(textArea);
        Set<String> identifiers = new HashSet<>();

        for (String subjectId : subjectIDs.split(","))
            identifiers.add(subjectId.trim());

        // validate...
        if (!identifiers.containsAll(Arrays.asList(ids)))
            Assert.fail("The Participant Group wizard did not contain the subject IDs : " + ids);
    }

    /** verify that we can change a list's name
     * pre-conditions: list with name listName exists
     * post-conditions: list now named lCHANGEstName
     * @param listName
     * @return new name of list
     */
    @LogMethod
    private String changeListName(String listName)
    {
        String newListName = listName.substring(0, 1) + "CHANGE" + listName.substring(2);
        selectListName(listName);
        clickButtonContainingText("Edit Selected", APPEARS_AFTER_PICKER_LOAD);

        setFormElement(Locator.name(LABEL_FIELD), newListName);

        clickButtonContainingText("Save", 0);

        waitForTextToDisappear(listName, 2*defaultWaitForPage);
        assertTextPresent(newListName);
        return newListName;
    }

    /**
     * verify that we can delete a list and its name no longer appears in classification list
     * pre-conditions:  list listName exists
     * post-conditions:  list listName does not exist
     * @param listName list to delete
     */
    @LogMethod
    private void deleteListTest(String listName)
    {
        sleep(1000);
        selectListName(listName);

        clickButtonContainingText("Delete Selected", 0);

        //make sure we can change our minds

        _extHelper.waitForExtDialog("Delete Group");
        clickButtonContainingText("No", 0);
        waitForExtMaskToDisappear();
        assertTextPresent(listName);


        clickButtonContainingText("Delete Selected", 0);
        _extHelper.waitForExtDialog("Delete Group");
        clickButtonContainingText("Yes", 0);
        waitForExtMaskToDisappear();
        refresh();
        assertTextNotPresent(listName);

    }

    /** verify that attempting to create a list with the expected name and list of IDs causes
     * the error specified by expectedError
     *
     * @param ids IDs to enter in classification list
     * @param expectedError error message to expect
     * @param listName name to enter in classification label
     */
    @LogMethod
    private void attemptCreateExpectError(String ids, String expectedError, String listName)
    {
        startCreateParticipantGroup();

        setFormElement(Locator.name(LABEL_FIELD), listName);
        setFormElement(Locator.name(ID_FIELD), ids);
        clickButtonContainingText("Save", 0);
        waitForText(expectedError, 5*defaultWaitForPage);
        clickButtonContainingText("OK", 0);
        clickButtonContainingText("Cancel", 0);
        assertTextNotPresent(listName);
    }

    /**
     * verify that an already created list contains the pIDs we expect it to and can be changed.
     * pre-conditions:  listName exists with the specified IDs
     * post-conditions:  listName exists, with the same IDs, minus the first one
     *
     * @param listName
     * @param pIDs
     */
    @LogMethod
    private void editClassificationList(String listName, String pIDs)
    {
        sleep(1000);
        selectListName(listName);

        clickButtonContainingText("Edit Selected", APPEARS_AFTER_PICKER_LOAD);
        String newPids = getFormElement(Locator.name(ID_FIELD));
        assertSetsEqual(pIDs, newPids, ", *");
        log("IDs present after opening list: " + newPids);

        //remove first element
        sleep(1000);
        waitForElement(Locator.xpath("//input[contains(@name, 'infoCombo')]"));
        newPids = pIDs.substring(pIDs.indexOf(",")+2);
        setFormElement(Locator.name(ID_FIELD), newPids);
        log("edit list of IDs to: " + newPids);

        //save, close, reopen, verify change
        _extHelper.waitForExtDialog("Define Mouse Group");
        clickButtonContainingText("Save", 0);
        waitForExtMaskToDisappear();
        sleep(1000);
        waitForElement(Locator.xpath("//button[contains(@id, 'deleteSelected')]"));
        selectListName(listName);
        clickButtonContainingText("Edit Selected", APPEARS_AFTER_PICKER_LOAD);

        sleep(1000);
        waitForElement(Locator.xpath("//input[contains(@name, 'infoCombo')]"));
        String pidsAfterEdit =   getFormElement(Locator.name(ID_FIELD));
        log("pids after edit: " + pidsAfterEdit);

        String[] arrPids = newPids.replace(" ","").split(","); Arrays.sort(arrPids);
        String[] arrAfter = pidsAfterEdit.replace(" ","").split(","); Arrays.sort(arrAfter);
        Assert.assertTrue(Arrays.deepEquals(arrPids, arrAfter));

        clickButtonContainingText("Cancel", 0);
    }



    // select the list name from the main classification page
    private void selectListName(String listName)
    {

        Locator report = Locator.tagContainingText("div", listName);

        // select the report and click the delete button
        waitForElement(report, 10000);
        selenium.mouseDownAt(report.toString(), "1,1");
        selenium.clickAt(report.toString(), "1, 1");
    }

    /**
     * very basic test of ability to enter and exit clist creation screen
     *
     * pre-condition:  at participant classification main screen
     * post-condition:  no change
     */
    private void cancelCreateClassificationList()
    {
        startCreateParticipantGroup();
        clickButtonContainingText("Cancel", 0);
    }

    /**preconditions: at participant picker main page
     * post-conditions:  at screen for creating new PP list
     */
    private void startCreateParticipantGroup()
    {
        waitAndClick(Locator.buttonContainingText("Create"));
        _extHelper.waitForExtDialog("Define Mouse Group");
        String dataset = getFormElement(Locator.name("infoCombo"));
        if (dataset.length() > 0)
        {
            waitForElement(Locator.id("demoDataRegion"));
        }
    }


    /** create list using add all
     *
     * @param listName name of list to create
     * @param filtered should list be filtered?  If so, only participants with DEMasian=0 will be included
     * @return ids in new list
     */
    @LogMethod
    private String createListWithAddAll(String listName, boolean filtered)
    {
        startCreateParticipantGroup();
        setFormElement(Locator.name(LABEL_FIELD), listName);
        DataRegionTable table = new DataRegionTable("demoDataRegion", this, true);

        if(filtered)
        {
            table.setFilter("DEMasian", "Equals", "0", 0);
            waitForText("Filter", WAIT_FOR_JAVASCRIPT);
        }

        clickButtonContainingText("Add All", 0);

        List<String> idsInColumn = table.getColumnDataAsText("Mouse Id");
        String idsInForm = getFormElement(Locator.name(ID_FIELD));
        assertIDListsMatch(idsInColumn, idsInForm);

        clickButtonContainingText("Save", 0);

        _extHelper.waitForLoadingMaskToDisappear(WAIT_FOR_JAVASCRIPT);
        waitForText(listName, WAIT_FOR_JAVASCRIPT);
        return idsInForm;
    }

    /**
     * Compare list of IDs extracted from a column to those entered in
     * the form.  They should be identical.
     * @param idsInColumn
     * @param idsInForm
     */
    private void assertIDListsMatch(List<String> idsInColumn, String idsInForm)
    {
        //assert same size
        int columnCount = idsInColumn.size()-2; //the first entry in column count is the name
        int formCount = idsInForm.length() - idsInForm.replace(",", "").length() - 1; //number of commas + 1 = number of entries
        Assert.assertEquals("Wrong number of participants selected", columnCount, formCount);
    }

    private void goToManageParticipantClassificationPage(String projectName, String studyName, String subjectNoun)
    {
        //else
        sleep(1000);
        goToManageStudyPage(projectName, studyName);
        clickAndWait(Locator.linkContainingText("Manage " + subjectNoun + " Groups"));
    }

    @LogMethod
    protected void verifyStudyAndDatasets()
    {
        goToProjectHome();
        verifyDemographics();
        verifyVisitMapPage();
        verifyManageDatasetsPage();


        if(quickTest)
            return;

        if(!isManualTest)
            verifyAlternateIDs();

        verifyHiddenVisits();
        verifyVisitImportMapping();
        verifyCohorts();

        // configure QC state management before importing duplicate data
        clickFolder(getStudyLabel());
        clickAndWait(Locator.linkWithText("Manage Study"));
        clickAndWait(Locator.linkWithText("Manage Dataset QC States"));
        setFormElement("newLabel", "unknown QC");
        setFormElement("newDescription", "Unknown data is neither clean nor dirty.");
        clickCheckboxById("dirty_public");
        clickCheckbox("newPublicData");
        clickButton("Save");
        selectOptionByText("defaultDirectEntryQCState", "unknown QC");
        selectOptionByText("showPrivateDataByDefault", "Public data");
        clickButton("Save");

        // return to dataset import page
        clickFolder(getStudyLabel());
        clickAndWait(Locator.linkWithText(datasetLink));
        clickAndWait(Locator.linkWithText("verifyAssay"));
        assertTextPresent("QC State");
        assertTextNotPresent("1234_B");
        clickMenuButton("QC State", "All data");
        clickButton("QC State", 0);
        assertTextPresent("unknown QC");
        assertTextPresent("1234_B");

        //Import duplicate data
        clickButton("Import Data");
        setFormElement("text", _tsv);
        _listHelper.submitImportTsv_error("Duplicates were found");
        //Now explicitly replace, using 'mouseid' instead of 'participantid'
        _tsv = "mouseid\tsequencenum\tvisitdate\tSampleId\tDateField\tNumberField\tTextField\treplace\n" +
                "1234\t1\t1/1/2006\t1234_A\t2/1/2006\t5000\tnew text\tTRUE\n" +
                "1234\t1\t1/1/2006\t1234_B\t2/1/2006\t5000\tnew text\tTRUE\n";
        _listHelper.submitTsvData(_tsv);
        assertTextPresent("5000.0");
        assertTextPresent("new text");
        assertTextPresent("QC State");
        _customizeViewsHelper.openCustomizeViewPanel();
        _customizeViewsHelper.addCustomizeViewColumn("QCState", "QC State");
        _customizeViewsHelper.applyCustomView();
        assertTextPresent("unknown QC");

        // Test Bad Field Names -- #13607
        clickButton("Manage Dataset");
        clickButton("Edit Definition");
        waitAndClick(WAIT_FOR_JAVASCRIPT, Locator.navButton("Add Field"), 0);
        int newFieldIndex = getXpathCount(Locator.xpath("//input[starts-with(@name, 'ff_name')]")) - 1;
        _listHelper.setColumnName(getPropertyXPath("Dataset Fields"), newFieldIndex, "Bad Name");
        clickButton("Save");
        clickButton("View Data");
        _customizeViewsHelper.openCustomizeViewPanel();
        _customizeViewsHelper.addCustomizeViewColumn("Bad Name", "Bad Name");
        _customizeViewsHelper.applyCustomView();
        clickMenuButton("QC State", "All data");
        clickAndWait(Locator.linkWithText("edit", 0));
        setFormElement(Locator.input("quf_Bad Name"), "Updatable Value");
        clickButton("Submit");
        assertTextPresent("Updatable Value");
        clickAndWait(Locator.linkWithText("edit", 0));
        assertFormElementEquals(Locator.input("quf_Bad Name"), "Updatable Value");
        setFormElement(Locator.input("quf_Bad Name"), "Updatable Value11");
        clickButton("Submit");
        assertTextPresent("Updatable Value11");
    }

    private void verifyAlternateIDs()
    {
        clickFolder(STUDY_NAME);
        clickAndWait(Locator.linkWithText("Alt ID mapping"));
        assertTextPresent("Contains up to one row of Alt ID mapping data for each ");
        clickButton("Import Data");
        waitForText("This is the Alias Dataset. You do not need to include information for the date column");

        //the crawler should be paused (this is done in create) to verify
        log("Verify searching for alternate ID returns participant page");
        SearchHelper searchHelper = new SearchHelper(this);
        searchHelper.searchFor("888208905");
        assertTextPresent("Study Study 001 -- Mouse 999320016");
        goBack();
        goBack();

        //edit an entry, search for that
        log("TODO");

        Map nameAndValue = new HashMap(1);
        nameAndValue.put("Alt ID", "191919");
        (new ChartHelper(this)).editDrtRow(4, nameAndValue);
        searchHelper.searchFor("191919");
        // Issue 17203: Changes to study datasets not auto indexed
//        assertTextPresent("Study Study 001 -- Mouse 999320687");


    }

    @LogMethod
    protected void verifySpecimens()
    {
        clickFolder(getStudyLabel());
        addWebPart("Specimens");
        waitForText("Blood (Whole)");
        clickAndWait(Locator.linkWithText("Blood (Whole)"));
        specimenUrl = getCurrentRelativeURL();


        log("verify presence of \"create new request\" button");
        clickButton("Request Options", 0);
        assertElementPresent(Locator.tagWithText("span", "Create New Request"));
    }

    @LogMethod
    private void verifyParticipantComments()
    {
        log("creating the participant/visit comment dataset");
        clickFolder(getStudyLabel());
        clickAndWait(Locator.linkWithText("Manage Study"));
        clickAndWait(Locator.linkWithText("Manage Datasets"));
        clickAndWait(Locator.linkWithText("Create New Dataset"));

        setFormElement("typeName", PARTICIPANT_CMT_DATASET);
        clickButton("Next");
        waitForElement(Locator.xpath("//input[@id='DatasetDesignerName']"), WAIT_FOR_JAVASCRIPT);

        // set the demographic data checkbox
        checkCheckbox(Locator.xpath("//input[@name='demographicData']"));

        // add a comment field
        _listHelper.setColumnName(0, COMMENT_FIELD_NAME);
        _listHelper.setColumnLabel(0, PARTICIPANT_COMMENT_LABEL);
        _listHelper.setColumnType(0, ListHelper.ListColumnType.MutliLine);
        clickButton("Save");

        log("creating the participant/visit comment dataset");
        clickAndWait(Locator.linkWithText("Manage Datasets"));
        clickAndWait(Locator.linkWithText("Create New Dataset"));

        setFormElement("typeName", PARTICIPANT_VISIT_CMT_DATASET);
        clickButton("Next");
        waitForElement(Locator.xpath("//input[@id='DatasetDesignerName']"), WAIT_FOR_JAVASCRIPT);

        // add a comment field
        _listHelper.setColumnName(0, COMMENT_FIELD_NAME);
        _listHelper.setColumnLabel(0, PARTICIPANT_VISIT_COMMENT_LABEL);
        _listHelper.setColumnType(0, ListHelper.ListColumnType.MutliLine);
        clickButton("Save");

        log("configure comments");
        clickTab("Manage");
        clickAndWait(Locator.linkWithText("Manage Comments"));
        if (isTextPresent("Comments can only be configured for studies with editable datasets"))
        {
            log("configure editable datasets");
            clickTab("Manage");
            clickAndWait(Locator.linkWithText("Manage Security"));
            selectOptionByText(Locator.name("securityString"), "Basic security with editable datasets");
            waitForPageToLoad();

            log("configure comments");
            clickFolder(getStudyLabel());
            clickTab("Manage");
            clickAndWait(Locator.linkWithText("Manage Comments"));
        }
        selectOptionByText("participantCommentDataSetId", PARTICIPANT_CMT_DATASET);
        waitForPageToLoad();
        selectOptionByText("participantCommentProperty", PARTICIPANT_COMMENT_LABEL);

        selectOptionByText("participantVisitCommentDataSetId", PARTICIPANT_VISIT_CMT_DATASET);
        waitForPageToLoad();
        selectOptionByText("participantVisitCommentProperty", PARTICIPANT_VISIT_COMMENT_LABEL);
        clickButton("Save");

        clickFolder(getStudyLabel());
        waitForText("Blood (Whole)");
        clickAndWait(Locator.linkWithText("Blood (Whole)"));
        clickButton("Enable Comments/QC");
        log("manage participant comments directly");
        clickMenuButton("Comments and QC", "Manage Mouse Comments");

        int datasetAuditEventCount = getDatasetAuditEventCount(); //inserting a new event should increase this by 1;
        clickButton("Insert New");
        setFormElement("quf_MouseId", "999320812");
        setFormElement("quf_" + COMMENT_FIELD_NAME, "Mouse Comment");
        clickButton("Submit");
        //Issue 14894: Datasets no longer audit row insertion
        verifyAuditEventAdded(datasetAuditEventCount);

        clickFolder(getStudyLabel());
        waitForText("Blood (Whole)");
        clickAndWait(Locator.linkWithText("Blood (Whole)"));
        setFilter("SpecimenDetail", "MouseId", "Equals", "999320812");

        assertTextPresent("Mouse Comment");
        clearAllFilters("SpecimenDetail", "MouseId");

        log("verify copying and moving vial comments");
        setFilter("SpecimenDetail", "GlobalUniqueId", "Equals", "AAA07XK5-01");
        selenium.click(".toggle");
        clickButton("Enable Comments/QC");
        clickMenuButton("Comments and QC", "Set Vial Comment or QC State for Selected");
        setFormElement("comments", "Vial Comment");
        clickButton("Save Changes");

        selenium.click(".toggle");
        clickMenuButton("Comments and QC", "Set Vial Comment or QC State for Selected");
        clickMenuButton("Copy or Move Comment(s)", "Copy", "To Mouse", "999320812");
        setFormElement("quf_" + COMMENT_FIELD_NAME, "Copied PTID Comment");
        clickButton("Submit");
        assertTextPresent("Copied PTID Comment");

        selenium.click(".toggle");
        clickMenuButton("Comments and QC", "Set Vial Comment or QC State for Selected");
        clickMenuButtonAndContinue("Copy or Move Comment(s)", "Move", "To Mouse", "999320812");
        getConfirmationAndWait();
        setFormElement("quf_" + COMMENT_FIELD_NAME, "Moved PTID Comment");
        clickButton("Submit");
        assertTextPresent("Moved PTID Comment");
        assertTextNotPresent("Mouse Comment");
        assertTextNotPresent("Vial Comment");
    }

    private void verifyAuditEventAdded(int previousCount)
    {
        log("Verify there is exactly one new DatasetAuditEvent, and it refers to the insertion of a new record");
        SelectRowsResponse selectResp = getDatasetAuditLog();
        List<Map<String,Object>> rows = selectResp.getRows();
        Assert.assertEquals("Unexpected size of datasetAuditEvent log", previousCount + 1, rows.size());
        log("Dataset audit log contents: " + rows);
        Assert.assertEquals("A new dataset record was inserted", rows.get(rows.size() - 1).get("Comment"));
    }

    private SelectRowsResponse getDatasetAuditLog()
    {
        SelectRowsCommand selectCmd = new SelectRowsCommand("auditLog", "DatasetAuditEvent");

        selectCmd.setMaxRows(-1);
        selectCmd.setContainerFilter(ContainerFilter.CurrentAndSubfolders);
        selectCmd.setColumns(Arrays.asList("*"));
        selectCmd.setSorts(Collections.singletonList(new Sort("Date", Sort.Direction.ASCENDING)));
        Connection cn = new Connection(getBaseURL(), getUsername(), PasswordUtil.getPassword());
        SelectRowsResponse selectResp = null;
        try
        {
            selectResp = selectCmd.execute(cn,  "/" +  getProjectName());
        }
        catch (Exception e)
        {
            Assert.fail("Error when attempting to verify audit trail: " + e.getMessage());
        }
        return selectResp;
    }

    private int getDatasetAuditEventCount()
    {
        SelectRowsResponse selectResp = getDatasetAuditLog();
        List<Map<String,Object>> rows = selectResp.getRows();
        return rows.size();
    }

    @LogMethod
    private void verifyDemographics()
    {
        clickFolder(getFolderName());
        clickAndWait(Locator.linkWithText("Study Navigator"));
        clickAndWait(Locator.linkWithText("24"));
        assertTextPresent(DEMOGRAPHICS_DESCRIPTION);
        assertTextPresent("Male");
        assertTextPresent("African American or Black");
        clickAndWait(Locator.linkWithText("999320016"));
        click(Locator.linkWithText("125: EVC-1: Enrollment Vaccination"));
        assertTextPresent("right deltoid");
        
        verifyDemoCustomizeOptions();
        verifyDatasetExport();
    }

    @LogMethod
    private void verifyDatasetExport()
    {
        pushLocation();
        exportDataRegion("Script", "R");
        goToAuditLog();
        selectOptionByText("view", "Query events");
        waitForPageToLoad();

        DataRegionTable auditTable =  new DataRegionTable("audit", this);
        String[][] columnAndValues = new String[][] {{"Created By", getDisplayName()},
                {"Project", PROJECT_NAME}, {"Container", STUDY_NAME}, {"SchemaName", "study"},
                {"QueryName", "DEM-1"}, {"Comment", "Exported to script type r"}};
        for(String[] columnAndValue : columnAndValues)
        {
            log("Checking column: "+ columnAndValue[0]);
            Assert.assertEquals(columnAndValue[1], auditTable.getDataAsText(0, columnAndValue[0]));
        }
        clickAndWait(Locator.linkContainingText("details"));

        popLocation();
    }

    private void verifyDemoCustomizeOptions()
    {
        log("verify demographic data set not present");
        clickAndWait(Locator.linkContainingText(DEMOGRAPHICS_TITLE));
        _customizeViewsHelper.openCustomizeViewPanel();
        Assert.assertFalse(_customizeViewsHelper.isColumnPresent("MouseVisit/DEM-1"));
    }

    @LogMethod
    protected void verifyVisitMapPage()
    {
        clickFolder(getStudyLabel());
        clickAndWait(Locator.linkWithText("Manage Study"));
        clickAndWait(Locator.linkWithText("Manage Visits"));

        // test optional/required/not associated
        clickAndWait(Locator.linkWithText("edit", 1));
        selectOption("dataSetStatus", 0, "NOT_ASSOCIATED");
        selectOption("dataSetStatus", 1, "NOT_ASSOCIATED");
        selectOption("dataSetStatus", 2, "NOT_ASSOCIATED");
        selectOption("dataSetStatus", 3, "OPTIONAL");
        selectOption("dataSetStatus", 4, "OPTIONAL");
        selectOption("dataSetStatus", 5, "OPTIONAL");
        selectOption("dataSetStatus", 6, "REQUIRED");
        selectOption("dataSetStatus", 7, "REQUIRED");
        selectOption("dataSetStatus", 8, "REQUIRED");
        clickButton("Save");
        clickAndWait(Locator.linkWithText("edit", 1));
        selectOption("dataSetStatus", 0, "NOT_ASSOCIATED");
        selectOption("dataSetStatus", 1, "OPTIONAL");
        selectOption("dataSetStatus", 2, "REQUIRED");
        selectOption("dataSetStatus", 3, "NOT_ASSOCIATED");
        selectOption("dataSetStatus", 4, "OPTIONAL");
        selectOption("dataSetStatus", 5, "REQUIRED");
        selectOption("dataSetStatus", 6, "NOT_ASSOCIATED");
        selectOption("dataSetStatus", 7, "OPTIONAL");
        selectOption("dataSetStatus", 8, "REQUIRED");
        clickButton("Save");
        clickAndWait(Locator.linkWithText("edit", 1));
        assertSelectOption("dataSetStatus", 0, "NOT_ASSOCIATED");
        assertSelectOption("dataSetStatus", 1, "OPTIONAL");
        assertSelectOption("dataSetStatus", 2, "REQUIRED");
        assertSelectOption("dataSetStatus", 3, "NOT_ASSOCIATED");
        assertSelectOption("dataSetStatus", 4, "OPTIONAL");
        assertSelectOption("dataSetStatus", 5, "REQUIRED");
        assertSelectOption("dataSetStatus", 6, "NOT_ASSOCIATED");
        assertSelectOption("dataSetStatus", 7, "OPTIONAL");
        assertSelectOption("dataSetStatus", 8, "REQUIRED");
    }

    @LogMethod
    protected void verifyManageDatasetsPage()
    {
        clickFolder(getFolderName());
        clickTab("Manage");
        clickAndWait(Locator.linkWithText("Manage Datasets"));

        clickAndWait(Locator.linkWithText("489"));
        assertTextPresent("ESIdt");
        assertTextPresent("Form Completion Date");
        assertTableCellTextEquals("details", 4, 1, "false");     // "Demographics Data" should be false

        // Verify that "Demographics Data" is checked and description is set
        clickAndWait(Locator.linkWithText("Manage Datasets"));
        clickAndWait(Locator.linkWithText("DEM-1: Demographics"));
        assertTableCellTextEquals("details", 4, 1, "true");
        assertTableCellTextEquals("details", 3, 3, DEMOGRAPHICS_DESCRIPTION);

        // "Demographics Data" bit needs to be false for the rest of the test
        setDemographicsBit("DEM-1: Demographics", false);

        log("verify ");
        clickButtonContainingText("View Data");
        _customizeViewsHelper.openCustomizeViewPanel();
        Assert.assertTrue("Could not find column \"MouseVisit/DEM-1\"", _customizeViewsHelper.isColumnPresent("MouseVisit/DEM-1"));
    }

    @LogMethod
    private void verifyHiddenVisits()
    {
        clickFolder(getStudyLabel());
        clickAndWait(Locator.linkWithText("Study Navigator"));
        assertTextNotPresent("Screening Cycle");
        assertTextNotPresent("Cycle 1");
        assertTextPresent("Pre-exist Cond");
        clickAndWait(Locator.linkWithText("Show All Datasets"));
        assertTextPresent("Screening Cycle");
        assertTextPresent("Cycle 1");
        assertTextPresent("Pre-exist Cond");
    }

    @LogMethod
    private void verifyVisitImportMapping()
    {
        clickAndWait(Locator.linkWithText("Manage Study"));
        clickAndWait(Locator.linkWithText("Manage Visits"));
        clickAndWait(Locator.linkWithText("Visit Import Mapping"));
        assertTableRowsEqual("customMapping", 2, VISIT_IMPORT_MAPPING.replace("SequenceNum", "Sequence Number Mapping"));

        Assert.assertEquals("Incorrect number of gray cells", 60, countTableCells(null, true));
        Assert.assertEquals("Incorrect number of non-gray \"Int. Vis. %{S.1.1} .%{S.2.1}\" cells", 1, countTableCells("Int. Vis. %{S.1.1} .%{S.2.1}", false));
        Assert.assertEquals("Incorrect number of gray \"Int. Vis. %{S.1.1} .%{S.2.1}\" cells", 18, countTableCells("Int. Vis. %{S.1.1} .%{S.2.1}", true));
        Assert.assertEquals("Incorrect number of non-gray \"Soc Imp Log #%{S.3.2}\" cells", 1, countTableCells("Soc Imp Log #%{S.3.2}", false));
        Assert.assertEquals("Incorrect number of gray \"Soc Imp Log #%{S.3.2}\" cells", 1, countTableCells("Soc Imp Log #%{S.3.2}", true));
        Assert.assertEquals("Incorrect number of non-gray \"ConMeds Log #%{S.3.2}\" cells", 1, countTableCells("ConMeds Log #%{S.3.2}", false));
        Assert.assertEquals("Incorrect number of gray \"ConMeds Log #%{S.3.2}\" cells", 1, countTableCells("ConMeds Log #%{S.3.2}", true));

        // Replace custom visit mapping and verify
        String replaceMapping = "Name\tSequenceNum\nBarBar\t4839\nFoofoo\t9732";
        clickAndWait(Locator.linkWithText("Replace Custom Mapping"));
        setFormElement(Locator.id("tsv"), replaceMapping);
        clickButton("Submit");
        assertTableRowsEqual("customMapping", 2, replaceMapping.replace("SequenceNum", "Sequence Number Mapping"));
        assertTextNotPresent("Cycle 10");
        assertTextNotPresent("All Done");

        Assert.assertEquals("Incorrect number of gray cells", 54, countTableCells(null, true));
        Assert.assertEquals("Incorrect number of non-gray \"Int. Vis. %{S.1.1} .%{S.2.1}\" cells", 1, countTableCells("Int. Vis. %{S.1.1} .%{S.2.1}", false));
        Assert.assertEquals("Incorrect number of gray \"Int. Vis. %{S.1.1} .%{S.2.1}\" cells", 18, countTableCells("Int. Vis. %{S.1.1} .%{S.2.1}", true));
        Assert.assertEquals("Incorrect number of non-gray \"Soc Imp Log #%{S.3.2}\" cells", 1, countTableCells("Soc Imp Log #%{S.3.2}", false));
        Assert.assertEquals("Incorrect number of gray \"Soc Imp Log #%{S.3.2}\" cells", 0, countTableCells("Soc Imp Log #%{S.3.2}", true));
        Assert.assertEquals("Incorrect number of non-gray \"ConMeds Log #%{S.3.2}\" cells", 1, countTableCells("ConMeds Log #%{S.3.2}", false));
        Assert.assertEquals("Incorrect number of gray \"ConMeds Log #%{S.3.2}\" cells", 0, countTableCells("ConMeds Log #%{S.3.2}", true));

        clickFolder(getFolderName());
        clickAndWait(Locator.linkWithText(datasetLink));
        clickAndWait(Locator.linkWithText("Types"));
        log("Verifying sequence numbers and visit names imported correctly");

        DataRegionTable table = new DataRegionTable("Dataset", this, true, true);
        List<String> sequenceNums = table.getColumnDataAsText("Sequence Num");
        Assert.assertEquals("Incorrect number of rows in Types dataset", 48, sequenceNums.size());

        int sn101 = 0;
        int sn201 = 0;

        for (String seqNum : sequenceNums)
        {
            // Use startsWith because StudyTest and StudyExportTest have different default format strings
            if (seqNum.startsWith("101.0"))
                sn101++;
            else if (seqNum.startsWith("201.0"))
                sn201++;
            else
                Assert.fail("Unexpected sequence number: " + seqNum);
        }

        Assert.assertEquals("Incorrect count for sequence number 101.0", 24, sn101);
        Assert.assertEquals("Incorrect count for sequence number 201.0", 24, sn201);
    }

    // Either param can be null
    private int countTableCells(String text, Boolean grayed)
    {
        List<String> parts = new LinkedList<>();

        if (null != text)
            parts.add("contains(text(), '" + text + "')");

        if (null != grayed)
        {
            if (grayed)
                parts.add("contains(@class, 'labkey-mv')");
            else
                parts.add("not(contains(@class, 'labkey-mv'))");
        }

        String path = "//td[" + StringUtils.join(parts, " and ") + "]";
        return getXpathCount(Locator.xpath(path));
    }

    protected void verifyCohorts()
    {
        verifyCohorts(true);
    }

    @LogMethod
    protected void verifyCohorts(boolean altIDsEnabled)       //todo
    {
        clickFolder(getStudyLabel());
        clickAndWait(Locator.linkWithText("Study Navigator"));
        clickAndWait(Locator.linkWithText("24"));

        // verify that cohorts are working
        assertTextPresent("999320016");
        assertTextPresent("999320518");

        clickMenuButton("Mouse Groups", "Cohorts", "Group 1");
        assertTextPresent("999320016");
        assertTextNotPresent("999320518");

        clickMenuButton("Mouse Groups", "Cohorts", "Group 2");
        assertTextNotPresent("999320016");
        assertTextPresent("999320518");

        // verify that the participant view respects the cohort filter:
        setSort("Dataset", "MouseId", SortDirection.ASC);
        clickAndWait(Locator.linkWithText("999320518"));
        if(!isManualTest)
            assertTextPresent("b: 888209407"); //Alternate ID
        click(Locator.linkWithText("125: EVC-1: Enrollment Vaccination"));
        assertTextNotPresent("Group 1");
        assertTextPresent("Group 2");
        clickAndWait(Locator.linkWithText("Next Mouse"));
        assertTextNotPresent("Group 1");
        assertTextPresent("Group 2");
        clickAndWait(Locator.linkWithText("Next Mouse"));
        assertTextNotPresent("Group 1");
        assertTextPresent("Group 2");
        clickAndWait(Locator.linkWithText("Next Mouse"));
    }
}
