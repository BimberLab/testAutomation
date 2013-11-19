/*
 * Copyright (c) 2011-2013 LabKey Corporation
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
import org.labkey.test.BaseWebDriverTest;
import org.labkey.test.Locator;
import org.labkey.test.TestTimeoutException;
import org.labkey.test.categories.DailyB;
import org.labkey.test.categories.Specimen;
import org.labkey.test.categories.Study;
import org.labkey.test.util.CustomizeViewsHelper;
import org.labkey.test.util.DataRegionTable;
import org.labkey.test.util.FileBrowserHelperWD;

import java.io.File;

import static org.junit.Assert.*;

/**
 * User: jeckels
 * Date: June 25, 2012
 *
 * Imports a SampleMinded data export (.xlsx) into the specimen repository.
 */
@Category({DailyB.class, Study.class, Specimen.class})
public class SampleMindedImportTest extends BaseWebDriverTest
{
    private static final String PROJECT_NAME = "SampleMindedImportTest";

    private static final String FILE = "SampleMindedExport.xlsx";

    @Override
    public String getAssociatedModuleDirectory()
    {
        return "server/modules/study";
    }

    @Override
    protected String getProjectName()
    {
        return PROJECT_NAME;
    }

    @Override
    protected boolean isFileUploadTest()
    {
        return true;
    }

    @Override
    protected void doCleanup(boolean afterTest) throws TestTimeoutException
    {
        File specimenDir = new File(getLabKeyRoot() + "/sampledata/study/specimens");
        File specimenArchive = new File(specimenDir, "SampleMindedExport.specimens");
        specimenArchive.delete();

        for (File file : specimenDir.listFiles())
        {
            if (file.getName().startsWith(FILE) && file.getName().endsWith(".log"))
            {
                file.delete();
            }
        }

        // Now delete the project
        super.doCleanup(afterTest);
    }

    @Override
    protected void doTestSteps() throws Exception
    {
        _containerHelper.createProject(PROJECT_NAME, "Study");
        clickButton("Create Study");
        setFormElement(Locator.name("startDate"),"2011-01-01");
        click(Locator.radioButtonByNameAndValue("simpleRepository", "true"));
        clickButton("Create Study");

        clickAndWait(Locator.linkWithText("manage visits"));
        clickAndWait(Locator.linkWithText("create new visit"));
        setFormElement(Locator.name("label"),"Visit SE");
        setFormElement(Locator.name("sequenceNumMin"),"999.0000");
        setFormElement(Locator.name("sequenceNumMax"),"999.9999");
        selectOptionByValue(Locator.name("sequenceNumHandling"),"logUniqueByDate");
        clickAndWait(Locator.linkWithText("save"));

        // "overview" is a dumb place for this link
        clickAndWait(Locator.linkWithText("Overview"));
        clickAndWait(Locator.linkWithText("manage files"));
        setPipelineRoot(getLabKeyRoot() + "/sampledata/study");
        clickAndWait(Locator.linkWithText(PROJECT_NAME + " Study"));
        clickAndWait(Locator.linkWithText("Manage Files"));

        clickButton("Process and Import Data");
        FileBrowserHelperWD fileBrowserHelper = new FileBrowserHelperWD(this);
        fileBrowserHelper.importFile("specimens/" + FILE, "Import Specimen Data");
        clickButton("Start Import");
        waitForPipelineJobsToComplete(1, "Import specimens: SampleMindedExport.xlsx", false);
        clickTab("Specimen Data");
        waitForElement(Locator.linkWithText("BAL"));
        assertLinkPresentWithText("BAL");
        assertLinkPresentWithText("Blood");
        clickAndWait(Locator.linkWithText("By Individual Vial"));
        assertLinkPresentWithTextCount("P1000001", 6);
        assertLinkPresentWithTextCount("P2000001", 3);
        assertLinkPresentWithTextCount("P20043001", 5);
        assertTextPresent("20045467");
        assertTextPresent("45627879");
        assertTextPresent("1000001-21");

        clickTab("Specimen Data");
        waitForElement(Locator.linkWithText("NewSpecimenType"));
        clickAndWait(Locator.linkWithText("NewSpecimenType"));
        assertTextPresent("EARL (003)");
        assertTextPresent("REF-A Cytoplasm Beaker");

        clickTab("Specimen Data");
        waitForElement(Locator.linkWithText("BAL"));
        clickAndWait(Locator.linkWithText("BAL"));
        assertTextPresent("BAL Supernatant");
        assertTextPresent("FREE (007)");
        DataRegionTable specimenTable = new DataRegionTable("SpecimenDetail", this, true, true);
        assertEquals("Incorrect number of vials.", "Count:  5", specimenTable.getTotal("Global Unique Id"));

        clickAndWait(Locator.linkWithText("Group vials"));
        assertLinkPresentWithTextCount("P20043001", 2);
        assertTextPresent("Visit SE");

        // add column sequencenum
        new CustomizeViewsHelper(this).openCustomizeViewPanel();
        new CustomizeViewsHelper(this).showHiddenItems();
        new CustomizeViewsHelper(this).addCustomizeViewColumn("SequenceNum");
        new CustomizeViewsHelper(this).applyCustomView();
        assertTextPresent("999.0138");
    }
}
