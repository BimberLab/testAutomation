/*
 * Copyright (c) 2012 LabKey Corporation
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
 * User: tchadick
 * Date: 10/17/12
 * Time: 3:44 PM
 */
public class ElisaAssayTest extends ElispotAssayTest
{
    private final static String TEST_ASSAY_PRJ_ELISA = "ELISA Test Verify Project";
    private final static String TEST_ASSAY_FLDR_NAB = "ELISA";

    protected static final String TEST_ASSAY_ELISA = "TestAssayELISA";
    protected static final String TEST_ASSAY_ELISA_DESC = "Description for ELISA assay";

    protected final static String TEST_ASSAY_USR_NAB_READER = "nabreader1@security.test";
    private final static String TEST_ASSAY_GRP_NAB_READER = "Nab Dataset Reader";   //name of Nab Dataset Readers group

    protected final String TEST_ASSAY_ELISA_FILE1 = getLabKeyRoot() + "/sampledata/Elisa/biotek_01.xlsx";
    protected final String TEST_ASSAY_ELISA_FILE2 = getLabKeyRoot() + "/sampledata/Elisa/biotek_02.xls";
    protected final String TEST_ASSAY_ELISA_FILE3 = getLabKeyRoot() + "/sampledata/Elisa/biotek_03.xls";
    protected final String TEST_ASSAY_ELISA_FILE4 = getLabKeyRoot() + "/sampledata/Elisa/biotek_04.xls";

    private static final String PLATE_TEMPLATE_NAME = "ELISAAssayTest Template";

    public String getAssociatedModuleDirectory()
    {
        return "server/modules/assay";
    }

    @Override
    protected String getProjectName()
    {
        return TEST_ASSAY_PRJ_ELISA;
    }
    
    @LogMethod
    protected void runUITests()
    {
        log("Starting ELISA Assay BVT Test");

        //revert to the admin user
        revertToAdmin();

        log("Testing ELISA Assay Designer");

        // set up a scripting engine to run a java transform script
        prepareProgrammaticQC();

        //create a new test project
        _containerHelper.createProject(TEST_ASSAY_PRJ_ELISA, null);

        //setup a pipeline for it
        setupPipeline(TEST_ASSAY_PRJ_ELISA);

        //add the Assay List web part so we can create a new ELISA assay
        clickFolder(TEST_ASSAY_PRJ_ELISA);
        addWebPart("Assay List");

        //create a new ELISA template
        createTemplate();

        //create a new ELISA assay
        clickFolder(TEST_ASSAY_PRJ_ELISA);
        clickButton("Manage Assays");
        clickButton("New Assay Design");
        checkRadioButton("providerName", "ELISA");
        clickButton("Next");

        log("Setting up ELISA assay");
        waitForElement(Locator.xpath("//input[@id='AssayDesignerName']"), WAIT_FOR_JAVASCRIPT);
        setFormElement(Locator.id("AssayDesignerName"), TEST_ASSAY_ELISA);

        selectOptionByValue(Locator.xpath("//select[@id='plateTemplate']"), PLATE_TEMPLATE_NAME);
        setFormElement(Locator.id("AssayDesignerDescription"), TEST_ASSAY_ELISA_DESC);


        // set the specimenId field default value to be : last entered
        Locator specimenField = Locator.xpath("//td[@class='labkey-wp-title-left' and text() ='Sample Fields']/../..//div[@id='name1']");
        click(specimenField);
        click(Locator.xpath("//td[@class='labkey-wp-title-left' and text() ='Sample Fields']/../..//span[text()='Advanced']"));
        selectOptionByValue(Locator.xpath("//td[@class='labkey-wp-title-left' and text() ='Sample Fields']/../..//select[@class='gwt-ListBox']"), "LAST_ENTERED");

        clickButton("Save", 0);
        waitForText("Save successful.", 20000);
//
        clickFolder(TEST_ASSAY_PRJ_ELISA);
        clickLinkWithText("Assay List");
        clickFolder(TEST_ASSAY_ELISA);

        log("Uploading ELISA Runs");
        clickButton("Import Data");
        clickButton("Next");

        uploadFile(TEST_ASSAY_ELISA_FILE1, "A", "Save and Import Another Run", true, 2, 6);
        assertTextPresent("Upload successful.");
        uploadFile(TEST_ASSAY_ELISA_FILE2, "B", "Save and Import Another Run", true, 2, 6);
        assertTextPresent("Upload successful.");
        uploadFile(TEST_ASSAY_ELISA_FILE3, "C", "Save and Import Another Run", true, 2, 6);
        assertTextPresent("Upload successful.");
        uploadFile(TEST_ASSAY_ELISA_FILE4, "D", "Save and Finish", true, 2, 6);

//        assertELISAData();
    }

    protected void createTemplate()
    {
        clickButton("Manage Assays");
        clickButton("Configure Plate Templates");
        clickLinkWithText("new 96 well (8x12) ELISA default template");
        Locator nameField = Locator.id("templateName");
        waitForElement(nameField, WAIT_FOR_JAVASCRIPT);
        setFormElement(nameField, PLATE_TEMPLATE_NAME);

        clickButton("Save & Close");
        waitForText(PLATE_TEMPLATE_NAME);
    }

    protected void uploadFile(String filePath, String uniqueifier, String finalButton, boolean testPrepopulation, int startSpecimen, int lastSpecimen)
    {
        for (int i = startSpecimen; i <= lastSpecimen; i++)
        {
            Locator specimenLocator = Locator.name("specimen" + (i) + "_SpecimenID");
            Locator participantLocator = Locator.name("specimen" + (i) + "_ParticipantID");

            // test for prepopulation of specimen form element values
//            if (testPrepopulation)
//                assertFormElementEquals(participantLocator, "ptid " + (i) + " " + uniqueifier);
            setFormElement(specimenLocator, "specimen " + (i) + " " + uniqueifier);
            setFormElement(participantLocator, "ptid " + (i) + " " + uniqueifier);

            setFormElement(Locator.name("specimen" + (i) + "_VisitID"), "" + (i));
        }

        File file1 = new File(filePath);
        setFormElement("__primaryFile__", file1);
        clickButton("Next");

        String allErrors = "";
        if (!testPrepopulation)
        {
            clickButton(finalButton); // cause errors
            clickLinkWithText("Too many errors to display (click to show all).\n");
            _extHelper.waitForExtDialog("All Errors");
            allErrors = Locator.css(".x-window-body").findElement(_driver).getText();
            _extHelper.clickExtButton("All Errors", "Close", 0);
            _extHelper.waitForExt3MaskToDisappear(WAIT_FOR_JAVASCRIPT);
        }

        String[] letters = {"A","B","C","D","E","F","G","H"};
        for (int i = 0; i <= 7; i++)
        {
            setFormElement(Locator.name(letters[i].toLowerCase()+"1"+letters[i]+"2_Concentration"), "" + (i + 1));

            if (!testPrepopulation)
            {
                Assert.assertTrue("Missing error for well group " + letters[i], allErrors.contains("Value for well group: " + letters[i] + "1-" + letters[i] + "2 cannot be blank."));
            }
        }

        clickButton(finalButton);
    }

    /**
     * Cleanup entry point.
     * @param afterTest
     */
    protected void doCleanup(boolean afterTest)
    {
        revertToAdmin();
        try {
            deleteProject(TEST_ASSAY_PRJ_ELISA);
            deleteEngine();
        }
        catch(Throwable T) {}

        deleteDir(getTestTempDir());
    } //doCleanup()
}
