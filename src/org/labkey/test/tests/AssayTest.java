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
import org.labkey.test.Locator;
import org.labkey.test.TestTimeoutException;
import org.labkey.test.util.DataRegionTable;
import org.labkey.test.util.LogMethod;

import static org.labkey.test.util.ListHelper.ListColumnType;

import java.io.File;

/**
 * User: jeckels
 * Date: Aug 10, 2007
 *
 * Modified by DaveS on 13 Sept 13 2007
 *  Added security-related tests, and refactored the code to run Luminex by itself in a separate project
 */
public class AssayTest extends AbstractAssayTest
{
    protected static final String TEST_ASSAY = "Test" + TRICKY_CHARACTERS + "Assay1";
    protected static final String TEST_ASSAY_DESC = "Description for assay 1";

    protected static final String TEST_ASSAY_SET_PROP_EDIT = "NewTargetStudy";
    protected static final String TEST_ASSAY_SET_PROP_NAME = "testAssaySetProp";
    protected static final int TEST_ASSAY_SET_PREDEFINED_PROP_COUNT = 2;
    protected static final ListColumnType[] TEST_ASSAY_SET_PROP_TYPES = { ListColumnType.Boolean, ListColumnType.Double, ListColumnType.Integer, ListColumnType.DateTime };
    protected static final String[] TEST_ASSAY_SET_PROPERTIES = { "false", "100.0", "200", "2001-10-10" };
    protected static final String TEST_ASSAY_RUN_PROP_NAME = "testAssayRunProp";
    protected static final int TEST_ASSAY_RUN_PREDEFINED_PROP_COUNT = 0;
    protected static final ListColumnType[] TEST_ASSAY_RUN_PROP_TYPES = { ListColumnType.String, ListColumnType.Boolean, ListColumnType.Double, ListColumnType.Integer, ListColumnType.DateTime };
    protected static final String TEST_ASSAY_RUN_PROP1 = "TestRunProp";
    protected static final String TEST_ASSAY_DATA_PROP_NAME = "testAssayDataProp";
    protected static final String TEST_ASSAY_DATA_ALIASED_PROP_NAME = "testAssayAliasedData";
    protected static final String ALIASED_DATA = "aliasedData";
    public static final int TEST_ASSAY_DATA_PREDEFINED_PROP_COUNT = 4;
    protected static final ListColumnType[] TEST_ASSAY_DATA_PROP_TYPES = { ListColumnType.Boolean, ListColumnType.Integer, ListColumnType.DateTime, ListColumnType.String };
    protected static final String TEST_RUN1 = "FirstRun";
    protected static final String TEST_RUN1_COMMENTS = "First comments";
    protected static final String TEST_RUN1_DATA1 = "specimenID\tparticipantID\tvisitID\t" + TEST_ASSAY_DATA_PROP_NAME + "20\t" + TEST_ASSAY_DATA_PROP_NAME + "5\t" + TEST_ASSAY_DATA_PROP_NAME + "6\n" +
            "AAA07XK5-05\t\t\ttrue\t20\t2000-01-01\n" +
            "AAA07XMC-02\t\t\ttrue\t19\t2000-02-02\n" +
            "AAA07XMC-04\t\t\ttrue\t18\t2000-03-03\n" +
            "AAA07XSF-02\t\t\tfalse\t17\t2000-04-04\n" +
            "AssayTestControl1\te\t5\tfalse\t16\t2000-05-05\n" +
            "AssayTestControl2\tf\tg\tfalse\t15\t2000-06-06";
    protected static final String TEST_RUN1_DATA2 = "specimenID\tparticipantID\tvisitID\t" + TEST_ASSAY_DATA_PROP_NAME + "4\t" + TEST_ASSAY_DATA_PROP_NAME + "5\t" + TEST_ASSAY_DATA_PROP_NAME + "6\n" +
            "AAA07XK5-05\t\ttrue\t20\t2000-01-01\n" +
            "AAA07XMC-02\t\t\ttrue\t19\t2000-02-02\n" +
            "AAA07XMC-04\t\t\ttrue\t18\t2000-03-03\n" +
            "AAA07XSF-02\t\t\tfalse\t17\t2000-04-04\n" +
            "AssayTestControl1\te\t5\tfalse\t16\t2000-05-05\n" +
            "AssayTestControl2\tf\tg\tfalse\t15\t2000-06-06";
    protected static final String TEST_RUN1_DATA3 = "specimenID\tparticipantID\tvisitID\t" + TEST_ASSAY_DATA_PROP_NAME + "4\t" + TEST_ASSAY_DATA_PROP_NAME + "5\t" + TEST_ASSAY_DATA_PROP_NAME + "6\n" +
            "AAA07XK5-05\t\t\ttrue\t20\t\n" +
            "AAA07XMC-02\t\t\ttrue\t19\t\n" +
            "AAA07XMC-04\t\t\ttrue\t18\t";
    protected static final String TEST_RUN1_DATA4 = "specimenID\tparticipantID\tvisitID\t" + TEST_ASSAY_DATA_PROP_NAME + "4\t" + TEST_ASSAY_DATA_PROP_NAME + "5\t" + TEST_ASSAY_DATA_PROP_NAME + "6\t" + TEST_ASSAY_DATA_ALIASED_PROP_NAME + "\n" +
            "AAA07XK5-05\t\t\ttrue\t\t2000-01-01\t"+ALIASED_DATA+"\n" +
            "AAA07XMC-02\t\t\ttrue\t\t2000-02-02\t"+ALIASED_DATA+"\n" +
            "AAA07XMC-04\t\t\ttrue\t\t2000-03-03\t"+ALIASED_DATA+"\n" +
            "AAA07XSF-02\t\t\tfalse\t\t2000-04-04\t"+ALIASED_DATA+"\n" +
            "AssayTestControl1\te\t5\tfalse\t\t2000-05-05\t"+ALIASED_DATA+"\n" +
            "AssayTestControl2\tf\t6\tfalse\t\t2000-06-06\t"+ALIASED_DATA;
    protected static final String TEST_RUN2 = "SecondRun";
    protected static final String TEST_RUN2_COMMENTS = "Second comments";
    protected static final String TEST_RUN2_DATA1 = "specimenID\tparticipantID\tvisitID\t" + TEST_ASSAY_DATA_PROP_NAME + "4\t" + TEST_ASSAY_DATA_PROP_NAME + "5\t" + TEST_ASSAY_DATA_PROP_NAME + "6\n" +
            "BAQ00051-09\tg\t7\ttrue\t20\t2000-01-01\n" +
            "BAQ00051-08\th\t8\ttrue\t19\t2000-02-02\n" +
            "BAQ00051-11\ti\t9\ttrue\t18\t2000-03-03\n";

