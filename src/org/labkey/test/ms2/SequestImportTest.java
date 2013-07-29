/*
 * Copyright (c) 2013 LabKey Corporation
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
package org.labkey.test.ms2;

import org.junit.experimental.categories.Category;
import org.labkey.test.BaseWebDriverTest;
import org.labkey.test.Locator;
import org.labkey.test.ModulePropertyValue;
import org.labkey.test.categories.DailyB;
import org.labkey.test.categories.MS2;
import org.labkey.test.util.CustomizeViewsHelperWD;

import java.util.ArrayList;
import java.util.List;

/**
 * User: jeckels
 * Date: 2/8/13
 */
@Category({DailyB.class, MS2.class})
public class SequestImportTest extends BaseWebDriverTest
{
    private static final String PROJECT_NAME = "SequestImport" + TRICKY_CHARACTERS_FOR_PROJECT_NAMES;

    private static final String[] TOTAL_PEPTIDES_FIELD_KEY = {"PeptideCounts", "TotalPeptides"};
    private static final String[] UNIQUE_PEPTIDES_FIELD_KEY = {"PeptideCounts", "DistinctPeptides"};

    @Override
    protected String getProjectName()
    {
        return PROJECT_NAME;
    }

    @Override
    protected void doTestSteps() throws Exception
    {
        setupProject();
        importSequestRun();
        verifyRunGrid();
    }

    private void verifyRunGrid()
    {
        clickAndWait(Locator.linkWithText("MS2 Dashboard"));

        // Customize the view to show the distinct and total peptide counts based on the criteria established
        // by the custom query
        CustomizeViewsHelperWD viewsHelper = new CustomizeViewsHelperWD(this, Locator.id("MS2ExtensionsRunGrid"));
        viewsHelper.openCustomizeViewPanel();
        viewsHelper.addCustomizeViewColumn(TOTAL_PEPTIDES_FIELD_KEY);
        viewsHelper.addCustomizeViewColumn(UNIQUE_PEPTIDES_FIELD_KEY);

        // Add a filter so that we can check the values were calculated and shown correctly
        viewsHelper.addCustomizeViewFilter(TOTAL_PEPTIDES_FIELD_KEY, "Total Peptides", "Equals", "2");
        viewsHelper.addCustomizeViewFilter(UNIQUE_PEPTIDES_FIELD_KEY, "Distinct Peptides", "Equals", "1");
        viewsHelper.saveDefaultView();

        // Make sure that our run is still showing
        assertTextPresent("raftflow10", "ipi.HUMAN.fasta.v2.31");

        // Do a peptide comparison, making sure that the target protein filter works
        setFormElement(Locator.name("targetProtein"), "IPI00176617");
        toggleCheckboxByTitle("Select/unselect all on current page");
        clickButton("Compare Peptides");
        assertTextPresent("S.GDPEEEEEEEEELVDPLTTVR.E", "raftflow10");
        // Make sure that other peptides have been filtered our correctly based on the target protein
        assertTextNotPresent("A.ADSNPAP.S", "A.VPSGQDNIHR.F");

        // Make sure that our target protein is remembered across page views
        click(Locator.linkWithText("MS2 Dashboard"));
        assertTextPresent("IPI00176617");
    }

    private void importSequestRun()
    {
        clickAndWait(Locator.linkWithText("MS2 Dashboard"));
        // Import a Sequest run
        clickButton("Process and Import Data");
        _extHelper.selectFileBrowserItem("raftflow10.pep.xml");
        selectImportDataActionNoWaitForGrid("Import Search Results");

        click(Locator.linkWithText("MS2 Dashboard"));
        waitAndClick(Locator.linkWithText("Data Pipeline"));
        waitForPipelineJobsToComplete(1, "Experiment Import", false);
    }

    private void setupProject()
    {
        _containerHelper.createProject(PROJECT_NAME, "MS2 Extensions");
        setPipelineRoot(getLabKeyRoot()+ "/sampledata/raftflow");
        enableModule(PROJECT_NAME, "MS2Extensions");
        List<ModulePropertyValue> properties = new ArrayList<>();
        // Clear out any custom queries that might have been set by the user
        properties.add(new ModulePropertyValue("MS2Extensions", "/", "peptideCountQuery", ""));
        properties.add(new ModulePropertyValue("MS2Extensions", "/", "peptideCountSchema", ""));

        // We're being impolite by changing the site-wide default without resetting the previous value, but we
        // can live with that for now
        setModuleProperties(properties);
    }

    @Override
    public String getAssociatedModuleDirectory()
    {
        return "server/modules/ms2";
    }

    @Override
    public BrowserType bestBrowser()
    {
        return BrowserType.CHROME;
    }
}
