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

import org.labkey.test.BaseWebDriverTest;
import org.labkey.test.Locator;

import java.io.File;

/**
 * User: elvan
 * Date: 2/29/12
 * Time: 6:48 PM
 */
public class PipelineCancelTest  extends BaseWebDriverTest
{
    private static final String STUDY_ZIP = "/sampledata/pipelineCancel/LabkeyDemoStudy.zip";
    @Override
    protected String getProjectName()
    {
        return "Pipeline Cancel Test";
    }

    public void doTestSteps()
    {
        _containerHelper.createProject(getProjectName(), "Study");
        startImportStudyFromZip(new File(getLabKeyRoot() + STUDY_ZIP).getPath());

        log("Cancel import");
        waitForText("Delaying import");
        clickAndWait(Locator.linkContainingText("Delaying import"));
        clickButton("Cancel");

        log("Verify cancel succeeded");
//        waitForText("CANCELLED");
        waitForText("Attempting to cancel");
//        waitForTextWithRefresh("Interrupting job by sending interrupt request", defaultWaitForPage);
        waitForTextWithRefresh("CANCELLED", defaultWaitForPage);

        goToProjectHome();
        assertTextPresent("This folder does not contain a study."); //part of the import will be done, but it shouldn't have gotten to participants.

    }

    @Override
    public String getAssociatedModuleDirectory()
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