    private static final String INVESTIGATOR = "Dr. No";
    private static final String GRANT = "SPECTRE";
    private static final String DESCRIPTION = "World Domination.";

    public String getAssociatedModuleDirectory()
    {
        return "server/modules/study";
    }

    @Override
    protected String getProjectName()
    {
        return TEST_ASSAY_PRJ_SECURITY;
    }

    /**
     * Cleanup entry point.
     * @param afterTest
     */
    protected void doCleanup(boolean afterTest) throws TestTimeoutException
    {
        deleteProject(TEST_ASSAY_PRJ_SECURITY, afterTest); //should also delete the groups

        //delete user accounts
        deleteUsers(afterTest, TEST_ASSAY_USR_PI1, TEST_ASSAY_USR_TECH1);
        deleteDir(getTestTempDir());
    } //doCleanup()

    /**
     *  Performs the Assay security test
     *  This test creates a project with a folder hierarchy with multiple groups and users;
     *  defines an Assay at the project level; uploads run data as a labtech; publishes
     *  as a PI, and tests to make sure that security is properly enforced
     */
    @LogMethod
    protected void runUITests()
    {

//        setSystemMaintenance(false);
        /** Manually start system maintenance... we'll check for completion at the end of the test (before mem check)
        * this has nothing to do with assays, but needs to be run regularly for performance reasons and can take some time to complete
        * assay test is the longest test in the BVTs, so it will cause the fewest delays here
         * **/
        startSystemMaintenance();



        log("Starting Assay security scenario tests");
        setupEnvironment();
        setupPipeline(TEST_ASSAY_PRJ_SECURITY);
        SpecimenImporter importer = new SpecimenImporter(getTestTempDir(), new File(getLabKeyRoot(), "/sampledata/study/specimens/sample_a.specimens"), new File(getTestTempDir(), "specimensSubDir"), TEST_ASSAY_FLDR_STUDY2, 1);
        importer.importAndWaitForComplete();
        defineAssay();
        uploadRuns(TEST_ASSAY_FLDR_LAB1, TEST_ASSAY_USR_TECH1);
        editResults();
        publishData();
        publishDataToDateBasedStudy();
        publishDataToVisitBasedStudy();
        editAssay();
        viewCrossFolderData();
        verifyStudyList();
        verifyRunDeletionRecallsDatasetRows();
        // TODO: Turn this on once file browser migration is complete -- see Nick.
//        verifyWebdavTree();
        goBack();


        // Now that the test is done, ensure that system maintenance is complete...
        waitForSystemMaintenanceCompletion();
    }

    @LogMethod(category = LogMethod.MethodType.VERIFICATION)
    private void verifyRunDeletionRecallsDatasetRows()
    {
        clickFolder(getProjectName());
        clickAndWait(Locator.linkWithText(TEST_ASSAY_FLDR_LAB1));
        clickAndWait(Locator.linkWithText(TEST_ASSAY));
        checkDataRegionCheckbox("Runs", 0);
        clickButton("Delete");
        // Make sure that it shows that the data is part of study datasets
        assertTextPresent("SecondRun", "2 dataset(s)", TEST_ASSAY);
        assertTextNotPresent("FirstRun");
        // Do the delete
        clickButton("Confirm Delete");

        // Be sure that we have a special audit record
        clickAndWait(Locator.linkWithText("view copy-to-study history"));
        assertTextPresent("3 row(s) were recalled to the assay: ");

        // Verify that the deleted run data is gone from the dataset
        clickAndWait(Locator.linkWithText(TEST_ASSAY_FLDR_STUDY2));
        clickAndWait(Locator.linkWithText("1 dataset"));
        clickAndWait(Locator.linkWithText(TEST_ASSAY));
        assertTextPresent("AAA07XMC-04", "FirstRun");
        assertTextNotPresent("BAQ00051-09", "SecondRun");
    }

    //Issue 12203: Incorrect files are visible from pipeline directory
    private void verifyWebdavTree()
    {
        beginAt("_webdav");
        _extHelper.selectFileBrowserItem(TEST_ASSAY_PRJ_SECURITY + "/Studies/Study 1");
        assertTextPresent("@pipeline", 2);
        Locator.XPathLocator l = Locator.xpath("//span[text()='@pipeline']");
        assertElementPresent(l,  1);

    }

    @LogMethod(category = LogMethod.MethodType.MIXEDPURPOSE)
    private void editResults()
    {
        // Verify that the results aren't editable by default
        clickFolder(TEST_ASSAY_FLDR_LAB1);
        clickAndWait(Locator.linkWithText(TEST_ASSAY));
        clickAndWait(Locator.linkWithText("view results"));
        assertLinkNotPresentWithText("edit");
        assertNavButtonNotPresent("Delete");

        // Edit the design to make them editable
        clickEditAssayDesign(true);
        waitForElement(Locator.xpath("//span[@id='id_editable_results_properties']"), WAIT_FOR_JAVASCRIPT);
        checkCheckbox(Locator.xpath("//span[@id='id_editable_results_properties']/input"));
        clickButton("Save & Close");

        // Try an edit
        clickFolder(TEST_ASSAY_FLDR_LAB1);
        clickAndWait(Locator.linkWithText(TEST_ASSAY));
        clickAndWait(Locator.linkWithText("view results"));
        assertElementPresent(Locator.xpath("//img[@src='/labkey/Experiment/unflagDefault.gif'][@title='Flag for review']"), 9);
        clickAndWait(Locator.linkWithText("edit"));
        setFormElement(Locator.name("quf_SpecimenID"), "EditedSpecimenID");
        setFormElement(Locator.name("quf_VisitID"), "601.5");
        setFormElement(Locator.name("quf_testAssayDataProp5"), "a");
        clickButton("Submit");
        assertTextPresent("Could not convert value: a");
        setFormElement(Locator.name("quf_testAssayDataProp5"), "514801");
        setFormElement(Locator.name("quf_Flags"), "This Flag Has Been Edited");
        clickButton("Submit");
        assertTextPresent("EditedSpecimenID", "601.5", "514801");
        assertElementPresent(Locator.xpath("//img[@src='/labkey/Experiment/flagDefault.gif'][@title='This Flag Has Been Edited']"), 1);
        assertElementPresent(Locator.xpath("//img[@src='/labkey/Experiment/unflagDefault.gif'][@title='Flag for review']"), 8);

        // Try a delete
        checkCheckbox(".select");
        selenium.chooseOkOnNextConfirmation();
        clickButton("Delete");
        assertConfirmation("Are you sure you want to delete the selected row?");

        // Verify that the edit was audited
        goToModule("Query");
        selectQuery("auditLog", "ExperimentAuditEvent");
        waitForElement(Locator.linkWithText("view data"), WAIT_FOR_JAVASCRIPT);
        clickAndWait(Locator.linkWithText("view data"));
        assertTextPresent("Data row, id ", ", edited in " + TEST_ASSAY + ".",
                "Specimen ID changed from 'AAA07XK5-05' to 'EditedSpecimenID'",
                "Visit ID changed from '601.0' to '601.5",
                "testAssayDataProp5 changed from blank to '514801'");
        assertTextPresent("Deleted data row.");

        clickFolder(TEST_ASSAY_PRJ_SECURITY);
    }

