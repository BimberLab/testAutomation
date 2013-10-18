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
package org.labkey.test.tests;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.labkey.test.Locator;
import org.labkey.test.categories.DailyB;
import org.labkey.test.categories.Reports;
import org.labkey.test.util.PortalHelper;

import java.io.File;
import java.util.ArrayList;

/**
 * User: Cory
 * Date: 10/4/13
 *
 * This test imports a folder archive that has 2 subfolders (a date based study and a visit based study) which have been
 * trimmed down to contain just the datasets/queries/etc. that are needed for the Time Chart reports in the study folders
 * that are imported (2 in the visit based study and 10 in the date based study).
 *
 * It verifies the contents of the imported time chart reports by checking the SVG content of one or more plots in the
 * report by checking the main title, axis label, axis ranges, and legend text. If the chart has a point click fn, it
 * tests that by clicking on a data point. And it checks the number of rows in the "view data" grid to verify filters
 * on the report.
 *
 * // TODO: add verification for plots with aggregate lines that have error bars
 * // TODO: add verification for plot line with and whether or not the data points are shown
 */
@Category({DailyB.class, Reports.class})
public class TimeChartImportTest extends TimeChartTest
{
    private static final String MULTI_FOLDER_ZIP = "/sampledata/study/TimeChartTesting.folder.zip";
    private static final String EXPORT_TEST_FOLDER = "exportTestFolder";
    private static final String DATE_STUDY_FOLDER_NAME = "Date Based Study";
    private static ArrayList<TimeChartInfo> DATE_CHARTS = new ArrayList<>();
    private static final String VISIT_STUDY_FOLDER_NAME = "Visit Based Study";
    private static ArrayList<TimeChartInfo> VISIT_CHARTS = new ArrayList<>();

    @BeforeClass
    public static void doSetup() throws Exception
    {
        TimeChartImportTest initTest = new TimeChartImportTest();
        initTest.doCleanup(false);

        initTest._containerHelper.createProject(initTest.getProjectName(), null);
        initTest.importFolderFromZip(new File(getLabKeyRoot(), MULTI_FOLDER_ZIP));
        initTest._containerHelper.createSubfolder(initTest.getProjectName(), EXPORT_TEST_FOLDER, "Collaboration");
        initTest.populateChartConfigs();

        currentTest = initTest;
    }

    @Test @Ignore
    public void testSteps(){}

    @Override
    protected void doVerifySteps(){}

    @Override
    protected void doCreateSteps(){}

