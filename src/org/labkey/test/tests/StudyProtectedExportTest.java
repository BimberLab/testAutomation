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

import org.junit.Assert;
import org.junit.experimental.categories.Category;
import org.labkey.test.Locator;
import org.labkey.test.categories.DailyB;
import org.labkey.test.util.DataRegionTable;
import org.labkey.test.util.LogMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: elvan
 * Date: 8/14/12
 * Time: 9:55 AM
 */
@Category({DailyB.class})
public class StudyProtectedExportTest extends StudyExportTest
{
    int pipelineJobCount = 1;
    private String idPreface = "P!@#$%^&*(";
    private int idLength = 7;
    private Map<String,String> _originalFirstMouseStats;

    @Override
    protected void doCreateSteps()
    {
        createStudyManually();

        _originalFirstMouseStats = getFirstMouseStats();
        setParticipantIdPreface(idPreface, idLength);
        
        setUpTrickyExport();
        exportStudy(true, true, false, true, true, false, null);
    }

    protected void setParticipantIdPreface(String idPreface, int idLength)
    {
        clickTab("Manage");
        clickAndWait(Locator.linkContainingText("Manage Alternate"));
        _extHelper.setExtFormElementByLabel("Prefix", idPreface);
        setFormElement("numberOfDigits", "" + idLength);
        clickButton("Change Alternate IDs", 0);
        waitForText("Are you sure you want to change all Alternate IDs?");
        clickButton("OK", WAIT_FOR_EXT_MASK_TO_DISSAPEAR);
        waitForText("Changing Alternate IDs is complete");
        clickButton("OK", WAIT_FOR_EXT_MASK_TO_DISSAPEAR);
    }

    private void verifyParticipantGroups(String originalID, String newID)
    {
        clickAndWait(Locator.linkWithText("Mice"));
        assertTextNotPresent(originalID);
        assertTextPresent(newID);

        // not in any group only appears if there are participants not in any of the groups in a category
        assertTextPresent("Group 1", "Group 2");
        assertTextNotPresent("Not in any cohort");

        _ext4Helper.uncheckGridRowCheckbox("Group 1");
        _ext4Helper.uncheckGridRowCheckbox("Group 2");

        waitForText("No matching Mice");


        _ext4Helper.clickParticipantFilterGridRowText("Group 1", 0);
        waitForText("Found 10 mice of 25");
        assertElementPresent(Locator.xpath("//a[contains(@href, 'participant.view')]"), 10);

        log("verify sorting by groups works properly");
        goToDatasets();
        clickAndWait(Locator.linkContainingText("LLS-2"));
        DataRegionTable drt = new DataRegionTable( "Dataset", this);
        Assert.assertEquals("unexpected number of rows on initial viewing", 5, drt.getDataRowCount());
        clickMenuButton("Mouse Groups", "Cohorts", "Group 1");
        Assert.assertEquals("unexpected number of rows for group 1", 3, drt.getDataRowCount());
        clickMenuButton("Mouse Groups", "Cohorts", "Group 2");
        Assert.assertEquals("unexpected number of rows for cohort 2", 2, drt.getDataRowCount());
    }

    private void verifyStatsDoNotMatch(Map originalFirstMouseStats, Map alteredFirstMouseStats)
    {
        for(String columnName : defaultStatsToCollect)
        {
            Assert.assertNotSame(originalFirstMouseStats.get(columnName), alteredFirstMouseStats.get(columnName));
        }
    }

    private void verifyStatsMatch(Map originalFirstMouseStats, Map alteredFirstMouseStats)
    {
        for(String columnName : defaultStatsToCollect)
        {
            Assert.assertEquals(originalFirstMouseStats.get(columnName), alteredFirstMouseStats.get(columnName));
        }
    }

    @LogMethod
    private void importAlteredStudy()
    {
        clickButton("Import Study");
        clickButton("Import Study Using Pipeline");
        waitAndClick(Locator.xpath("//div[contains(@class, 'x-tree-node') and @*='/']"));//TODO: Bad cookie. Marker class won't appear without this step.
        _fileBrowserHelper.selectFileBrowserItem("export/");
        Locator.XPathLocator checkbox = Locator.xpath("//div[contains(text(), 'My Study_')]");
        waitForElement(checkbox);
        int exportCount = getXpathCount(checkbox);
        checkbox = checkbox.index(exportCount - 1); // get most recent export
        waitForElement(checkbox);
        clickAt(checkbox, "1,1");

        _fileBrowserHelper.selectImportDataAction("Import Study");
        waitForPipelineJobsToComplete(++pipelineJobCount, "study import", false);
    }

