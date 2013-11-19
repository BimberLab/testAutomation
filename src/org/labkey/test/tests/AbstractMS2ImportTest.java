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

import org.labkey.test.Locator;
import org.labkey.test.SortDirection;
import org.labkey.test.TestTimeoutException;
import org.labkey.test.ms2.MS2TestBase;
import org.labkey.test.util.LogMethod;

import java.io.File;

import static org.junit.Assert.*;

/** Base class for MS2 tests that import existing results from the file system, instead of initiating new searches */
public abstract class AbstractMS2ImportTest extends MS2TestBase
{
    public static final String DEFAULT_EXPERIMENT = "Default Experiment";
    public static final String LEGACY_PEPTIDES_SCAN_6_100_VIEW_NAME = "legacyPeptidesScan6_100";
    public static final String QUERY_PEPTIDES_VIEW_NAME = "queryPeptidesView";
    public static final String QUERY_PROTEIN_GROUP_VIEW_NAME = "queryProteinGroupViewView";
    public static final String QUERY_PROTEINPROPHET_VIEW_NAME = "queryProteinProphetView";

    protected void doTestSteps()
    {
        setupMS2();
        clickAndWait(Locator.linkWithText("MS2 Dashboard"));
        verifyMS2();
    }

    protected static final String LEGACY_PROTEIN_VIEW_NAME = "legacyProteinView";
    protected static final String LEGACY_PROTEIN_PROPHET_VIEW_NAME = "legacyProteinProphetView";
    protected static final String VIEW4 = "queryView1";
    protected static final String VIEW5 = "queryView2";
    protected static final String PEPTIDE1 = "K.GSDSLSDGPACKR.S";
    protected static final String PEPTIDE2 = "R.TIDPVIAR.K";
    protected static final String PEPTIDE3 = "K.HVSGKIIGFFY.-";
    protected static final String PEPTIDE4 = "R.ISSTKMDGIGPK.K";
    protected static final String SEARCH_TYPE = "xtandem";
    protected static final String SEARCH_NAME3 = "X! Tandem";
    protected static final String ENZYME = "trypsin";
    protected static final String MASS_SPEC = "ThermoFinnigan";

    protected void doCleanup(boolean afterTest) throws TestTimeoutException
    {
        cleanPipe(SEARCH_TYPE);
        deleteProject(getProjectName(), afterTest);
    }

    protected void importMS2Run(String directoryName, int totalJobCount)
    {
        log("Upload existing MS2 data: ");
        clickFolder(FOLDER_NAME);
        clickButton("Process and Import Data");
        _extHelper.selectFileBrowserItem("bov_sample/" + SEARCH_TYPE + "/" + directoryName + "/" + SAMPLE_BASE_NAME + ".search.xar.xml");
        selectImportDataAction("Import Experiment");

        log("Going to the list of all pipeline jobs");
        clickAndWait(Locator.linkWithText("All"));

        log("Verify upload started.");
        assertTextPresent(SAMPLE_BASE_NAME + ".search.xar.xml");

        log("Verify upload finished.");
        waitForPipelineJobsToComplete(totalJobCount, "Waiting for upload to complete", false);
    }

    @LogMethod(category = LogMethod.MethodType.SETUP)
    protected void setupMS2()
    {
        log("Verifying that pipeline files were cleaned up properly");
        File test2 = new File(PIPELINE_PATH + "/bov_sample/" + SEARCH_TYPE + "/test2");
        if (test2.exists())
            fail("Pipeline files were not cleaned up; test2("+test2.toString()+") directory still exists");

        super.doTestSteps();

        importMS2Run("DRT1", 1);

        clickAndWait(Locator.linkWithText("MS2 Dashboard"));
        clickAndWait(Locator.linkContainingText("DRT1"));

        // Create some saved MS2 views
        selectOptionByText(Locator.name("viewParams"), "<Standard View>"); // Make sure we're not using a custom default view for the current user
        clickButton("Go");
        selectOptionByText(Locator.name("grouping"), "Peptides (Legacy)");
        clickAndWait(Locator.id("viewTypeSubmitButton"));
        addUrlParameter("exportAsWebPage=true");
        setFilter("MS2Peptides", "Scan", "Is Greater Than", "6", "Is Less Than or Equal To", "100");
        setSort("MS2Peptides", "Scan", SortDirection.DESC);
        clickButton("Save View");
        setFormElement(Locator.name("name"), LEGACY_PEPTIDES_SCAN_6_100_VIEW_NAME);
        clickButton("Save View");

        log("Make saved view for Protein for Comparison");
        selectOptionByText(Locator.name("viewParams"), "<Standard View>"); // Make sure we're not using a custom default view for the current user
        setFilter("MS2Peptides", "DeltaMass", "Is Greater Than", "0");
        selectOptionByText(Locator.name("grouping"), "Protein (Legacy)");
        clickAndWait(Locator.id("viewTypeSubmitButton"));
        setFilter("MS2Proteins", "SequenceMass", "Is Greater Than", "20000");
        addUrlParameter("exportAsWebPage=true");
        clickButton("Save View");
        setFormElement(Locator.name("name"), LEGACY_PROTEIN_VIEW_NAME);
        clickButton("Save View");

        selectOptionByText(Locator.name("viewParams"), LEGACY_PEPTIDES_SCAN_6_100_VIEW_NAME);
        clickButton("Go");
        selectOptionByText(Locator.name("grouping"), "ProteinProphet (Legacy)");
        clickAndWait(Locator.id("viewTypeSubmitButton"));
        setFilter("ProteinGroupsWithQuantitation", "GroupProbability", "Is Greater Than", "0.7");
        clickButton("Save View");
        setFormElement(Locator.name("name"), LEGACY_PROTEIN_PROPHET_VIEW_NAME);
        clickButton("Save View");

        selectOptionByText(Locator.name("viewParams"), LEGACY_PEPTIDES_SCAN_6_100_VIEW_NAME);
        clickButton("Go");
        selectOptionByText(Locator.name("grouping"), "Standard");
        clickAndWait(Locator.id("viewTypeSubmitButton"));
        clickButton("Save View");
        setFormElement(Locator.name("name"), QUERY_PEPTIDES_VIEW_NAME);
        clickButton("Save View");

        selectOptionByText(Locator.name("viewParams"), "<Standard View>");
        clickButton("Go");
        selectOptionByText(Locator.name("grouping"), "Protein Groups");
        clickAndWait(Locator.id("viewTypeSubmitButton"));
        setFilter("ProteinGroups", "Group", "Is Less Than", "6");
        clickButton("Save View");
        setFormElement(Locator.name("name"), QUERY_PROTEIN_GROUP_VIEW_NAME);
        clickButton("Save View");

        selectOptionByText(Locator.name("viewParams"), LEGACY_PEPTIDES_SCAN_6_100_VIEW_NAME);
        clickButton("Go");
        selectOptionByText(Locator.name("grouping"), "Standard");
        clickAndWait(Locator.id("viewTypeSubmitButton"));
        clickMenuButton("Views", "ProteinProphet");
        setFilter("MS2Peptides", "ProteinProphetData/ProteinGroupId/GroupProbability", "Is Greater Than", "0.7");
        clickButton("Save View");
        setFormElement(Locator.name("name"), QUERY_PROTEINPROPHET_VIEW_NAME);
        clickButton("Save View");
    }

    @LogMethod(category = LogMethod.MethodType.VERIFICATION)
    protected abstract void verifyMS2();
}
