/*
 * Copyright (c) 2011-2012 LabKey Corporation
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
import org.labkey.test.BaseWebDriverTest;
import org.labkey.test.Locator;
import org.labkey.test.util.RReportHelperWD;
import org.labkey.test.util.ResetTracker;
import org.labkey.test.util.UIContainerHelper;

/**
 * User: elvan
 * Date: 8/4/11
 * Time: 3:23 PM
 */
public class EmbeddedWebPartTest extends BaseWebDriverTest
{
    ResetTracker resetTracker = null;

    protected  final String PROJECT_NAME = TRICKY_CHARACTERS_FOR_PROJECT_NAMES + "Embedded web part test";

    @Override
    protected String getProjectName()
    {
        return PROJECT_NAME;
    }

    public EmbeddedWebPartTest()
    {
        setContainerHelper(new UIContainerHelper(this));
    }

    public void configure()
    {
        RReportHelperWD _rReportHelper = new RReportHelperWD(this);
        _rReportHelper.ensureRConfig();
        log("Setup project and list module");
        _containerHelper.createProject(getProjectName(), null);
        resetTracker = new ResetTracker(this);
    }

    @Override
    protected void doTestSteps() throws Exception
    {
        configure();
        embeddedQueryWebPartDoesNotRefreshOnChange();

        //configure for refresh monitoring
    }

    private void embeddedQueryWebPartDoesNotRefreshOnChange()
    {
        log("testing that embedded query web part does not refresh on change");

        //embed query part in wiki page
        addWebPart("Wiki");
        createNewWikiPage();
        clickLinkContainingText("Source", 0, false);
        setFormElement(Locator.name("name"), TRICKY_CHARACTERS + "wiki page");

        setWikiBody(getFileContents("server/test/data/api/EmbeddedQueryWebPart.html"));

        clickButton("Save & Close");
        waitForText("Display Name");

        String rViewName = TRICKY_CHARACTERS + "new R view";
        _customizeViewsHelper.createRView(null, rViewName);

        waitForElement(Locator.xpath("//table[contains(@class, 'labkey-data-region')]"), WAIT_FOR_JAVASCRIPT);

        resetTracker.startTrackingRefresh();

        clickMenuButtonAndContinue("Views", rViewName);

        resetTracker.assertWasNotRefreshed();
    }


    @Override
    protected void doCleanup(boolean afterTest)
    {
        deleteProject(PROJECT_NAME, false);
    }

    @Override
    public String getAssociatedModuleDirectory()
    {
        Assert.fail("Not implemented");
        return null;
    }
}