    private void populateChartConfigs()
    {
        VISIT_CHARTS.add(new TimeChartInfo(
                "One Measure: visit based plot per participant", 17, 47, false,
                new String[]{
                        "Created with Rapha\u00ebl 2.1.0\nAbbr Phy Exam: 999320016\n1 week Post-V#1\nInt. Vis. %{S.1.1} .%{S.2.1}\nGrp1:F/U/Grp2:V#2\nG1: 6wk/G2: 2wk\n6 week Post-V#2\nG1: V#2/G2: V#3\nInt. Vis. %{S.1.1} .%{S.2.1}\nInt. Vis. %{S.1.1} .%{S.2.1}\n6 wk Post-V#2/V#3\nVisit Label\nTemperature: body\n32.5\n33.0\n33.5\n34.0\n34.5\n35.0\n35.5\n36.0\n36.5\n37.0\n999320016",
                        "Created with Rapha\u00ebl 2.1.0\nAbbr Phy Exam: 999320518\n1 week Post-V#1\nInt. Vis. %{S.1.1} .%{S.2.1}\nGrp1:F/U/Grp2:V#2\nG1: 6wk/G2: 2wk\n6 week Post-V#2\nG1: V#2/G2: V#3\nInt. Vis. %{S.1.1} .%{S.2.1}\nInt. Vis. %{S.1.1} .%{S.2.1}\n6 wk Post-V#2/V#3\nVisit Label\nTemperature: body\n37.5\n38.0\n38.5\n39.0\n39.5\n999320518"
                }
        ));

        VISIT_CHARTS.add(new TimeChartInfo(
                "Two Measure: group mean with one plot per dimension", 2, 38, false,
                new String[]{
                        "Created with Rapha\u00ebl 2.1.0\nAPX-1: Abbreviated Physical Exam: 1. Weight\n1 week Post-V#1\nInt. Vis. %{S.1.1} .%{S.2.1}\nGrp1:F/U/Grp2:V#2\nG1: 6wk/G2: 2wk\n6 week Post-V#2\n1 wk Post-V#2/V#3\n2 wk Post-V#2/V#3\n4 wk Post-V#2/V#3\nVisit\n1. Weight\n80.0\n100.0\n120.0\n140.0\n160.0\n180.0\n200.0\nGroup 1\nFemale\nMale",
                        "Created with Rapha\u00ebl 2.1.0\nAPX-1: Abbreviated Physical Exam: 2. Body Temp\n1 week Post-V#1\nInt. Vis. %{S.1.1} .%{S.2.1}\nGrp1:F/U/Grp2:V#2\nG1: 6wk/G2: 2wk\n6 week Post-V#2\n1 wk Post-V#2/V#3\n2 wk Post-V#2/V#3\n4 wk Post-V#2/V#3\nVisit\n2. Body Temp\n33.0\n34.0\n35.0\n36.0\n37.0\n38.0\n39.0\n40.0\nGroup 1\nFemale\nMale"
                }
        ));

        DATE_CHARTS.add(new TimeChartInfo(
                "One Measure: filtered for pregnancy records", 1, 6, false,
                new String[]{
                        "Created with Rapha\u00ebl 2.1.0\nPhysical Exam\n50\n100\n150\n200\n250\nDays Since Start Date\nPulse\n58.0\n60.0\n62.0\n64.0\n66.0\n68.0\n70.0\n72.0\n74.0\n76.0\n78.0\n249320489\n249320897"
                }
        ));

        DATE_CHARTS.add(new TimeChartInfo(
                "Four Measures: one axis with point click fn enabled", 1, 17, true,
                new String[]{
                        "Created with Rapha\u00ebl 2.1.0\nLuminexAssay, Lab Results, GenericAssay, Physical Exam\n50\n100\n150\n200\n250\n300\n350\nDays Since Start Date\nFI, CD4+ (cells/mm3), M1, Weight (kg)\n200\n400\n600\n800\n1000\n1200\n1400\n249318596 Weight (kg)\n249318596 ABI-QSTAR\n249318596 CD4+(cells/mm3)\n249318596 TNF-alpha(40)"
                }
        ));

        DATE_CHARTS.add(new TimeChartInfo(
                "Four Measures: one axis with x-axis range and interval changed", 1, 17, true,
                new String[]{
                        "Created with Rapha\u00ebl 2.1.0\nLuminexAssay, Lab Results, GenericAssay, Physical Exam\n0\n5\n10\n15\n20\n25\n30\n35\nMonths\nFI, CD4+ (cells/mm3), M1, Weight (kg)\n200\n400\n600\n800\n1000\n1200\n1400\n249318596 Weight (kg)\n249318596 ABI-QSTAR\n249318596 CD4+(cells/mm3)\n249318596 TNF-alpha(40)"
                }
        ));

        DATE_CHARTS.add(new TimeChartInfo(
                "One Measure: FI luminex IL-10 and IL-2 data by Analyte dimension", 2, 30, false,
                new String[]{
                        "Created with Rapha\u00ebl 2.1.0\nLuminex: IL-10 (23)\n0\n50\n100\n150\n200\nDays Since Start Date\nFI\n80.0\n800.0\n249318596\n249320107\n249320127\n249320489\n249320897\n249325717",
                        "Created with Rapha\u00ebl 2.1.0\nLuminex: IL-2 (3)\n0\n50\n100\n150\n200\nDays Since Start Date\nFI\n60.0\n600.0\n249318596\n249320107\n249320127\n249320489\n249320897\n249325717"
                }
        ));

        DATE_CHARTS.add(new TimeChartInfo(
                "One Measure: FI luminex with thin lines and no data points", 1, 5, false,
                new String[]{
                        "Created with Rapha\u00ebl 2.1.0\nLuminex\n50\n100\n150\n200\nDays Since Start Date\nFI\n70.0\n700.0\n249318596 TNF-alpha(40)\n249318596 IL-2 (3)\n249318596 IL-10 (23)"
                }
        ));

        DATE_CHARTS.add(new TimeChartInfo(
                "One Measure: y-axis log scale and manual range on right side", 1, 33, false,
                new String[]{
                        "Created with Rapha\u00ebl 2.1.0\nHIV Test Results\n0\n50\n100\n150\n200\n250\n300\n350\nDays Since Start Date\nViral Load Quantified (copies/ml)\n100.0\n1000.0\n10000.0\n100000.0\n1000000.0\n10000000.0\n249318596\n249320107\n249320127\n249320489\n249320897"
                }
        ));

        DATE_CHARTS.add(new TimeChartInfo(
                "One Measure: y-axis log scale and manual range", 1, 33, false,
                new String[]{
                        "Created with Rapha\u00ebl 2.1.0\nHIV Test Results\n0\n50\n100\n150\n200\n250\n300\n350\nDays Since Start Date\nViral Load Quantified (copies/ml)\n100.0\n1000.0\n10000.0\n100000.0\n1000000.0\n10000000.0\n249318596\n249320107\n249320127\n249320489\n249320897"
                }
        ));

        DATE_CHARTS.add(new TimeChartInfo(
                "Two Measures: cd4 left axis and vl right axis by participant", 3, 23, false,
                new String[]{
                        "Created with Rapha\u00ebl 2.1.0\nPTID: 249318596\n0\n50\n100\n150\n200\n250\n300\n350\nDays Since Start Date\nCD4+ (cells/mm3)\n200.0\n300.0\n400.0\n500.0\n600.0\n700.0\n800.0\n900.0\n1000.0\n1100.0\n1200.0\n1300.0\nViral Load Quantified (copies/ml)\n100.0\n1000.0\n10000.0\n100000.0\n1000000.0\n10000000.0\n249318596 CD4+(cells/mm3)\n249318596 Viral LoadQuantified (copies/ml)",
                        "Created with Rapha\u00ebl 2.1.0\nPTID: 249320127\n0\n50\n100\n150\n200\n250\n300\n350\nDays Since Start Date\nCD4+ (cells/mm3)\n200.0\n300.0\n400.0\n500.0\n600.0\n700.0\n800.0\n900.0\n1000.0\n1100.0\n1200.0\n1300.0\nViral Load Quantified (copies/ml)\n100.0\n1000.0\n10000.0\n100000.0\n1000000.0\n10000000.0\n249320127 CD4+(cells/mm3)\n249320127 Viral LoadQuantified (copies/ml)",
                        "Created with Rapha\u00ebl 2.1.0\nPTID: 249320897\n0\n50\n100\n150\n200\n250\n300\n350\nDays Since Start Date\nCD4+ (cells/mm3)\n200.0\n300.0\n400.0\n500.0\n600.0\n700.0\n800.0\n900.0\n1000.0\n1100.0\n1200.0\n1300.0\nViral Load Quantified (copies/ml)\n100.0\n1000.0\n10000.0\n100000.0\n1000000.0\n10000000.0\n249320897 CD4+(cells/mm3)\n249320897 Viral LoadQuantified (copies/ml)"
                }
        ));

        DATE_CHARTS.add(new TimeChartInfo(
                "Two Measure: all cohorts and groups", 8, 99, false,
                new String[]{
                        "Created with Rapha\u00ebl 2.1.0\nLab Results: Group 1: Accute HIV-1\n0\n50\n100\n150\n200\n250\n300\n350\nDays Since Start Date\nHemoglobin, Lymphs (cells/mm3)\n200.0\n400.0\n600.0\n800.0\n1000.0\n1200.0\n1400.0\n1600.0\n1800.0\n2000.0\n2200.0\nGroup 1: Accute HIV-1Lymphs (cells/mm3)\nGroup 1: Accute HIV-1Hemoglobin",
                        "Created with Rapha\u00ebl 2.1.0\nLab Results: Group 2: HIV-1 Negative\n0\n50\n100\n150\n200\n250\n300\n350\nDays Since Start Date\nHemoglobin, Lymphs (cells/mm3)\n200.0\n400.0\n600.0\n800.0\n1000.0\n1200.0\n1400.0\n1600.0\n1800.0\n2000.0\n2200.0\nGroup 2: HIV-1 NegativeLymphs (cells/mm3)\nGroup 2: HIV-1 NegativeHemoglobin"
                }
        ));

        DATE_CHARTS.add(new TimeChartInfo(
                "Two Measure: showing both individual lines and aggregate", 3, 50, false,
                new String[]{
                        "Created with Rapha\u00ebl 2.1.0\nLab Results: Group 1: Accute HIV-1\n0\n50\n100\n150\n200\n250\n300\n350\nDays Since Start Date\nHemoglobin, Lymphs (cells/mm3)\n20.0\n200.0\n249318596 Lymphs(cells/mm3)\n249320107 Lymphs(cells/mm3)\n249320489 Lymphs(cells/mm3)\nGroup 1: Accute HIV-1Lymphs (cells/mm3)\n249318596 Hemoglobin\n249320107 Hemoglobin\n249320489 Hemoglobin\nGroup 1: Accute HIV-1Hemoglobin",
                        "Created with Rapha\u00ebl 2.1.0\nLab Results: First ptid\n0\n50\n100\n150\n200\n250\n300\n350\nDays Since Start Date\nHemoglobin, Lymphs (cells/mm3)\n20.0\n200.0\n249318596 Lymphs(cells/mm3)\nFirst ptid Lymphs(cells/mm3)\n249318596 Hemoglobin\nFirst ptid Hemoglobin",
                        "Created with Rapha\u00ebl 2.1.0\nLab Results: Female\n0\n50\n100\n150\n200\n250\n300\n350\nDays Since Start Date\nHemoglobin, Lymphs (cells/mm3)\n20.0\n200.0\n2000.0\n249320107 Lymphs(cells/mm3)\n249320127 Lymphs(cells/mm3)\n249320489 Lymphs(cells/mm3)\n249320897 Lymphs(cells/mm3)\nFemale Lymphs(cells/mm3)\n249320107 Hemoglobin\n249320127 Hemoglobin\n249320489 Hemoglobin\n249320897 Hemoglobin\nFemale Hemoglobin"
                }
        ));
    }

