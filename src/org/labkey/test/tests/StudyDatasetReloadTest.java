/*
 * Copyright (c) 2014 LabKey Corporation
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
import org.labkey.test.TestFileUtils;
import org.labkey.test.categories.DailyA;
import org.labkey.test.util.LogMethod;

import java.io.File;

@Category({DailyA.class})
public class StudyDatasetReloadTest extends StudyBaseTest
{
    private static final File STUDY_WITH_BIT = TestFileUtils.getSampleData("studies/StudyWithDemoBit.folder.zip");
    private static final File STUDY_WITHOUT_BIT = TestFileUtils.getSampleData("studies/StudyWithoutDemoBit.folder.zip");

    protected String getProjectName()
    {
        return "StudyDatasetReloadProject";
    }

    protected String getFolderName()
    {
        return "Study Dataset Reload";
    }

    @Override
    @LogMethod(category = LogMethod.MethodType.SETUP)
    protected void doCreateSteps()
    {
        initializeFolder();
        initializePipeline(null);
        clickFolder(getFolderName());

        log("Import study with Demographics bit set on dataset");
        importFolderFromZip(STUDY_WITH_BIT);
    }

    @Override
    @LogMethod(category = LogMethod.MethodType.VERIFICATION)
    protected void doVerifySteps()
    {
        reloadStudyFromZip(STUDY_WITHOUT_BIT);

        // Check changes
        clickFolder(getFolderName());
        clickAndWait(Locator.linkContainingText("DEM: Demographics"));
        clickButton("Manage Dataset");
        assertTextPresent("StaffCode", "DoubleNum");
        assertElementPresent(Locator.tagWithText("td", "Staff Code").append("/../td/input[last()][@checked]"));     // MV
        assertElementPresent(Locator.tagWithText("td", "VisitDay").append("/../td/input[last()][not(@checked)]"));  // MV

        clickButtonContainingText("Edit Definition");
        waitForElement(Locator.name("description"), BaseWebDriverTest.WAIT_FOR_JAVASCRIPT);
        assertNotChecked(Locator.checkboxByName("demographicData"));

        goToManageStudy();
        clickButton("Reload Study");
        setFormElement(Locator.name("folderZip"), STUDY_WITH_BIT);
        clickButton("Reload Study From Local Zip Archive");
        waitForPipelineJobsToComplete(3, "Study Reload", false);

        // Check changes
        clickFolder(getFolderName());
        clickAndWait(Locator.linkContainingText("DEM-1: Demographics"));
        clickButton("Manage Dataset");
        assertTextNotPresent("StaffCode", "DoubleNum");
        assertElementPresent(Locator.tagWithText("td", "VisitDay").append("/../td/input[last()][@checked]"));  // MV

        clickButtonContainingText("Edit Definition");
        waitForElement(Locator.name("description"), BaseWebDriverTest.WAIT_FOR_JAVASCRIPT);
        assertChecked(Locator.checkboxByName("demographicData"));
    }


}