    /**
     * Defines an test assay at the project level for the security-related tests
     */
    @LogMethod(category = LogMethod.MethodType.SETUP)
    private void defineAssay()
    {
        log("Defining a test assay at the project level");
        //define a new assay at the project level
        //the pipeline must already be setup
        clickFolder(TEST_ASSAY_PRJ_SECURITY);
        addWebPart("Assay List");

        //copied from old test
        clickButton("Manage Assays");
        clickButton("New Assay Design");
        assertElementNotPresent(Locator.radioButtonByNameAndValue("providerName", "Flow"));
        checkRadioButton("providerName", "General");
        clickButton("Next");

        waitForElement(Locator.xpath("//input[@id='AssayDesignerName']"), WAIT_FOR_JAVASCRIPT);

        selenium.type("//input[@id='AssayDesignerName']", TEST_ASSAY);
        selenium.type("//textarea[@id='AssayDesignerDescription']", TEST_ASSAY_DESC);

        for (int i = TEST_ASSAY_SET_PREDEFINED_PROP_COUNT; i < TEST_ASSAY_SET_PREDEFINED_PROP_COUNT + TEST_ASSAY_SET_PROP_TYPES.length; i++)
        {
            _listHelper.addField("Batch Fields", i, TEST_ASSAY_SET_PROP_NAME + i, TEST_ASSAY_SET_PROP_NAME + i, TEST_ASSAY_SET_PROP_TYPES[i - TEST_ASSAY_SET_PREDEFINED_PROP_COUNT]);
        }

        for (int i = TEST_ASSAY_RUN_PREDEFINED_PROP_COUNT; i < TEST_ASSAY_RUN_PREDEFINED_PROP_COUNT + TEST_ASSAY_RUN_PROP_TYPES.length; i++)
        {
            _listHelper.addField("Run Fields", i, TEST_ASSAY_RUN_PROP_NAME + i, TEST_ASSAY_RUN_PROP_NAME + i, TEST_ASSAY_RUN_PROP_TYPES[i - TEST_ASSAY_RUN_PREDEFINED_PROP_COUNT]);
        }

        for (int i = TEST_ASSAY_DATA_PREDEFINED_PROP_COUNT; i < TEST_ASSAY_DATA_PREDEFINED_PROP_COUNT + TEST_ASSAY_DATA_PROP_TYPES.length; i++)
        {
            _listHelper.addField("Data Fields", i, TEST_ASSAY_DATA_PROP_NAME + i, TEST_ASSAY_DATA_PROP_NAME + i, TEST_ASSAY_DATA_PROP_TYPES[i - TEST_ASSAY_DATA_PREDEFINED_PROP_COUNT]);
        }

        _listHelper.addField("Data Fields", TEST_ASSAY_DATA_PREDEFINED_PROP_COUNT + TEST_ASSAY_DATA_PROP_TYPES.length, "Flags", "Flags", ListColumnType.Flag);

        // Set some to required
        setRequired("Batch Fields", TEST_ASSAY_SET_PREDEFINED_PROP_COUNT);
        setRequired("Batch Fields", TEST_ASSAY_SET_PREDEFINED_PROP_COUNT+1);
        setRequired("Run Fields", 0);
        setRequired("Data Fields", 0);
        setRequired("Data Fields", TEST_ASSAY_DATA_PREDEFINED_PROP_COUNT + 2);

        // import aliases
        _listHelper.clickRow(getPropertyXPath("Data Fields"), TEST_ASSAY_DATA_PREDEFINED_PROP_COUNT + 3);
        click(Locator.xpath(getPropertyXPath("Data Fields") + "//span[contains(@class,'x-tab-strip-text') and text()='Advanced']"));
        waitForElement(Locator.xpath(getPropertyXPath("Data Fields") + "//td/input[@id='importAliases']"), WAIT_FOR_JAVASCRIPT);
        setFormElement(Locator.xpath(getPropertyXPath("Data Fields") + "//td/input[@id='importAliases']"), TEST_ASSAY_DATA_ALIASED_PROP_NAME);

        sleep(1000);
        clickButton("Save", 0);
        waitForText("Save successful.", 20000);

    } //defineAssay()

    /**
     * Generates the text that appears in the target study drop-down for a given study name
     * @param studyName name of the target study
     * @return formatted string of what appears in the target study drop-down
     */
    private String getTargetStudyOptionText(String studyName)
    {
        //the format used in the drop down is:
        // /<project>/<studies>/<study1> (<study> Study)
        return "/" + TEST_ASSAY_PRJ_SECURITY + "/" + TEST_ASSAY_FLDR_STUDIES + "/" +
                    studyName + " (" + studyName + " Study)";
    } //getTargetStudyOptionText()