    @Override
    protected void doVerifySteps()
    {
        verifyImportingAlternateIds();

        deleteStudy(getStudyLabel());
        importAlteredStudy();
        goToDatasetWithProtectedColum();
        assertTextNotPresent(protectedColumnLabel);

        Map<String,String> alteredFirstMouseStats = getFirstMouseStats();
        Assert.assertTrue(alteredFirstMouseStats.get("Mouse Id").startsWith(idPreface));
        Assert.assertEquals(idPreface.length() + idLength, alteredFirstMouseStats.get("Mouse Id").length());
        DataRegionTable drt = new DataRegionTable( "Dataset", this);
        /* DOB doesn't change because it's a text field, not a true date.
           since it's the most unique thing on the page, we can use it to see a specific user and verify that
           the date fields did change
         */
        Assert.assertNotSame("2005-01-01", drt.getDataAsText(drt.getRow("1.Date of Birth", "1965-03-06"), "Contact Date"));
        verifyStatsDoNotMatch(_originalFirstMouseStats, alteredFirstMouseStats);
        verifyParticipantGroups(_originalFirstMouseStats.get("Mouse Id"), alteredFirstMouseStats.get("Mouse Id"));

        deleteStudy(getStudyLabel());
        importAlteredStudy();
        waitForPipelineJobsToComplete(3, "Study reimport", false);

        Map reimportedFirstMouseStats = getFirstMouseStats();
        verifyStatsMatch(alteredFirstMouseStats, reimportedFirstMouseStats);

        log("Verify second export and clinic masking");

        startSpecimenImport(4, SPECIMEN_ARCHIVE_A);
        waitForPipelineJobsToComplete(4, "Specimen import", false);
        exportStudy(true, true, false, true, true, true, null);

        deleteStudy(getStudyLabel());
        importAlteredStudy();
        waitForPipelineJobsToComplete(5, "Study reimport with specimens", false);

        verifyMaskedClinics(8);
    }

    private void goToDatasetWithProtectedColum()
    {
        goToDatasets();
        clickAndWait(Locator.linkContainingText(datasetWithProtectedColumn));
    }

    private void goToDatasets()
    {
        clickFolder(getFolderName());
        clickAndWait(Locator.linkContainingText("datasets"));
    }

    String datasetWithProtectedColumn =  "PT-1: Participant Transfer";
    String protectedColumnLabel = "Staff Initials/Date";
    private void setUpTrickyExport()
    {
        goToDatasetWithProtectedColum();
        clickButton("Manage Dataset");
        clickButton("Edit Definition");

        waitAndClick(Locator.name("ff_label9"));
        setColumnProtected();
        sleep(1000); //TODO
        clickButton("Save", 0);
        waitForSaveAssay();

    }

    protected  void verifyMaskedClinics(int clinicCount)
    {
        List<String> nonClinics = new ArrayList<>();

        goToSchemaBrowser();
        selectQuery("study", "Location");
        waitAndClickAndWait(Locator.linkWithText("view data"));
        DataRegionTable query = new DataRegionTable("query", this);
        int labelCol = query.getColumn("Label");
        int labCodeCol = query.getColumn("Labware Lab Code");
        int clinicCol = query.getColumn("Clinic");
        int rowCount = query.getDataRowCount();
        int foundClinics = 0;
        for (int i = 0; i < rowCount; i++)
        {
            if (query.getDataAsText(i, clinicCol).equals("true"))
            {
                foundClinics++;
                Assert.assertTrue("Clinic Location name was not masked", query.getDataAsText(i, labelCol).equals("Clinic"));
                Assert.assertTrue("Clinic Labware Lab Code was not masked", query.getDataAsText(i, labCodeCol).equals(""));
            }
            else // non-clinic
            {
                Assert.assertFalse("Non-clinic Location name was masked", query.getDataAsText(i, labelCol).equals("Clinic"));
            }
        }
        Assert.assertEquals("Unexpected number of clinics", clinicCount, foundClinics);

        clickTab("Manage");
        clickAndWait(Locator.linkWithText("Manage Locations"));
        foundClinics = 0;
        rowCount = getXpathCount(Locator.xpath("id('manageLocationsTable')/tbody/tr"));
        for (int i = 2; i <= rowCount - 2; i++) // skip header row; Stop before Add Location row & Save/Cancel button row
        {
            Locator.XPathLocator rowLoc = Locator.xpath("id('manageLocationsTable')/tbody/tr["+i+"]");
            String locName = getFormElement(rowLoc.append("/td/input[@name='labels']"));
            String locTypes = getText(rowLoc.append("/td[4]"));
            if (locTypes.contains("Clinic"))
            {
                Assert.assertTrue("Clinic Location name not masked", locName.equals("Clinic"));
                foundClinics++;
            }
            else
            {
                Assert.assertFalse("Clinic Location name not masked", locName.equals("Clinic"));
                nonClinics.add(locName);
            }
        }
        Assert.assertEquals("Unexpected number of clinics", clinicCount, foundClinics);

        clickTab("Specimen Data");
        waitAndClickAndWait(Locator.linkWithText("Blood (Whole)"));
        DataRegionTable vialsTable = new DataRegionTable("SpecimenDetail", this);
        List<String> procLocs = vialsTable.getColumnDataAsText("Processing Location");
        procLocs.remove(procLocs.size() - 1); // Skip aggregate row
        for (String procLoc : procLocs)
        {
            Assert.assertTrue("Processing Locations was not masked", procLoc.equals("Clinic") || nonClinics.contains(procLoc));
        }
        List<String> siteNames = vialsTable.getColumnDataAsText("Site Name");
        siteNames.remove(siteNames.size() - 1); // Skip aggregate row
        for (String siteName : siteNames)
        {
            Assert.assertTrue("Site Name was not masked", siteName.equals("Clinic") || siteName.equals("In Transit"));
        }
    }

