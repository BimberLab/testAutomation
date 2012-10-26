/*
 * Copyright (c) 2006-2012 LabKey Corporation
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

import junit.framework.Assert;
import org.labkey.test.Locator;
import org.labkey.test.SortDirection;

import java.io.File;
import java.io.IOException;

/**
 * User: billnelson@uky.edu
 * Date: Aug 7, 2006
 * Time: 1:04:36 PM
 *
 */
public class SequestTest extends AbstractMS2SearchEngineTest
{
    protected static final String PEPTIDE = "K.VFHFVR.Q";
    protected static final String PEPTIDE2 = "K.GRLLIQMLK.Q";
    protected static final String PEPTIDE3 = "K.KLYNEELK.A";
    protected static final String PEPTIDE4 = "K.ARKAPQYSK.R";
    protected static final String PEPTIDE5 = "K.DSPRISIAGR.L";
    protected static final String PROTEIN = "gi|34558016|50S_RIBOSOMAL_PRO";
    protected static final String SEARCH = "gi|15677167|30S_ribosomal_pro";
    protected static final String SEARCH_FIND = "Neisseria meningitidis";
    protected static final String PROTOCOL = "Sequest analysis";
    protected static final String SEARCH_TYPE = "sequest";
    protected static final String SEARCH_BUTTON = "Sequest";
    protected static final String SEARCH_NAME = "SEQUEST";

    protected void doCleanup(boolean afterTest) throws IOException
    {
        try {
            deleteViews(VIEW); } catch (Throwable t) {}
        try {deleteRuns(); } catch (Throwable t) {}
        cleanPipe(SEARCH_TYPE);
        try {deleteFolder(PROJECT_NAME, FOLDER_NAME); } catch (Throwable t) {}
        try {deleteProject(PROJECT_NAME); } catch (Throwable t) {}
    }

    protected void doTestSteps()
    {

        beginAt("/admin/showCustomizeSite.view");
        if (null == getAttribute(Locator.name("sequestServer"), "value") || "".equals(getAttribute(Locator.name("sequestServer"), "value")))
        {
            log("Your sequest settings are not configured.  Skipping sequest test.");
            return;
        }

        log("Testing your Sequest settings");
        addUrlParameter("testInPage=true");
        pushLocation();
        clickLinkWithText("Test Sequest settings");
        assertTextPresent("test passed.");
        popLocation();

        String altSequestServer = "bogus.domain";
        log("Testing wrong Sequest server via " + altSequestServer);
        setFormElement("sequestServer", altSequestServer);
        pushLocation();
        clickLinkWithText("Test Sequest settings");
        assertTextPresent("Test failed.");
        assertTextPresent("Failed to interact with SequestQueue application");
        log("Return to customize page.");
        popLocation();

        log("Verifying that pipeline files were cleaned up properly");
        File test2 = new File(_pipelinePath + "/bov_sample/" + SEARCH_TYPE + "/test2");
        if (test2.exists())
            Assert.fail("Pipeline files were not cleaned up; test2("+test2.toString()+") directory still exists");

        super.doTestSteps();
    }

    protected void setupEngine()
    {
        log("Analyze " + SEARCH_NAME + " sample data.");
        waitAndClickButton(SEARCH_BUTTON + " Peptide Search");
    }

    protected void basicChecks()
    {
        clickLinkWithText("MS2 Dashboard");
        clickLinkWithImage(getContextPath() + "/MS2/images/runIcon.gif");

        // Make sure we're not using a custom default view for the current user
        selectOptionByText("viewParams", "<Standard View>");
        clickButton("Go");

        log("Test filtering and sorting");
        setFilter("MS2Peptides", "Mass", "Is Greater Than", "1000");
        assertTextNotPresent(PEPTIDE);
        setSort("MS2Peptides", "Scan", SortDirection.DESC);
        assertTextBefore(PEPTIDE2, PEPTIDE3);

        log("Test Save View");
        clickButton("Save View");
        setFormElement("name", VIEW);
        clickButton("Save View");
        selectOptionByText("viewParams", "<Standard View>");
        clickButton("Go");
        assertTextPresent(PEPTIDE);
        selectOptionByText("viewParams", VIEW);
        clickButton("Go");
        assertTextNotPresent(PEPTIDE);
        assertTextBefore(PEPTIDE2, PEPTIDE3);

        log("Test exporting");
        pushLocation();
        addUrlParameter("exportAsWebPage=true");
        clickButton("Export All", 0);
        clickLinkWithText("TSV", 0);
        assertTextNotPresent(PEPTIDE);
        assertTextBefore(PEPTIDE2, PEPTIDE3);
        assertTextPresent(PROTEIN);
        popLocation();

        log("Test Comparing Peptides");
        clickLinkWithText("MS2 Dashboard");
        click(Locator.name(".toggle"));
        waitForElement(Locator.navButton("Compare"), WAIT_FOR_JAVASCRIPT);
        clickButton("Compare", 0);
        clickLinkWithText("Peptide");
        selectOptionByText("viewParams", VIEW);
        clickButton("Compare");
        assertTextPresent("(Mass > 1000)");

        //Put in once bug with filters in postgres is fixed
        //assertTextNotPresent(PEPTIDE);

        setSort("MS2Compare", "Peptide", SortDirection.DESC);
        assertTextBefore(PEPTIDE5, PEPTIDE4);

        log("Navigate to folder Portal");
        clickLinkWithText("MS2 Dashboard");

        log("Verify experiment information in MS2 runs.");
        assertLinkPresentWithText(PROTOCOL);

        log("Test Protein Search");
        selenium.type("identifier", SEARCH);
        selenium.click("exactMatch");
        clickButton("Search");
        assertLinkPresentContainingText(SAMPLE_BASE_NAME + " (test2)");
        clickAndWait(Locator.id("expandCollapse-ProteinSearchProteinMatches"), 0);
        assertLinkPresentContainingText(SAMPLE_BASE_NAME + " (test2)");
        assertTextPresent(SEARCH_FIND);

        selenium.type("minimumProbability", "2.0");
        clickButton("Search");
        clickAndWait(Locator.id("expandCollapse-ProteinSearchProteinMatches"), 0);
        assertTextPresent(SEARCH_FIND);
        assertLinkNotPresentWithText(SAMPLE_BASE_NAME + " (test2)");

        selenium.type("identifier", "GarbageProteinName");
        selenium.type("minimumProbability", "");
        clickButton("Search");
        clickAndWait(Locator.id("expandCollapse-ProteinSearchProteinMatches"), 0);
        assertTextNotPresent(SEARCH_FIND);
        assertTextPresent("No data to show");

    }
}