    /**
     * Uploads run data for the centrally defined Assay while impersonating a labtech-style user
     * @param folder    name of the folder into which we should upload
     * @param asUser    the user to impersonate before uploading
     */
    @LogMethod(category = LogMethod.MethodType.SETUP)
    private void uploadRuns(String folder, String asUser)
    {
        log("Uploading runs into folder " + folder + " as user " + asUser);
        impersonate(asUser);
        clickFolder(TEST_ASSAY_PRJ_SECURITY);
        clickFolder(folder);

        clickAndWait(Locator.linkWithText("Assay List"));
        clickAndWait(Locator.linkWithText(TEST_ASSAY));

        //nav trail check
        assertTextPresent("Assay List >  " + TEST_ASSAY + " Batches >  ");

        clickButton("Import Data");
        assertTextPresent(TEST_ASSAY_SET_PROP_NAME + "3");

        log("Batch properties");
        clickButton("Next");
        assertTextPresent(TEST_ASSAY_SET_PROP_NAME + (TEST_ASSAY_SET_PREDEFINED_PROP_COUNT + 1) + " is required and must be of type Number (Double).");
        setFormElement(TEST_ASSAY_SET_PROP_NAME + (TEST_ASSAY_SET_PREDEFINED_PROP_COUNT + 1), "Bad Test");
        setFormElement(TEST_ASSAY_SET_PROP_NAME + (TEST_ASSAY_SET_PREDEFINED_PROP_COUNT + 2), "Bad Test");
        setFormElement(TEST_ASSAY_SET_PROP_NAME + (TEST_ASSAY_SET_PREDEFINED_PROP_COUNT + 3), "Bad Test");
        clickButton("Next");
        assertTextPresent(TEST_ASSAY_SET_PROP_NAME + (TEST_ASSAY_SET_PREDEFINED_PROP_COUNT + 1) + " must be of type Number (Double).");
        assertTextPresent(TEST_ASSAY_SET_PROP_NAME + (TEST_ASSAY_SET_PREDEFINED_PROP_COUNT + 2) + " must be of type Integer.");
        assertTextPresent(TEST_ASSAY_SET_PROP_NAME + (TEST_ASSAY_SET_PREDEFINED_PROP_COUNT + 3) + " must be of type Date and Time.");
        setFormElement(TEST_ASSAY_SET_PROP_NAME + (TEST_ASSAY_SET_PREDEFINED_PROP_COUNT + 1), TEST_ASSAY_SET_PROPERTIES[1]);
        setFormElement(TEST_ASSAY_SET_PROP_NAME + (TEST_ASSAY_SET_PREDEFINED_PROP_COUNT + 2), TEST_ASSAY_SET_PROPERTIES[2]);
        setFormElement(TEST_ASSAY_SET_PROP_NAME + (TEST_ASSAY_SET_PREDEFINED_PROP_COUNT + 3), TEST_ASSAY_SET_PROPERTIES[3]);

        //ensure that the target study drop down contains Study 1 and Study 2 only and not Study 3
        //(labtech1 does not have read perms to Study 3)
        assertTextPresent(getTargetStudyOptionText(TEST_ASSAY_FLDR_STUDY1));
        assertTextPresent(getTargetStudyOptionText(TEST_ASSAY_FLDR_STUDY2));
        assertTextNotPresent(getTargetStudyOptionText(TEST_ASSAY_FLDR_STUDY3));

        //select Study2 as the target study (note that PI is not an Editor in this study so we can test for override case)
        selenium.select("//select[@name='targetStudy']", getTargetStudyOptionText(TEST_ASSAY_FLDR_STUDY2));

        clickButton("Next");

        log("Check properties set.");
        assertTextPresent(TEST_ASSAY_SET_PROPERTIES[1]);
        assertTextPresent(TEST_ASSAY_SET_PROPERTIES[2]);
        assertTextPresent(TEST_ASSAY_SET_PROPERTIES[3]);
        assertTextPresent(TEST_ASSAY_SET_PROPERTIES[0]);

        log("Run properties and data");
        clickButton("Save and Finish");
        assertTextPresent(TEST_ASSAY_RUN_PROP_NAME + "0 is required and must be of type Text (String).");
        selenium.type("name", TEST_RUN1);
		selenium.type("comments", TEST_RUN1_COMMENTS);
        setFormElement(TEST_ASSAY_RUN_PROP_NAME + "0", TEST_ASSAY_RUN_PROP1);
        clickButton("Save and Finish");
        assertTextPresent("Data file contained zero data rows");
        selenium.click("//input[@value='textAreaDataProvider']");
        selenium.type("TextAreaDataCollector.textArea", TEST_RUN1_DATA1);
        clickButton("Save and Finish");

        selenium.click("//input[@value='textAreaDataProvider']");
        selenium.type("TextAreaDataCollector.textArea", TEST_RUN1_DATA2);
        clickButton("Save and Finish");
        assertTextPresent("There are errors in the uploaded data: VisitID must be of type Number (Double)");
        Assert.assertEquals(TEST_RUN1, selenium.getValue("name"));
        Assert.assertEquals(TEST_RUN1_COMMENTS, selenium.getValue("comments"));
        selenium.click("//input[@value='textAreaDataProvider']");
        selenium.type("TextAreaDataCollector.textArea", TEST_RUN1_DATA3);
        clickButton("Save and Import Another Run");
        assertTextPresent("There are errors in the uploaded data: " + TEST_ASSAY_DATA_PROP_NAME + "6 is required. ");

        selenium.click("//input[@value='textAreaDataProvider']");
        selenium.type("TextAreaDataCollector.textArea", TEST_RUN1_DATA4);
        clickButton("Save and Import Another Run");

        Assert.assertEquals("", selenium.getValue("name"));
        Assert.assertEquals("", selenium.getValue("comments"));
        selenium.type("name", TEST_RUN2);
		selenium.type("comments", TEST_RUN2_COMMENTS);
        selenium.type("TextAreaDataCollector.textArea", TEST_RUN2_DATA1);
        clickButton("Save and Finish");

        log("Check out the data for one of the runs");
        assertNoLabkeyErrors();
        assertTextPresent(TEST_ASSAY + " Runs");
        assertTextPresent(TEST_ASSAY_RUN_PROP1);
        assertTextPresent(TEST_ASSAY_SET_PROPERTIES[0]);
        assertTextPresent(TEST_ASSAY_SET_PROPERTIES[3]);
        clickAndWait(Locator.linkWithText(TEST_RUN1));
        isTextPresent("2.0");
        assertTextNotPresent("7.0");
        // Make sure that our specimen IDs resolved correctly
        assertTextPresent("AAA07XSF-02");
        assertTextPresent("999320885");
        assertTextPresent("301");
        assertTextPresent("AAA07XK5-05");
        assertTextPresent("999320812");
        assertTextPresent("601");
        assertTextPresent(TEST_ASSAY_DATA_PROP_NAME + "4");
        assertTextPresent(TEST_ASSAY_DATA_PROP_NAME + "5");
        assertTextPresent(TEST_ASSAY_DATA_PROP_NAME + "6");
        assertTextPresent("2000-06-06");
        assertTextPresent("0.0");
        assertTextPresent("f");
        assertTextPresent(ALIASED_DATA);

        _customizeViewsHelper.openCustomizeViewPanel();
        _customizeViewsHelper.addCustomizeViewColumn("SpecimenID/GlobalUniqueId", "Specimen Global Unique Id");
        _customizeViewsHelper.addCustomizeViewColumn("SpecimenID/Specimen/PrimaryType", "Specimen Specimen Primary Type");
        _customizeViewsHelper.addCustomizeViewColumn("SpecimenID/AssayMatch", "Specimen Assay Match");
        _customizeViewsHelper.removeCustomizeViewColumn("Run/testAssayRunProp1");
        _customizeViewsHelper.removeCustomizeViewColumn("Run/Batch/testAssaySetProp2");
        _customizeViewsHelper.removeCustomizeViewColumn("testAssayDataProp4");
        _customizeViewsHelper.applyCustomView();

        assertTextPresent("Blood (Whole)", 4);

        Locator.XPathLocator trueLocator = Locator.xpath("//table[contains(@class, 'labkey-data-region')]//td[text() = 'true']");
        int totalTrues = getXpathCount(trueLocator);
        Assert.assertEquals(4, totalTrues);

        setFilter("Data", "SpecimenID", "Starts With", "AssayTestControl");

        // verify that there are no trues showing for the assay match column that were filtered out
        totalTrues = getXpathCount(trueLocator);
        Assert.assertEquals(0, totalTrues);

        log("Check out the data for all of the runs");
        clickAndWait(Locator.linkWithText("view results"));
        clearAllFilters("Data", "SpecimenID");
        assertTextPresent("7.0");
        assertTextPresent("18");

        assertTextPresent("Blood (Whole)", 7);

        Locator.XPathLocator falseLocator = Locator.xpath("//table[contains(@class, 'labkey-data-region')]//td[text() = 'false']");
        int totalFalses = getXpathCount(falseLocator);
        Assert.assertEquals(3, totalFalses);

        setFilter("Data", "SpecimenID", "Does Not Start With", "BAQ");

        // verify the falses have been filtered out
        totalFalses = getXpathCount(falseLocator);
        Assert.assertEquals(0, totalFalses);

        //Check to see that the bad specimen report includes the bad assay results and not the good ones
        //The report doesn't have top level UI (use a wiki) so just jump there.
        beginAt("specimencheck/" + TEST_ASSAY_PRJ_SECURITY + "/assayReport.view");
        waitForText("Global Specimen ID", 10000);
        waitForElement(Locator.linkWithText("BAQ00051-09"), 10000);
        assertLinkPresentWithText("BAQ00051-09");
        assertLinkPresentWithText("BAQ00051-08");
        assertLinkPresentWithText("BAQ00051-11");
        assertTextNotPresent("AAA");
        stopImpersonating();
        clickFolder(TEST_ASSAY_PRJ_SECURITY);
    } //uploadRuns()