    @Test
    public void verifyVisitBasedCharts()
    {
        goToProjectHome();
        clickFolder(VISIT_STUDY_FOLDER_NAME);
        for (TimeChartInfo chartInfo : VISIT_CHARTS)
        {
            clickTab("Clinical and Assay Data");
            waitForElement(Locator.linkWithText(chartInfo.getName()));
            clickAndWait(Locator.linkWithText(chartInfo.getName()));
            verifyTimeChartInfo(chartInfo, true);
        }
    }

    @Test
    public void verifyDateBasedCharts()
    {
        goToProjectHome();
        clickFolder(DATE_STUDY_FOLDER_NAME);
        for (TimeChartInfo chartInfo : DATE_CHARTS)
        {
            clickTab("Clinical and Assay Data");
            waitForElement(Locator.linkWithText(chartInfo.getName()));
            clickAndWait(Locator.linkWithText(chartInfo.getName()));
            verifyTimeChartInfo(chartInfo, true);
        }
    }

    private void verifyTimeChartInfo(TimeChartInfo info, boolean isTimeChartWizard)
    {
        log("Verify chart information: " + info.getName());

        waitForCharts(info.getCountSVGs());
        for (int i = 0; i < info.getSvg().length; i++)
        {
            log("Verify SVG for chart index " + i);
            assertSVG(info.getSvg()[i], i);
        }

        // verify that if the plot has a point click fn, the data points are clickable
        if (info.hasPointClickFn())
        {
            click(Locator.css("svg a circle"));
            _extHelper.waitForExtDialog("Data Point Information");
            waitAndClick(Locator.ext4Button("OK"));
        }

        if (!isTimeChartWizard)
            return;

        // verify that clicking the Export as Script button works
        _ext4Helper.clickExt4MenuButton(false, Locator.ext4Button("Export"), false, "Export as Script");
        _extHelper.waitForExtDialog("Export Script");
        waitAndClick(Locator.ext4Button("Close"));

        // verify that there is a PDF export for each plot
        _ext4Helper.clickExt4MenuButton(false, Locator.ext4Button("Export"), true, "Export as Script");
        assertElementPresent(Locator.xpath("//div[contains(@class, 'x4-menu-item-icon') and contains(@style, 'pdf.gif')]"), info.getCountSVGs());

        // verify the count of records in the view data grid
        clickButton("View Data", 0);
        waitForElement(Locator.paginationText(info.getGridCount()));
    }

