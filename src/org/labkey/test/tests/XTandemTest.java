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

import org.junit.Assert;
import org.junit.experimental.categories.Category;
import org.labkey.test.Locator;
import org.labkey.test.SortDirection;
import org.labkey.test.categories.BVT;
import org.labkey.test.categories.MS2;
import org.labkey.test.categories.XTandem;
import org.labkey.test.ms2.AbstractXTandemTest;

import java.io.File;

@Category({BVT.class, MS2.class, XTandem.class})
public class XTandemTest extends AbstractXTandemTest
{
    protected static final String SEARCH = "gi|4689022";
    protected static final String SEARCH_FIND = "SCHIZOSACCHAROMYCES";
    protected static final String SEARCH_FIND_ALT = "Schizosaccharomyces";
    protected static final String PROTOCOL = "X!Tandem analysis";
    protected static final String PEPTIDE_CROSSTAB_RADIO_PROBABILITY_ID = "peptideProphetRadioButton";
    protected static final String PEPTIDE_CROSSTAB_RADIO_PROBABILITY_VALUE = "probability";
    protected static final String PEPTIDE_CROSSTAB__PROBABILITY_TEXTBOX_NAME = "peptideProphetProbability";
    protected static final String PEPTIDE_CROSSTAB_RADIO_NAME = "peptideFilterType";
    protected static final String PEPTIDE_CROSSTAB_RADIO_VALUE_NONE = "none";

    protected void doTestSteps()
    {
        doTestStepsSetDepth(false);
    }
    protected void doTestStepsSetDepth(boolean isQuickTest)
    {
        setIsQuickTest(isQuickTest);

        log("Verifying that pipeline files were cleaned up properly");
        File test2 = new File(PIPELINE_PATH + "/bov_sample/" + SEARCH_TYPE + "/test2");
        if (test2.exists())
            Assert.fail("Pipeline files were not cleaned up; test2("+test2.toString()+") directory still exists");

        super.doTestSteps();
    }

    protected void basicChecks()
    {
        goToModule("Query");
        selectQuery("ms2", "Fractions");
        waitForElement(Locator.linkWithText("view data"), WAIT_FOR_JAVASCRIPT);
        clickAndWait(Locator.linkWithText("view data"));
        assertTextPresent("CAexample_mini.mzXML");
        // There should be 200 scans total
        assertTextPresent("200");
        // There should be 100 MS1 scans and 100 MS2 scans
        assertTextPresent("100");

        clickAndWait(Locator.linkWithText("MS2 Dashboard"));
        clickAndWait(Locator.linkWithImage(getContextPath() + "/MS2/images/runIcon.gif"));

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
        assertTextNotPresent("K.VFHFVR.Q");
        assertTextBefore(PEPTIDE2, PEPTIDE3);

        verifyPeptideDetailsPage();


        log("Test exporting");
        pushLocation();
        addUrlParameter("exportAsWebPage=true");
        clickButton("Export All", 0);
        clickAndWait(Locator.xpath("//a/span[text()='TSV']"));
        assertTextNotPresent(PEPTIDE);
        assertTextBefore(PEPTIDE2, PEPTIDE3);
        assertTextPresent(PROTEIN);
        popLocation();

        log("Test Comparing Peptides");
        clickAndWait(Locator.linkWithText("MS2 Dashboard"));
        click(Locator.name(".toggle"));
        waitForElement(Locator.navButton("Compare"), WAIT_FOR_JAVASCRIPT);
        clickButton("Compare", 0);
        clickAndWait(Locator.linkWithText("Peptide (Legacy)"));
        selectOptionByText("viewParams", VIEW);
        clickButton("Compare");
        assertTextPresent("(Mass > 1000)");

        //Put in once bug with filters in postgres is fixed
        assertTextNotPresent(PEPTIDE);

        setSort("MS2Compare", "Peptide", SortDirection.DESC);
        assertTextBefore(PEPTIDE5, PEPTIDE4);


        clickAndWait(Locator.linkWithText("MS2 Dashboard"));
        verifyPeptideCrosstab();
        verifyComparePeptides();

    }

    private void verifyComparePeptides()
    {
        clickAndWait(Locator.linkWithText("Setup Compare Peptides"));
        clickRadioButtonById(PEPTIDE_CROSSTAB_RADIO_PROBABILITY_ID);
        setFormElement(PEPTIDE_CROSSTAB__PROBABILITY_TEXTBOX_NAME, "0.75");
        clickButton("Compare");
        assertTextPresent(PEPTIDE3);
        assertTextPresent(PEPTIDE4);
        assertTextNotPresent(PEPTIDE);

        log("Navigate to folder Portal");
        clickAndWait(Locator.linkWithText("MS2 Dashboard"));

        log("Verify experiment information in MS2 runs.");
        assertLinkPresentWithText(PROTOCOL);

        log("Test Protein Search");
        selenium.type("identifier", SEARCH);
        selenium.click("exactMatch");
        clickButton("Search");
        assertLinkPresentContainingText(SAMPLE_BASE_NAME + " (test2)");
        clickAndWait(Locator.id("expandCollapse-ProteinSearchProteinMatches"), 0);
        Assert.assertTrue(isTextPresent(SEARCH_FIND) || isTextPresent(SEARCH_FIND_ALT));

        selenium.type("minimumProbability", "2.0");
        clickButton("Search");
        clickAndWait(Locator.id("expandCollapse-ProteinSearchProteinMatches"), 0);
        Assert.assertTrue(isTextPresent(SEARCH_FIND) || isTextPresent(SEARCH_FIND_ALT));
        assertLinkNotPresentWithText(SAMPLE_BASE_NAME + " (test2)");

        selenium.type("identifier", "GarbageProteinName");
        selenium.type("minimumProbability", "");
        clickButton("Search");
        clickAndWait(Locator.id("expandCollapse-ProteinSearchProteinMatches"), 0);
        Assert.assertTrue(!(isTextPresent(SEARCH_FIND) || isTextPresent(SEARCH_FIND_ALT)));
        assertTextNotPresent(SEARCH_FIND);
        assertTextPresent("No data to show");
    }

    private void verifyPeptideDetailsPage()
    {

        log("Test peptide details page");
        selenium.openWindow("", "pep");
        click(Locator.linkWithText(PEPTIDE2));
        selenium.waitForPopUp("pep", "10000");
        selenium.selectWindow("pep");
        assertTextPresent("gi|4689022|ribosomal_protein_");  // Check for protein
        assertTextPresent("CAexample_mini.pep.xml - bov_sample/CAexample_mini (test2)"); // Check for run name
        assertTextPresent("1373.4690"); // Check for mass
        waitForText("44.0215"); // Look for b3+ ions, populated bu JavaScript
        assertTextPresent("87.0357", "130.0499");
        selenium.close();
        selenium.selectWindow(null);
    }

    private void verifyPeptideCrosstab()
    {
        log("Test PeptideCrosstab");
        click(Locator.name(".toggle"));
        waitForElement(Locator.navButton("Compare"), WAIT_FOR_JAVASCRIPT);
        clickButton("Compare", 0);
        clickAndWait(Locator.linkWithText("Peptide"));

        checkRadioButton(PEPTIDE_CROSSTAB_RADIO_NAME, PEPTIDE_CROSSTAB_RADIO_VALUE_NONE);
        clickButton("Compare");
        assertTextPresent(PEPTIDE3);
        assertTextPresent(PEPTIDE4);
        assertTextPresent(PEPTIDE);
    }

    @Override
    protected boolean isPipelineToolsTest()
    {
        return true;
    }
}