    /**
     * Impersonates the PI user and publishes the data previous uploaded.
     * This will also verify that the PI cannot publish to studies for which
     * the PI does not have Editor permissions.
     */
    @LogMethod(category = LogMethod.MethodType.MIXEDPURPOSE)
    private void publishData()
    {
        log("Prepare visit map to check PTID counts in study navigator.");
        clickAndWait(Locator.linkWithText(TEST_ASSAY_FLDR_STUDY1));
        clickAndWait(Locator.linkWithText("Manage"));
        clickAndWait(Locator.linkWithText("Manage Visits"));
        clickAndWait(Locator.linkWithText("Import Visit Map"));
        setFormElement("content", "301-302|X|Test Visit");
        clickButton("Import");

        log("Publishing the data as the PI");

        //impersonate the PI
        impersonate(TEST_ASSAY_USR_PI1);
        clickFolder(TEST_ASSAY_PRJ_SECURITY);

        //select the Lab1 folder and view all the data for the test assay
        clickFolder(TEST_ASSAY_FLDR_LAB1);
        clickAndWait(Locator.linkWithText(TEST_ASSAY));
        clickAndWait(Locator.linkWithText("view results"));

        //select all the data rows and click publish
        selenium.click(".toggle");
        clickButton("Copy to Study");

        //the target study selected before was Study2, but the PI is not an editor there
        //so ensure that system has correctly caught this fact and now asks the PI to
        //select a different study, and lists only those studies in which the PI is
        //an editor

        //ensure warning
        assertTextPresent("WARNING: You do not have permissions to copy to one or more of the selected run's associated studies.");

        //ensure that Study2 and Study 3 are not available in the target study drop down
        assertElementNotPresent(Locator.xpath("//select[@name='targetStudy']/option[.='" +
                getTargetStudyOptionText(TEST_ASSAY_FLDR_STUDY2) + "']"));
        assertElementNotPresent(Locator.xpath("//select[@name='targetStudy']/option[.='" +
                getTargetStudyOptionText(TEST_ASSAY_FLDR_STUDY3) + "']"));

        //Study1 is the only one left, so it should be there and already be selected
        assertElementPresent(Locator.xpath("//select[@name='targetStudy']/option[.='" +
                getTargetStudyOptionText(TEST_ASSAY_FLDR_STUDY1) + "']"));

        // Make sure the selected study is Study1
        selectOptionByText(Locator.xpath("//select[@name='targetStudy']"), getTargetStudyOptionText(TEST_ASSAY_FLDR_STUDY1));

        clickButton("Next");
        assertTextPresent("Copy to " + TEST_ASSAY_FLDR_STUDY1 + " Study: Verify Results");

        setFormElement("visitId", "301.5");
        clickButton("Copy to Study");

        log("Verifying that the data was published");
        _customizeViewsHelper.openCustomizeViewPanel();
        _customizeViewsHelper.addCustomizeViewColumn("QCState", "QC State");
        _customizeViewsHelper.applyCustomView();
        assertTextPresent("Pending Review");
        assertTextPresent("a");
        assertTextPresent(TEST_RUN1_COMMENTS);
        assertTextPresent("2000-01-01");
        clickTab("Overview");
        clickAndWait(Locator.linkWithText("Study Navigator"));

        log("Test participant counts and row counts in study overview");
        String[] row2 = new String[]{TEST_ASSAY, "7", "1", "1", "1", "1", "1", "2"};
        assertTableRowsEqual("studyOverview", 1, new String[][]{row2});
        // Manually click the checkbox -- normal checkCheckbox() method doesn't seem to work for checkbox that reloads using onchange event
        click(Locator.checkboxByNameAndValue("visitStatistic", "RowCount"));
        waitForPageToLoad();
        row2 = new String[]{TEST_ASSAY, "7 / 8", "1 / 1", "1 / 1", "1 / 1", "1 / 1", "1 / 1", "2 / 3"};
        assertTableRowsEqual("studyOverview", 1, new String[][]{row2});
        uncheckCheckbox("visitStatistic", "ParticipantCount");
        waitForPageToLoad();
        row2 = new String[]{TEST_ASSAY, "8", "1", "1", "1", "1", "1", "3"};
        assertTableRowsEqual("studyOverview", 1, new String[][]{row2});

        clickAndWait(Locator.linkWithText("8"));

        assertTextPresent("301.0");
        assertTextPresent("9.0");
        assertTextPresent("8.0");
        assertLinkPresentWithTextCount("999320396", 2);
        assertLinkPresentWithTextCount("999320885", 1);
        assertTextPresent(TEST_RUN1_COMMENTS);
        assertTextPresent(TEST_RUN2_COMMENTS);
        assertTextPresent(TEST_RUN1);
        assertTextPresent(TEST_RUN2);
        assertTextPresent("2000-06-06");
        assertTextPresent(TEST_ASSAY_RUN_PROP1);
        assertTextPresent("18");

        // test recall
        clickFolder(TEST_ASSAY_FLDR_LAB1);
        clickAndWait(Locator.linkWithText(TEST_ASSAY));
        clickAndWait(Locator.linkWithText("view copy-to-study history"));

        // Set a filter so that we know we're recalling SecondRun
        setFilter("audit", "Comment", "Starts With", "3 row(s) were copied to a study from the assay");
        clickAndWait(Locator.linkWithText("details"));
        checkCheckbox(Locator.checkboxByName(".toggle"));
        clickButton("Recall Rows", 0);
        getConfirmationAndWait();
        assertTextPresent("row(s) were recalled to the assay: " + TEST_ASSAY);

        // Set a filter so that we know we're looking at the copy event for SecondRun again
        setFilter("audit", "Comment", "Starts With", "3 row(s) were copied to a study from the assay");

        // verify audit entry was adjusted
        clickAndWait(Locator.linkWithText("details"));
        assertTextPresent("All rows that were previously copied in this event have been recalled");

        stopImpersonating();
    } //publishData()

