/*
 * Copyright (c) 2013-2018 LabKey Corporation
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
import org.labkey.test.categories.Daily;
import org.labkey.test.categories.Specimen;
import org.labkey.test.util.LogMethod;
import org.labkey.test.util.StudyHelper;

import java.io.File;

@Category({Daily.class, Specimen.class})
@BaseWebDriverTest.ClassTimeout(minutes = 6)
public class SpecimenMultipleImportTest extends StudyBaseTest
{
    protected static final String PROJECT_NAME = "AliquotVerifyProject";

    protected static final File SPECIMEN_ARCHIVE_15 = StudyHelper.getSpecimenArchiveFile("lab15.specimens");
    protected static final File SPECIMEN_ARCHIVE_19 = StudyHelper.getSpecimenArchiveFile("lab19.specimens");
    protected static final File SPECIMEN_ARCHIVE_20 = StudyHelper.getSpecimenArchiveFile("lab20.specimens");
    protected static final File SPECIMEN_ARCHIVE_21 = StudyHelper.getSpecimenArchiveFile("lab21.specimens");
    protected static final File SPECIMEN_ARCHIVE_22 = StudyHelper.getSpecimenArchiveFile("lab22.specimens");

    @Override
    protected String getProjectName()
    {
        return PROJECT_NAME;
    }

    @Override
    protected BrowserType bestBrowser()
    {
        return BrowserType.CHROME;
    }

    @Override
    @LogMethod
    protected void doCreateSteps()
    {
        enableEmailRecorder();
        initializeFolder();

        clickButton("Create Study");
        setFormElement(Locator.name("label"), getStudyLabel());
        clickButton("Create Study");
        _studyHelper.setupAdvancedRepositoryType();

        setPipelineRoot(StudyHelper.getStudySubfolderPath());

        startSpecimenImport(1, SPECIMEN_ARCHIVE_15);
        startSpecimenImport(2, SPECIMEN_ARCHIVE_19);
        startSpecimenImport(3, SPECIMEN_ARCHIVE_20);
        startSpecimenImport(4, SPECIMEN_ARCHIVE_21);
        startSpecimenImport(5, SPECIMEN_ARCHIVE_22);

        waitForSpecimenImport();
    }

    @Override
    @LogMethod
    protected void doVerifySteps()
    {
        clickFolder(getFolderName());
        goToSpecimenData();
        waitAndClickAndWait(Locator.linkWithText("Blood (Whole)").notHidden());

        // check for number of specimens in import, total volume is a good identifier for everything
        assertTextPresent("75,990,002.5");

        // look for a specimen that came from 1st and 3rd file
        assertTextPresent("999320895", "999320422");
    }
}
