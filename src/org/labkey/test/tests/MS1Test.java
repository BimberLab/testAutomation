/*
 * Copyright (c) 2007-2012 LabKey Corporation
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
import org.labkey.test.BaseSeleniumWebTest;
import org.labkey.test.Locator;
import org.labkey.test.SortDirection;
import org.labkey.test.TestTimeoutException;
import org.labkey.test.util.UIContainerHelper;

import java.io.File;

/**
 * MS1 BVT
 * Created by IntelliJ IDEA.
 * User: Dave
 * Date: Nov 7, 2007
 * Time: 9:33:52 AM
 */
public class MS1Test extends BaseSeleniumWebTest
{
    public static final String PROJ_MAIN = "~~MS1 BVT PROJECT~~"; //use spaces to test for url encoding issues
    public static final String MS1_FOLDER_TYPE = "MS1";
    public static final String X_PROTOCOL = "X Search";
    public static final String FEATURES_PROTOCOL = "Feature Pep Match";
    public static final String BASE_FILE_NAME_1 = "test1";
    public static final String BASE_FILE_NAME_2 = "test2";
    public static final String BASE_FILE_NAME_3 = "msi-sample";
    public static final String FEATURES_TSV_EXTENSION = ".peptides.tsv";
    public static final String PEAKS_XML_EXTENSION = ".peaks.xml";
    public static final String PEP_XML_EXTENSION = ".pep.xml";
    public static final String SEARCH_XAR_XML_EXTENSION = ".search.xar.xml";
    public static final String PIPE_XAR_XML_EXTENSION = ".pipe.xar.xml";
    public static final String MZXML_EXTENSION = ".mzXML";
    public static final String DATAREGION_FEATURES = "fv";
    public static final String DATAREGION_PEAKS = "query"; //default query view data region name

    public static final String PIPELINE_ROOT_LINK = "root";
    public static final String PIPELINE_XTANDEM_DIR = "xtandem";
    public static final String PIPELINE_MS1PEP_DIR = "ms1peptides";
    public static final String PIPELINE_INSPECT_DIR = "inspect";
    public static final String PIPELINE_FIND_FEATURES_PROTOCOL = "Find Features";
    public static final String PIPELINE_IMPORT_EXPR_BUTTON = "Import Experiment";
    public static final String PIPELINE_PROCESS_AND_IMPORT_BUTTON = "Process and Import Data";
    public static final String PIPELINE_IMPORT_MS1_FEATURES_BUTTON = "Import";

    private static final File _pipelinePathMain = new File(getLabKeyRoot(), "/sampledata/ms1/bvt");