    /**
     * Designed to test automatic timepoint generation when copying to a date based study.
     * Most tests of timepoint matching are covered by separate junit tests; however,
     * this will create 1 pre-existing timepoint, and when copying data this timepoint should be
     * chosen for appropriate records.
     */
    @LogMethod(category = LogMethod.MethodType.MIXEDPURPOSE)
    private void publishDataToDateBasedStudy()
    {
        log("Prepare visit map to check PTID counts in study navigator.");

        clickFolder(TEST_ASSAY_PRJ_SECURITY);
        clickFolder(TEST_ASSAY_FLDR_STUDY3);

        clickAndWait(Locator.linkWithText("Manage"));
        clickAndWait(Locator.linkWithText("Manage Timepoints"));
        clickAndWait(Locator.linkWithText("Create New Timepoint"));
        setFormElement("label", "Preexisting Timepoint");
        setFormElement("sequenceNumMin", "50");
        setFormElement("sequenceNumMax", "89");
        setFormElement("typeCode", "Screening");

        clickButton("Save");

        //select the Lab1 folder and view all the data for the test assay
        clickFolder(TEST_ASSAY_FLDR_LAB1);
        clickAndWait(Locator.linkWithText(TEST_ASSAY));
        clickAndWait(Locator.linkWithText("view results"));

        //select all the data rows and click publish
        selenium.click(".toggle");
        clickButton("Copy to Study");

        checkCheckbox(Locator.xpath("//td//input[@type='checkbox']"));

        // Make sure the selected study is Study3
        selectOptionByText(Locator.xpath("//select[@name='targetStudy']"), getTargetStudyOptionText(TEST_ASSAY_FLDR_STUDY3));

        clickButton("Next");
        assertTextPresent("Copy to " + TEST_ASSAY_FLDR_STUDY3 + " Study: Verify Results");

        //populate initial set of values and verify the timepoint preview column
        String[] dates = new String[]{"2000-02-02", "2000-03-03", "2000-04-04", "2000-05-05", "2000-06-06", "2000-01-01", "2000-02-02", "2000-03-03"};
        int idx = 1;
        for (String d : dates)
        {
            setFormElement(Locator.xpath("(//input[@name='date'])[" + idx + "]"), d);
            idx++;
        }

        setFormElement(Locator.xpath("(//input[@name='participantId'])[1]"), "new1");
        setFormElement(Locator.xpath("(//input[@name='participantId'])[2]"), "new2");
        setFormElement(Locator.xpath("(//input[@name='participantId'])[3]"), "new3");
        setFormElement(Locator.xpath("(//input[@name='participantId'])[4]"), "new4");

        clickButton("Re-Validate");
        waitForPageToLoad();

        //validate timepoints:
        assertElementPresent(Locator.xpath("//td[text()='Day 32 - 39' and following-sibling::td[text()='AAA07XMC-02' and following-sibling::td[text()='301.0']]]"));
        assertElementPresent(Locator.xpath("//td[text()='Preexisting Timepoint' and following-sibling::td[text()='AAA07XMC-04' and following-sibling::td[not(text())]]]"));
        assertElementPresent(Locator.xpath("//td[text()='Day 90 - 95' and following-sibling::td[text()='AAA07XSF-02' and following-sibling::td[not(text())]]]"));

        assertElementPresent(Locator.xpath("//td[text()='Day 120 - 127' and following-sibling::td[text()='AssayTestControl1' and following-sibling::td[text()='5.0']]]"));
        assertElementPresent(Locator.xpath("//td[text()='Day 152 - 159' and following-sibling::td[text()='AssayTestControl2' and following-sibling::td[text()='6.0']]]"));
        assertElementPresent(Locator.xpath("//td[text()='Day 0 - 7' and following-sibling::td[text()='BAQ00051-09' and following-sibling::td[text()='7.0']]]"));
        assertElementPresent(Locator.xpath("//td[text()='Day 32 - 39' and following-sibling::td[text()='BAQ00051-08' and following-sibling::td[text()='8.0']]]"));
        assertElementPresent(Locator.xpath("//td[text()='Preexisting Timepoint' and following-sibling::td[text()='BAQ00051-11' and following-sibling::td[text()='9.0']]]"));
        clickButton("Copy to Study");

        log("Verifying that the data was published");
        assertTextPresent("a");
        assertTextPresent(TEST_RUN1_COMMENTS);
        assertTextPresent("2000-01-01");
        clickTab("Overview");
        clickAndWait(Locator.linkWithText("Study Navigator"));

        log("Test participant counts and row counts in study overview");
        String[] row2 = new String[]{TEST_ASSAY, "8", "1", "2", "2", "1", "1", "1"};
        assertTableRowsEqual("studyOverview", 1, new String[][]{row2});
        // Manually click the checkbox -- normal checkCheckbox() method doesn't seem to work for checkbox that reloads using onchange event
        click(Locator.checkboxByNameAndValue("visitStatistic", "RowCount"));
        waitForPageToLoad();
        row2 = new String[]{TEST_ASSAY, "8 / 8", "1 / 1", "2 / 2", "2 / 2", "1 / 1", "1 / 1", "1 / 1"};
        assertTableRowsEqual("studyOverview", 1, new String[][]{row2});

        log("Test that correct timepoints were created");

        clickTab("Overview");
        clickAndWait(Locator.linkWithText("Manage Study"));
        clickAndWait(Locator.linkWithText("Manage Timepoints"));
        Assert.assertTrue(isTextPresent("Day 0 - 7"));
        Assert.assertTrue(isTextPresent("Day 32 - 39"));
        Assert.assertTrue(isTextPresent("Day 90 - 95"));
        Assert.assertTrue(isTextPresent("Day 120 - 127"));
        Assert.assertTrue(isTextPresent("Day 152 - 159"));
    } //publishDataToDateBasedStudy()


