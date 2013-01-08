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

package org.labkey.test.module;

import org.labkey.test.BaseWebDriverTest;
import org.labkey.test.Locator;
import org.labkey.test.TestTimeoutException;

/**
 * User: ulberge
 * Date: Aug 7, 2007
 */
public class ModuleTest extends BaseWebDriverTest
{
    private static final String PROJECT_NAME = "ModuleVerifyProject";
    private static final String TEST_MODULE_TEMPLATE_FOLDER_NAME = "testmodule";

    protected void doCleanup(boolean afterTest) throws TestTimeoutException
    {
        deleteProject(getProjectName(), afterTest);
    }

    protected void doTestSteps()
    {
        _containerHelper.createProject(PROJECT_NAME, null);
        log("Test module created from moduleTemplate");
        clickAndWait(Locator.linkWithText(PROJECT_NAME));
        goToFolderManagement();
        clickAndWait(Locator.linkWithText("Folder Type"));
        checkCheckbox(Locator.xpath("//input[@value='" + TEST_MODULE_TEMPLATE_FOLDER_NAME + "']"));
        clickButton("Update Folder");
        clickTab(TEST_MODULE_TEMPLATE_FOLDER_NAME);
        assertTextPresent("Hello, and welcome to the " + TEST_MODULE_TEMPLATE_FOLDER_NAME + " module.");
    }
    
    public String getAssociatedModuleDirectory()
    {
        return null;
    }

    @Override
    protected String getProjectName()
    {
        return PROJECT_NAME;
    }
}