    protected void doTestSteps()
    {
        log("Starting MS1 BVT");
        setupEnvironment();

        importData(PROJ_MAIN, X_PROTOCOL, FEATURES_PROTOCOL);

        testViews(PROJ_MAIN);
    }

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        _containerHelper = new UIContainerHelper(this);
    }

    protected void setupEnvironment()
    {
        //for now, just create a main project with standard permissions
        //in the future, we should expand this to include multiple
        //projects and a more complex security scenario to ensure that
        //users can't view MS1 data they are not supposed to see
        _containerHelper.createProject(PROJ_MAIN, MS1_FOLDER_TYPE);

        //setup the pipeline
        setupPipeline(PROJ_MAIN, _pipelinePathMain.getAbsolutePath());
    }

    protected void setupPipeline(String project, String path)
    {
        log("Setting up pipeline for project " + project + "...");
        clickAndWait(Locator.linkWithText(project));

        //test invalid path
        setPipelineRoot(path + "-invalid");
        assertTextPresent("-invalid' does not exist");

        //set to valid path
        setPipelineRoot(path);
        assertTextPresent("The pipeline root was set to");
        assertTextNotPresent("does not exist");
        log("Pipeline successfully setup.");
    }

    protected void importData(String project, String xProtocol, String featuresProtocol)
    {
        log("Importing sample data...");

        //two x-tandem peptide experiments
        importXtandemExps(project, xProtocol);

        //and two features+peaks experiments
        importFeaturesExp(project, xProtocol, featuresProtocol);

        //add a simple features tsv with no peptide identifications
        //import it into the subfolder
        importPepTsvFile(project, PIPELINE_FIND_FEATURES_PROTOCOL);

        //go back to the portal/data pipeline page and wait for all four experiments to be complete
        clickAndWait(Locator.linkWithText(project));
        clickAndWait(Locator.linkWithText("Data Pipeline"));
        waitForPipelineJobsToComplete(5, "Experiment Import", false);

        log("Sample data imported.");

        //After data is imported, run the system maintenance task so that
        //we do a vacuum on Postgres. Otherwise, we'll get a timeout
        log("Running system maintenance task (vacuum on Postgres)...");
        startSystemMaintenance();
        waitForSystemMaintenanceCompletion();
        log("System maintenance task complete.");
    }

    protected void importXtandemExps(String project, String xProtocol)
    {
        //go back to the portal page
        clickAndWait(Locator.linkWithText(project));

        clickButton(PIPELINE_PROCESS_AND_IMPORT_BUTTON);

        _extHelper.selectFileBrowserItem(PIPELINE_XTANDEM_DIR + "/" + xProtocol + "/");

        selectImportDataAction(PIPELINE_IMPORT_EXPR_BUTTON);
    }

    protected void importFeaturesExp(String project, String xProtocol, String featuresProtocol)
    {
        //go back to the portal page
        clickAndWait(Locator.linkWithText(project));

        clickButton(PIPELINE_PROCESS_AND_IMPORT_BUTTON);

        _extHelper.selectFileBrowserItem(PIPELINE_XTANDEM_DIR + "/" + xProtocol + "/" + PIPELINE_MS1PEP_DIR + "/" + featuresProtocol + "/");

        selectImportDataAction(PIPELINE_IMPORT_EXPR_BUTTON);
    }

    protected void importPepTsvFile(String project, String protocol)
    {
        //go back to the portal page
        clickAndWait(Locator.linkWithText(project));

        clickButton(PIPELINE_PROCESS_AND_IMPORT_BUTTON);

        _extHelper.selectFileBrowserItem(PIPELINE_INSPECT_DIR + "/" + protocol + "/");

        selectImportDataAction(PIPELINE_IMPORT_EXPR_BUTTON);
    }

    protected void testViews(String project)
    {
        log("Testing views...");
        testFeaturesView(project);
        testSimilarSearchView(project);
        testPepSearchView(project);
        testCompareView(project);
        verifyFeaturesRendered(project);
        log("Finished Testing views.");
    }

    //verifies that the features file with no peptide associations actually
    //displayes the features properly.
    protected void verifyFeaturesRendered(String folder)
    {
        log("Verifying features view rendered in folder " + folder + "...");

        clickAndWait(Locator.linkWithText(folder));
        clickAndWait(Locator.linkWithText(getRunTitle("ms1-data", BASE_FILE_NAME_3, PIPELINE_FIND_FEATURES_PROTOCOL)));

        assertTextNotPresent("No data to show");
        assertTextPresent("1,432.8550");
        assertTextPresent("of 183"); //Showing 1 - 100 of 183

        log("Features rendered OK");
    }

    protected void testCompareView(String project)
    {
        log("Testing Compare Runs view");
        clickAndWait(Locator.linkWithText(project));
        checkAllOnPage("MSInspectFeatureRuns");
        clickButton("Compare", 60000);
        assertTextPresent("-.TMITDSLAVVLQR.R");
        assertTextPresent("236.9828");

        //test links
        pushLocation();
        //run title
        clickAndWait(Locator.linkWithText(getRunTitle(BASE_FILE_NAME_1, FEATURES_PROTOCOL)));
        assertTextPresent("229.8220"); //time value in first row
        popLocation();

        pushLocation();
        //measure value
        clickAndWait(Locator.linkWithText("236.9828"));
        assertTextPresent("2146"); //scan
        popLocation();

        //test measure filtering
        _customizeViewsHelper.openCustomizeViewPanel();
        _customizeViewsHelper.addCustomizeViewFilter("CTAGG_COUNT_FeatureId", "Num Features", "Is Greater Than", "1");
        _customizeViewsHelper.applyCustomView();
        assertLinkNotPresentWithText("1");

        pushLocation();
        addUrlParameter("exportAsWebPage=true");
        clickExportToText();
        assertTextPresent("K.GAGAFGYFEVTHDITR.Y");
        popLocation();

        //test fk table column filtering
        _customizeViewsHelper.openCustomizeViewPanel();
        _customizeViewsHelper.addCustomizeViewFilter("CTAGG_MIN_FeatureId/MZ", "First Feature MZ", "Is Greater Than", "500");
        _customizeViewsHelper.applyCustomView();
        assertTextNotPresent("461.7480"); //mz value

        log("Compare runs view OK.");
    }

    protected void testPepSearchView(String project)
    {
        log("Testing peptide search view");
        clickAndWait(Locator.linkWithText(project));
        ensureAdminMode();
        addWebPart("Peptide Search");

        setFormElement("pepSeq", "EASGDLPEAQIVK, AVVQDPALKPLALVYGEATSR");
        uncheckCheckbox("exact");
        clickButton("Search");

        //other scans should also be there
        assertTextPresent("459");
        assertTextPresent("1948");

        assertTextPresent("K.AVVQDPALKPLALVYGEATSR.R");
        assertTextPresent("K.E^ASGDLPEAQIVK.H");

        //make sure that an exact search doesn't find peptides with modifiers
        checkCheckbox("exact");
        clickButton("Search");
        assertTextNotPresent("K.E^ASGDLPEAQIVK.H");

        //jump to details
        clickAndWait(Locator.linkWithText("details"));
        clickAndWait(Locator.linkWithText("features with same"));
        assertTextPresent("K.AVVQDPALKPLALVYGEATSR.R");

        log("Peptide search OK.");
    }

    protected void testSimilarSearchView(String project)
    {
        log("Testing Similar Search View...");

        clickAndWait(Locator.linkWithText(project));
        clickAndWait(Locator.linkWithText(getRunTitle(BASE_FILE_NAME_2, FEATURES_PROTOCOL)));

        setFilter(DATAREGION_FEATURES, "MS2ConnectivityProbability", "Is Greater Than or Equal To", "0.90");
        setFilter(DATAREGION_FEATURES, "Scan", "Equals", "1948");
        clickAndWait(Locator.linkWithText("similar"));
        assertFormElementEquals("mzSource", "733.4119");
        assertFormElementEquals("timeSource", "1928.3200");
        assertTextPresent("1904");

        //scan 1904 should also be there
        assertTextPresent("1904");

        setFormElement("mzOffset", "100");
        selectOptionByValue("mzUnits", "mz");
        clickButton("Search");

        assertTextPresent("1888");
        assertTextPresent("1921");
        assertTextPresent("1976");

        selectOptionByValue("timeUnits", "scans");
        assertFormElementEquals("timeSource", "1948");
        setFormElement("timeOffset", "2");
        clickButton("Search");
        assertTextNotPresent("659.3492"); //m/z value that should no longer be there

        log("Finished Testing Similar Search View.");
    }

    protected String getRunTitle(String baseFileName, String protocol)
    {
        return X_PROTOCOL + "/" + baseFileName + " (" + protocol + ")";
    }

    protected String getRunTitle(String parentDir,String baseFileName, String protocol)
    {
        return parentDir + "/" + baseFileName + " (" + protocol + ")";
    }

    protected void testFeaturesView(String project)
    {
        clickAndWait(Locator.linkWithText(project));
        String run1Title = getRunTitle(BASE_FILE_NAME_1, FEATURES_PROTOCOL);
        String run2Title = getRunTitle(BASE_FILE_NAME_2, FEATURES_PROTOCOL);
        assertTextPresent(run1Title);
        assertTextPresent(run2Title);

        //Features View
        log("Tesitng showFeatures.view....");
        clickLinkWithText(run2Title, true);

        //test filtering
        log("Testing filtering...");
        setFilter(DATAREGION_FEATURES, "MS2ConnectivityProbability", "Is Greater Than or Equal To", "0.90");
        setFilter(DATAREGION_FEATURES, "TotalIntensity", "Is Greater Than or Equal To", "40000");

        //test sort
        log("Testing sort...");
        setSort(DATAREGION_FEATURES, "Intensity", SortDirection.DESC);
        assertTextBefore("66,204.2900", "49,012.0600");

        //test customize view
        log("Testing customize view...");
        _customizeViewsHelper.openCustomizeViewPanel();
        _customizeViewsHelper.removeCustomizeViewColumn("RelatedPeptide");
        _customizeViewsHelper.removeCustomizeViewColumn("RelatedPeptide/Fraction/Run/Description");
        _customizeViewsHelper.addCustomizeViewColumn("KL");
        _customizeViewsHelper.applyCustomView();

        assertTextPresent("KL");
        assertTextNotPresent("Related Peptide");
        assertTextNotPresent("K.AVVQDPALKPLALVYGEATSR.R");

        //reset view
        _customizeViewsHelper.openCustomizeViewPanel();
        _customizeViewsHelper.revertUnsavedView();

        //add other columns from peptide data
        //and test saving under a name
        _customizeViewsHelper.openCustomizeViewPanel();
        _customizeViewsHelper.addCustomizeViewColumn("RelatedPeptide/PeptideProphet", "PepProphet");
        _customizeViewsHelper.addCustomizeViewColumn("RelatedPeptide/Protein", "Protein");
        _customizeViewsHelper.addCustomizeViewSort("RelatedPeptide/PeptideProphet", "PepProphet", "Ascending");
        _customizeViewsHelper.moveCustomizeViewSort("RelatedPeptide/PeptideProphet", true);
        _customizeViewsHelper.saveCustomView("My View");

        assertTextPresent("PepProphet");
        assertTextPresent("Protein");
        assertTextPresent("18protmix|P46406|G3P_RABIT");
        assertTextBefore("0.9956", "0.9862");

        //switch back to default view
        clickMenuButton("Views", "default");
        assertTextNotPresent("PepProphet");
        assertTextNotPresent("Protein");
        assertTextNotPresent("18protmix|P46406|G3P_RABIT");

        //test export
        log("Testing export...");
        addUrlParameter("exportAsWebPage=true");
        pushLocation();
        clickExportToText();
        assertTextPresent("Scan");
        assertTextPresent("1948");
        assertTextPresent("1585");

        //ensure filtering and sorting are still in effect
        assertTextNotPresent("5,972.8930");
        assertTextBefore("66,204.2900", "49,012.0600");
        
        popLocation();

        //test printing
        pushLocation();
        addUrlParameter("exportType=printRows&exportRegion=fv");
        assertTextPresent("Scan");
        assertTextPresent("1948");
        assertTextPresent("1585");

        //ensure filtering and sorting are still in effect
        assertTextNotPresent("5,972.8930");
        assertTextBefore("66,204.2900", "49,012.0600");
        popLocation();

        //filter to just a single scan with peak data so we can test the other views
        setFilter(DATAREGION_FEATURES, "Scan", "Equals", "1948");

        //verify the data file information
        log("Verifying data file and software information...");
        assertTextPresent(BASE_FILE_NAME_2 + FEATURES_TSV_EXTENSION);
        assertTextPresent(BASE_FILE_NAME_2 + MZXML_EXTENSION);

        //verify the software information
        assertTextPresent("msInspect");
        assertTextPresent("(Fred Hutchinson Cancer Research Center)");
        assertTextPresent("org.fhcrc.cpl.viewer.feature.extraction.FeatureFinder");

        log("showFeatures.view is OK.");

        //Peaks View
        log("Testing showPeaks.view...");
        clickAndWait(Locator.linkWithText("peaks"));

        //test filtering
        setFilter(DATAREGION_PEAKS, "MZ", "Is Greater Than or Equal To", "1500");

        //verify the data file info
        assertTextPresent(BASE_FILE_NAME_2 + PEAKS_XML_EXTENSION);
        assertTextPresent(BASE_FILE_NAME_2 + MZXML_EXTENSION);

        //very the software info
        assertTextPresent("peakaboo");
        assertTextPresent("mzHigh");
        assertTextPresent("2000");

        log("showPeaks.view OK.");

        //Feature Details View
        //go back to the features list and make sure next and prev features work
        log("Testing showFeatureDetails...");
        clickAndWait(Locator.linkWithText(project));
        clickAndWait(Locator.linkWithText(run2Title));
        setFilter(DATAREGION_FEATURES, "MS2ConnectivityProbability", "Is Greater Than or Equal To", "0.90");
        setFilter(DATAREGION_FEATURES, "Scan", "Equals", "1948");
        clickAndWait(Locator.linkWithText("details"));

        assertCharts();
        assertChartRendered(Locator.imageWithSrc("type=bubble", true));
        Assert.assertTrue(isButtonDisabled("<< Previous Feature"));

        //test next/prev buttons
        log("Testing Prev/Next buttons on feature details");
        clickButton("Next Feature >>");
        Assert.assertTrue(isButtonDisabled("Next Feature >>"));
        assertChartRendered(Locator.imageWithSrc("type=bubble", true));
        clickButton("<< Previous Feature");
        Assert.assertTrue(isButtonDisabled("<< Previous Feature"));
        assertChartRendered(Locator.imageWithSrc("type=bubble", true));

        log("showFeatureDetails.view OK");

        clickAndWait(Locator.linkWithText(project));
        log("Finsihed testing features views.");
    }

    protected void assertCharts()
    {
        assertImagePresentWithSrc("type=elution", true);
        assertImagePresentWithSrc("type=spectrum", true);
        assertImagePresentWithSrc("type=bubble", true);
    }

    protected void assertChartRendered(Locator.XPathLocator loc)
    {
        String src = selenium.getValue(loc.toString() + "/@src");
        String urlCur = getURL().toString();
        String base = urlCur.substring(0, urlCur.indexOf("showFeatureDetails.view"));

        selenium.open(base + src.substring(src.indexOf("showChart.view?")));
        Assert.assertTrue(200 == getResponseCode());
        selenium.open(urlCur);
    }

    protected void doCleanup(boolean afterTest) throws TestTimeoutException
    {
        log("Cleaning up MS1 BVT...");
        deleteProject(getProjectName(), afterTest);
        try{purgeFiles();}
        catch(Throwable ignore) {}
        log("MS1 BVT cleaned up successfully.");
    }

    protected void purgeFiles()
    {
        goToAdminConsole();
        clickAndWait(Locator.linkWithText("ms1"));
        if(isNavButtonPresent("Purge Deleted MS1 Data Now"))
        {
            log("Purging MS1 Test data files...");
            clickButton("Purge Deleted MS1 Data Now");

            int iters = 0;
            while(isTextPresent("MS1 data is currently being purged"))
            {
                log("Wating for purge to complete...");
                sleep(3000);
                ++iters;
                refresh();
            }

            if(iters > 100)
                log("WARNING: Purging of MS1 BVT data took more than 5 minutes. Consider using smaller files.");

            log("MS1 data successfully purged.");
        }
    }

    public String getAssociatedModuleDirectory()
    {
        return "server/modules/ms1";
    }

    @Override
    protected String getProjectName()
    {
        return PROJ_MAIN;
    }
}