    /**
     * Designed to test automatic timepoint generation when copying to a date based study.
     * Most tests of timepoint matching are covered by separate junit tests; however,
     * this will create 1 pre-existing timepoint, and when copying data this timepoint should be
     * chosen for appropriate records.
     */
    @LogMethod(category = LogMethod.MethodType.MIXEDPURPOSE)
    private void publishDataToVisitBasedStudy()
    {
        log("Prepare visit map to check PTID counts in study navigator.");

        clickFolder(TEST_ASSAY_PRJ_SECURITY);
        clickFolder(TEST_ASSAY_FLDR_STUDY2);

        clickAndWait(Locator.linkWithText("Manage"));
        clickAndWait(Locator.linkWithText("Manage Visits"));
        clickAndWait(Locator.linkWithText("Import Visit Map"));
        setFormElement("content",
            "6-13|X|Test Visit1\n" +
            "50-70|X|Test Visit2\n" +
            "302-303|X|Test Visit3\n"
        );
        clickButton("Import");

        //select the Lab1 folder and view all the data for the test assay
        clickFolder(TEST_ASSAY_FLDR_LAB1);
        clickAndWait(Locator.linkWithText(TEST_ASSAY));
        clickAndWait(Locator.linkWithText("view results"));

        //select all the data rows and click publish
        selenium.click(".toggle");
        clickButton("Copy to Study");

        checkCheckbox(Locator.xpath("//td//input[@type='checkbox']"));

        // Make sure the selected study is Study2
        selectOptionByText(Locator.xpath("//select[@name='targetStudy']"), getTargetStudyOptionText(TEST_ASSAY_FLDR_STUDY2));

        clickButton("Next");
        assertTextPresent("Copy to " + TEST_ASSAY_FLDR_STUDY2 + " Study: Verify Results");

        //populate initial set of values and verify the timepoint preview column
        String[] visits = new String[]{"302", "33", "4", "70"};
        int idx = 1;
        for (String v : visits)
        {
            setFormElement(Locator.xpath("(//input[@name='visitId'])[" + idx + "]"), v);
            idx++;
        }

        setFormElement(Locator.xpath("(//input[@name='participantId'])[1]"), "new1");
        setFormElement(Locator.xpath("(//input[@name='participantId'])[2]"), "new2");
        setFormElement(Locator.xpath("(//input[@name='participantId'])[3]"), "new3");
        setFormElement(Locator.xpath("(//input[@name='participantId'])[4]"), "new4");

        clickButton("Re-Validate");
        waitForPageToLoad();

        //validate timepoints:
        Assert.assertTrue(isElementPresent(Locator.xpath("//td[text()='Test Visit3' and following-sibling::td[text()='AAA07XMC-02']]")));
        Assert.assertTrue(isElementPresent(Locator.xpath("//td[text()='33' and following-sibling::td[text()='AAA07XMC-04']]")));
        Assert.assertTrue(isElementPresent(Locator.xpath("//td[text()='4' and following-sibling::td[text()='AAA07XSF-02']]")));

        Assert.assertTrue(isElementPresent(Locator.xpath("//td[text()='Test Visit2' and following-sibling::td[text()='AssayTestControl1']]")));
        Assert.assertTrue(isElementPresent(Locator.xpath("//td[text()='Test Visit1' and following-sibling::td[text()='AssayTestControl2']]")));
        Assert.assertTrue(isElementPresent(Locator.xpath("//td[text()='Test Visit1' and following-sibling::td[text()='BAQ00051-09']]")));
        Assert.assertTrue(isElementPresent(Locator.xpath("//td[text()='Test Visit1' and following-sibling::td[text()='BAQ00051-08']]")));
        Assert.assertTrue(isElementPresent(Locator.xpath("//td[text()='Test Visit1' and following-sibling::td[text()='BAQ00051-11']]")));

        clickButton("Copy to Study");

        log("Verifying that the data was published");
        assertTextPresent("a");
        assertTextPresent(TEST_RUN1_COMMENTS);
        assertTextPresent("2000-01-01");
        clickTab("Overview");
        clickAndWait(Locator.linkWithText("Study Navigator"));

        log("Test participant counts and row counts in study overview");
        String[] row2 = new String[]{TEST_ASSAY, "8", "", "", "", "", "", "", "1", "", "", "4", "", "", "", "", "1", "1", "", "", "", "1", "", "", "", "", ""};
        assertTableRowsEqual("studyOverview", 1, new String[][]{row2});
        // Manually click the checkbox -- normal checkCheckbox() method doesn't seem to work for checkbox that reloads using onchange event
        click(Locator.checkboxByNameAndValue("visitStatistic", "RowCount"));
        waitForPageToLoad();
        row2 = new String[]{TEST_ASSAY, "8 / 8", "", "", "", "", "", "", "1 / 1", "", "", "4 / 4", "", "", "", "", "1 / 1", "1 / 1", "", "", "", "1 / 1", "", "", "", "", ""};
        assertTableRowsEqual("studyOverview", 1, new String[][]{row2});

        log("Test that correct timepoints were created");

        clickTab("Overview");
        clickAndWait(Locator.linkWithText("Manage Study"));
        clickAndWait(Locator.linkWithText("Manage Visits"));
        Assert.assertTrue(isTextPresent("Test Visit1"));
        Assert.assertTrue(isTextPresent("6.0-13.0"));
        Assert.assertTrue(isTextPresent("Test Visit2"));
        Assert.assertTrue(isTextPresent("50.0-70.0"));
        Assert.assertTrue(isTextPresent("Test Visit3"));
        Assert.assertTrue(isTextPresent("302.0-303.0"));
    } //publishDataToVisitBasedStudy()