    @Override
    public void runApiTests()
    {

    }


    //TODO
    private void setColumnProtected()
    {
        click(Locator.tagContainingText("span", "Advanced"));
        checkCheckbox("protected");
    }

    String[] defaultStatsToCollect = {"Mouse Id", "Contact Date"};
    //ID, DOB
    public Map<String, String> getFirstMouseStats()
    {
        goToDatasets();
        clickAndWait(Locator.linkContainingText("DEM-1"));
        DataRegionTable drt = new DataRegionTable("Dataset", this);
        Map stats = new HashMap();


        for(int i = 0; i <defaultStatsToCollect.length; i++)
        {
            stats.put(defaultStatsToCollect[i], drt.getDataAsText(0, defaultStatsToCollect[i]));
        }

        return stats;
    }

    private static final String BAD_ALTERNATEID_MAPPING =
            "ParticipantId\tAlternateId\tDateOffset\n" +
                    "999320582\tNEWALT_32\t0\n" +
                    "999320638\tNEWALT_32\t1";

    private static final String ALTERNATEID_MAPPING =
            "ParticipantId\tAlternateId\tDateOffset\n" +
                    "999320582\tNEWALT_32\t0\n" +
                    "999320638\tNEWALT_33\t1";

    private static final String BAD_ALTERNATEID_MAPPING_2 =
            "ParticipantId\tAlternateId\tDateOffset\n" +
                    "999320533\tNEWALT_32\t0\n" +
                    "999320638\tNEWALT_33\t1";

    private static final String BAD_ALTERNATEID_MAPPING_3 =
            "ParticipantId\tAlternateId\tDateOffset\n" +
                    "999320582\n" +
                    "999320638\tNEWALT_13\t1";

    private static final String BAD_ALTERNATEID_MAPPING_4 =
                    "999320582\tNEWALT_12\t0\n" +
                    "999320638\tNEWALT_13\t1";

    private static final String ALTERNATEID_MAPPING_2 =
            "AlternateId\tParticipantId\tDateOffset\n" +
                    "NEWALT_32AB9\t999320582\t0\n" +
                    "NEWALT_333Q\t999320638\t1";

    @LogMethod
    private void verifyImportingAlternateIds()
    {
        goToManageStudy();
        clickAndWait(Locator.linkContainingText("Manage Alternate"));
        clickButton("Import");
        waitForElement(Locator.xpath("//textarea[@id='tsv3']"));
        assertTextPresent("Export Participant Transforms");
        setFormElement(Locator.xpath("//textarea[@id='tsv3']"), BAD_ALTERNATEID_MAPPING);
        clickButton("Submit", "Two participants may not share the same Alternate ID.");

        setFormElement(Locator.xpath("//textarea[@id='tsv3']"), ALTERNATEID_MAPPING);
        clickButton("Submit");

        // Test that ids actually got changed
        clickButton("Import");
        waitForElement(Locator.xpath("//textarea[@id='tsv3']"));
        setFormElement(Locator.xpath("//textarea[@id='tsv3']"), BAD_ALTERNATEID_MAPPING_2);
        clickButton("Submit", "Two participants may not share the same Alternate ID.");

        // Test input lacking all columns
        setFormElement(Locator.xpath("//textarea[@id='tsv3']"), BAD_ALTERNATEID_MAPPING_3);
        clickButton("Submit", "Either AlternateId or DateOffset must be specified.");

        // Test input without header row
        setFormElement(Locator.xpath("//textarea[@id='tsv3']"), BAD_ALTERNATEID_MAPPING_4);
        clickButton("Submit", "The header row must contain ParticipantId and either AlternateId, DateOffset or both.");

        // Good input with different column order
        setFormElement(Locator.xpath("//textarea[@id='tsv3']"), ALTERNATEID_MAPPING_2);
        clickButton("Submit");

        assertTextPresent("Manage Alternate", "Aliases");
        clickButton("Done");
    }
}