    @Test
    public void verifyExportToScript()
    {
        log("Export Time Chart as Script and paste into Wiki");
        goToProjectHome();
        clickFolder(VISIT_STUDY_FOLDER_NAME);
        clickTab("Clinical and Assay Data");

        TimeChartInfo info = VISIT_CHARTS.get(0);
        waitForElement(Locator.linkWithText(info.getName()));
        clickAndWait(Locator.linkWithText(info.getName()));
        waitForCharts(info.getCountSVGs());

        _ext4Helper.clickExt4MenuButton(false, Locator.ext4Button("Export"), false, "Export as Script");
        _extHelper.waitForExtDialog("Export Script");
        String exportScript = _extHelper.getCodeMirrorValue("export-script-textarea");
        waitAndClick(Locator.ext4Button("Close"));

        clickFolder(EXPORT_TEST_FOLDER);
        new PortalHelper(this).addWebPart("Wiki");
        createNewWikiPage("HTML");
        setFormElement(Locator.name("name"), "timeChartExportTest");
        setWikiBody(exportScript);
        saveWikiPage();

        verifyTimeChartInfo(info, false);
    }

    @Override
    protected BrowserType bestBrowser()
    {
        return BrowserType.CHROME;
    }

    private class TimeChartInfo
    {
        private String _name;
        private int _countSVGs;
        private int _gridCount;
        private boolean _hasPointClickFn;
        private String[] _svg;

        public TimeChartInfo(String name, int countSVGs, int gridCount, boolean hasPointClickFn, String[] svg)
        {
            _name = name;
            _countSVGs = countSVGs;
            _gridCount = gridCount;
            _hasPointClickFn = hasPointClickFn;
            _svg = svg;
        }

        public String getName()
        {
            return _name;
        }

        public int getGridCount()
        {
            return _gridCount;
        }

        public String[] getSvg()
        {
            return _svg;
        }

        public int getCountSVGs()
        {
            return _countSVGs;
        }

        public boolean hasPointClickFn()
        {
            return _hasPointClickFn;
        }
    }
}
