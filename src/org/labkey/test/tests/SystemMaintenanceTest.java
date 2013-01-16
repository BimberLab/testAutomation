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

import org.labkey.test.BaseSeleniumWebTest;
import org.labkey.test.Locator;
import org.labkey.test.TestTimeoutException;

/**
 * User: elvan
 * Date: 8/25/12
 * Time: 6:23 PM
 */
public class SystemMaintenanceTest extends BaseSeleniumWebTest
{
    @Override
    protected String getProjectName()
    {
        return null;
    }

    @Override
    protected void doTestSteps() throws Exception
    {

        // Disable scheduled system maintenance
        setSystemMaintenance(false);
        // Manually start system maintenance... we'll check for completion at the end of the test (before mem check)
        startSystemMaintenance();

        checkRadioButton("usageReportingLevel", "MEDIUM");     // Force devs to report full usage info
        checkRadioButton("exceptionReportingLevel", "HIGH");   // Force devs to report full exception info
        clickButton("Save");

        // Now that the test is done, ensure that system maintenance is complete...
        waitForSystemMaintenanceCompletion();

        // Verify scheduled system maintenance is disabled.
        goToAdminConsole();
        clickAndWait(Locator.linkWithText("running threads"));
        assertTextNotPresent("SystemMaintenance");

    }

    @Override
    protected void doCleanup(boolean afterTest) throws TestTimeoutException
    {

    }

    @Override
    public String getAssociatedModuleDirectory()
    {
        return null;
    }
}