    /**
     * Tests editing of an existing assay definition
     */
    @LogMethod(category = LogMethod.MethodType.MIXEDPURPOSE)
    private void editAssay()
    {
        log("Testing edit and delete and assay definition");

        clickFolder(TEST_ASSAY_PRJ_SECURITY);

        clickAndWait(Locator.linkWithText(TEST_ASSAY));
        clickEditAssayDesign(false);
        waitForElement(Locator.xpath(getPropertyXPath("Data Fields") + "//td//input[@name='ff_name5']"), WAIT_FOR_JAVASCRIPT);
        _listHelper.setColumnName(getPropertyXPath("Data Fields"), 5, TEST_ASSAY_DATA_PROP_NAME + "edit");
        _listHelper.setColumnLabel(getPropertyXPath("Data Fields"), 5, TEST_ASSAY_DATA_PROP_NAME + "edit");
        deleteField("Data Fields", 4);
        clickButton("Save", 0);
        waitForText("Save successful.", WAIT_FOR_JAVASCRIPT);

        //ensure that label has changed in run data in Lab 1 folder
        clickFolder(TEST_ASSAY_FLDR_LAB1);
        clickAndWait(Locator.linkWithText(TEST_ASSAY));
        clickAndWait(Locator.linkWithText(TEST_RUN1));
        assertTextPresent(TEST_ASSAY_DATA_PROP_NAME + "edit");
        assertTextNotPresent(TEST_ASSAY_DATA_PROP_NAME + 4);

        AuditLogTest.verifyAuditEvent(this, AuditLogTest.ASSAY_AUDIT_EVENT, AuditLogTest.COMMENT_COLUMN, "were copied to a study from the assay: " + TEST_ASSAY, 5);
    } //editAssay()

    @LogMethod(category = LogMethod.MethodType.VERIFICATION)
    private void viewCrossFolderData()
    {
        log("Testing cross-folder data");

        clickFolder(TEST_ASSAY_PRJ_SECURITY);

        // Remove so we can easily click on the Views menu for the right web part
        removeWebPart("Assay List");

        addWebPart("Assay Runs");
        selectOptionByText("viewProtocolId", "General: " + TEST_ASSAY);
        // assay runs has a details page that needs to be submitted
        clickButton("Submit", defaultWaitForPage);

        // Set the container filter to include subfolders
        clickMenuButton("Views", "Folder Filter", "Current folder and subfolders");

        assertTextPresent("FirstRun");
        assertTextPresent("SecondRun");

        log("Setting the customized view to include subfolders");
        _customizeViewsHelper.openCustomizeViewPanel();

        _customizeViewsHelper.clipFolderFilter();
        _customizeViewsHelper.saveCustomView("");

        assertTextPresent("FirstRun");
        assertTextPresent("SecondRun");

        log("Testing select all data and view");
        clickCheckbox(".toggle");
        clickButton("Show Results", defaultWaitForPage);
        verifySpecimensPresent(3, 2, 3);

        log("Testing clicking on a run");
        clickFolder(TEST_ASSAY_PRJ_SECURITY);
        clickAndWait(Locator.linkWithText("FirstRun"));
        verifySpecimensPresent(3, 2, 0);

        clickAndWait(Locator.linkWithText("view results"));
        clearAllFilters("Data", "SpecimenID");
        verifySpecimensPresent(3, 2, 3);

        log("Testing assay-study linkage");
        clickAndWait(Locator.linkWithText(TEST_ASSAY_FLDR_STUDY1));
        addWebPart("Datasets");
        clickAndWait(Locator.linkWithText(TEST_ASSAY));
        clickButton("View Source Assay", defaultWaitForPage);

        assertTextPresent("FirstRun");
        assertTextPresent("SecondRun");

        clickAndWait(Locator.linkWithText("FirstRun"));
        verifySpecimensPresent(3, 2, 0);

        clickAndWait(Locator.linkWithText("view results"));
        clearAllFilters("Data", "SpecimenID");
        verifySpecimensPresent(3, 2, 3);

        // Verify that the correct copied to study column is present
        assertTextPresent("Copied to Study 1 Study");

        log("Testing copy to study availability");
        clickFolder(TEST_ASSAY_PRJ_SECURITY);
        clickAndWait(Locator.linkWithText("SecondRun"));

        clickCheckbox(".toggle");
        clickButton("Copy to Study", defaultWaitForPage);
        clickButton("Next", defaultWaitForPage);

        verifySpecimensPresent(0, 0, 3);

        clickButton("Cancel", defaultWaitForPage);

        clickFolder(TEST_ASSAY_PRJ_SECURITY);

        // Add it back to the portal page
        addWebPart("Assay List");
    }

    @LogMethod(category = LogMethod.MethodType.VERIFICATION)
    private void verifyStudyList()
    {
        clickAndWait(Locator.linkWithText(TEST_ASSAY_FLDR_STUDIES));
        addWebPart("Study List");
        assertLinkPresentWithText(TEST_ASSAY_FLDR_STUDY1 + " Study");
        assertLinkPresentWithText(TEST_ASSAY_FLDR_STUDY2 + " Study");
        assertLinkPresentWithText(TEST_ASSAY_FLDR_STUDY3 + " Study");
        clickWebpartMenuItem("Studies", "Customize");

        //verify grid view
        selectOptionByText(Locator.name("displayType"), "Grid");
        clickButton("Submit");
        assertLinkNotPresentWithText("edit");

        //edit study properties
        clickAndWait(Locator.linkWithText(TEST_ASSAY_FLDR_STUDY1 + " Study"));
        click(Locator.tagWithAttribute("img", "title", "Edit"));
        waitForElement(Locator.name("Investigator"), WAIT_FOR_JAVASCRIPT);
        setFormElement("Investigator", INVESTIGATOR);
        setFormElement("Grant", GRANT);
        setFormElement("Description", DESCRIPTION);
        clickButton("Submit");

        //verify study properties (grid view)
        clickAndWait(Locator.linkWithText(TEST_ASSAY_FLDR_STUDIES));
        DataRegionTable table = new DataRegionTable("qwpStudies", this, false);
        Assert.assertEquals("Studies not sorted correctly.", TEST_ASSAY_FLDR_STUDY1 + " Study", table.getDataAsText(0, "Label"));
        Assert.assertEquals("Failed to set study investigator.", INVESTIGATOR, table.getDataAsText(0, "Investigator"));
        Assert.assertEquals("Failed to set study grant.", GRANT, table.getDataAsText(0, "Grant"));
        Assert.assertEquals("Failed to set study description.", DESCRIPTION, table.getDataAsText(0, "Description"));

        //verify study properties (details view)
        clickWebpartMenuItem("Studies", "Customize");
        selectOptionByText(Locator.name("displayType"), "Details");
        clickButton("Submit");
        assertTextPresent(INVESTIGATOR, DESCRIPTION);
        assertTextNotPresent(GRANT, TEST_ASSAY_FLDR_STUDY1 + " Study tracks data"); //Old description
    }

    private void verifySpecimensPresent(int aaa07Count, int controlCount, int baq00051Count)
    {
        assertTextPresent("AAA07", aaa07Count);
        assertTextPresent("AssayTestControl", controlCount);
        assertTextPresent("BAQ00051", baq00051Count);
    }

    protected boolean isFileUploadTest()
    {
        return true;
    }
}
