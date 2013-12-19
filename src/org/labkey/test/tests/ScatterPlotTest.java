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

import org.junit.experimental.categories.Category;
import org.labkey.test.Locator;
import org.labkey.test.TestTimeoutException;
import org.labkey.test.categories.BVT;
import org.labkey.test.categories.Reports;
import org.labkey.test.util.LogMethod;

import static org.junit.Assert.*;

/**
 * User: tchadick
 * Date: 6/11/13
 */
@Category({BVT.class, Reports.class})
public class ScatterPlotTest extends GenericChartsTest
{
    protected static final String DEVELOPER_USER = "developer_user1@report.test";

    @Override
    protected void doCleanup(boolean afterTest) throws TestTimeoutException
    {
        deleteUsers(afterTest, DEVELOPER_USER);
        super.doCleanup(afterTest);
    }

    @LogMethod
    protected void testPlots()
    {
        doManageViewsScatterPlotTest();
        doDataRegionScatterPlotTest();
        doQuickChartScatterPlotTest();
        doCustomizeScatterPlotTest(); // Uses scatter plot created by doDataRegionScatterPlotTest()
        doPointClickScatterPlotTest(); // Uses scatter plot created by doManageViewsScatterPlotTest()
        doDeleteMeasureTest(); // Uses scatter plot created by doCustomizeScatterPlotTest()
        doDeleteQueryTest(); // Uses scatter plot created by doCustomizeScatterPlotTest(), deletes physical exam query.
    }

    private static final String SCATTER_PLOT_MV_1 = "60\n70\n80\n90\n100\n110\n60\n80\n100\n120\n140\n160\n180\n200\nAPX-1: Abbreviated Physical Exam - 1. Weight\n4. Pulse\n1. Weight";
    private static final String SCATTER_PLOT_MV_2 = "Mice A\nNot in Cat Mice Let\nMice B\nMice C\n40.0\nTest Title\nTestXAxis\nTestYAxis";
    private static final String SCATTER_PLOT_NAME_MV = "ManageViewsScatterPlot";
    private static final String SCATTER_PLOT_DESC_MV = "This scatter plot was created through the manage views UI";
    @LogMethod
    private void doManageViewsScatterPlotTest()
    {
        clickProject(getProjectName());
        clickFolder(getFolderName());
        goToManageViews();
        clickAddReport("Scatter Plot");

        _extHelper.waitForExtDialog("Select Chart Query");
        //TODO: weird timing with these combo scatteres.
        //Try once bug fixed: 15520: Scatter Plot - Allows selection of invalid schema/Query combination
        //_extHelper.selectExt4ComboBoxItem("Schema", "assay");
        //_extHelper.selectExt4ComboBoxItem("Query", "AssayList");
        //_extHelper.selectExt4ComboBoxItem("Schema", "study");
        _extHelper.selectExt4ComboBoxItem("Query", "APX-1 (APX-1: Abbreviated Physical Exam)");

        // Todo: put better wait here
        sleep(5000);
        _ext4Helper.clickWindowButton("Select Chart Query", "Save", 0, 0);
        _extHelper.waitForExtDialog("Y Axis");
        waitForText("1. Weight", WAIT_FOR_JAVASCRIPT);
        click(Locator.xpath(_extHelper.getExtDialogXPath("Y Axis") + "//div[text()='1. Weight']"));
        _ext4Helper.clickWindowButton("Y Axis", "Ok", 0, 0);
        _extHelper.waitForExtDialog("X Axis");
        waitForText("4. Pulse", WAIT_FOR_JAVASCRIPT);
        click(Locator.xpath(_extHelper.getExtDialogXPath("X Axis") + "//div[text()='4. Pulse']"));
        clickDialogButtonAndWaitForMaskToDisappear("X Axis", "Ok");

        //Verify scatter plot
        // getText(Locator.css("svg"))
        assertSVG(SCATTER_PLOT_MV_1);

        log("Set Plot Title");
        goToAxisTab("APX-1: Abbreviated Physical Exam");
        _extHelper.waitForExtDialog("Main Title");
        setFormElement(Locator.name("chart-title-textfield"), CHART_TITLE);
        waitForElement(Locator.css(".revertMainTitle:not(.x4-disabled)"));
        clickDialogButtonAndWaitForMaskToDisappear("Main Title", "OK");
        waitForText(CHART_TITLE);

        log("Set Y Axis");
        goToAxisTab("1. Weight");
        _extHelper.waitForExtDialog("Y Axis");
        click(Locator.ext4Radio("log"));
        waitForText("2. Body Temp", WAIT_FOR_JAVASCRIPT);
        click(Locator.xpath(_extHelper.getExtDialogXPath("Y Axis") + "//div[text()='2. Body Temp']"));
        setFormElement(Locator.name("label"), "TestYAxis");
        clickDialogButtonAndWaitForMaskToDisappear("Y Axis", "Ok");
        waitForText("TestYAxis");

        log("Set X Axis");
        goToAxisTab("4. Pulse");
        _extHelper.waitForExtDialog("X Axis");
        click(Locator.ext4Radio("log"));
        waitForText(MOUSE_GROUP_CATEGORY, WAIT_FOR_JAVASCRIPT);
        click(Locator.xpath(_extHelper.getExtDialogXPath("X Axis") + "//div[text()='" + MOUSE_GROUP_CATEGORY + "']"));
        _extHelper.setExtFormElementByLabel("X Axis", "Label:", "TestXAxis");
        clickDialogButtonAndWaitForMaskToDisappear("X Axis", "Ok");
        waitForText("TestXAxis");

        assertSVG(SCATTER_PLOT_MV_2);

        clickButton("Save", 0);
        _extHelper.waitForExtDialog("Save");
        //Verify name requirement
        _ext4Helper.clickWindowButton("Save", "Save", 0, 0);
        _extHelper.waitForExtDialog("Error");
        _ext4Helper.clickWindowButton("Error", "OK", 0, 0);
        _extHelper.waitForExtDialogToDisappear("Error");

        //Test cancel button
        setFormElement("reportName", "TestReportName");
        setFormElement("reportDescription", "TestReportDescription");
        _ext4Helper.clickWindowButton("Save", "Cancel", 0, 0);
        assertTextNotPresent("TestReportName");

        savePlot(SCATTER_PLOT_NAME_MV, SCATTER_PLOT_DESC_MV);
    }

    private static final String SCATTER_PLOT_DR_1 = "60\n65\n70\n75\n80\n85\n90\n50\n55\n60\n65\n70\n75\n80\n85\n90\n95\n100\n105\n110\nAPX-1: Abbreviated Physical Exam - 1. Weight\n4. Pulse\n1. Weight";
    private static final String SCATTER_PLOT_DR_2 = "60\n70\n80\n90\n100\n110\n60\n80\n100\n120\n140\n160\n180\n200\nAPX-1: Abbreviated Physical Exam - 1. Weight\n4. Pulse\n1. Weight";
    private static final String SCATTER_PLOT_NAME_DR = "DataRegionScatterPlot";
    private static final String SCATTER_PLOT_DESC_DR = "This scatter plot was created through a data region's 'Views' menu";
    /// Test Scatter Plot created from a filtered data region.
    @LogMethod
    private void doDataRegionScatterPlotTest()
    {
        clickProject(getProjectName());
        clickFolder(getFolderName());
        clickAndWait(Locator.linkWithText("APX-1: Abbreviated Physical Exam"));
        setFilter("Dataset", "APXpulse", "Is Less Than", "100");
        _extHelper.clickMenuButton("Charts", "Create Scatter Plot");

        _extHelper.waitForExtDialog("Y Axis");
        waitForText("1. Weight", WAIT_FOR_JAVASCRIPT);
        click(Locator.xpath(_extHelper.getExtDialogXPath("Y Axis") + "//div[text()='1. Weight']"));
        _ext4Helper.clickWindowButton("Y Axis", "Ok", 0, 0);
        _extHelper.waitForExtDialog("X Axis");
        waitForText("4. Pulse", WAIT_FOR_JAVASCRIPT);
        click(Locator.xpath(_extHelper.getExtDialogXPath("X Axis") + "//div[text()='4. Pulse']"));
        clickDialogButtonAndWaitForMaskToDisappear("X Axis", "Ok");

        //Verify scatter plot
        assertSVG(SCATTER_PLOT_DR_1);

        //Change filter and check scatter plot again
        clickButton("View Data", 0);
        clearFilter("Dataset", "APXpulse", 0);
        waitForText("36.0"); // Body temp for filtered out row
        clickButton("View Chart", 0);
        _ext4Helper.waitForMaskToDisappear();
        assertSVG(SCATTER_PLOT_DR_2);

        log("Verify point stying");

        savePlot(SCATTER_PLOT_NAME_DR, SCATTER_PLOT_DESC_DR);
    }

    private static final String SCATTER_PLOT_QC = "0.0\n200000.0\n400000.0\n600000.0\n800000.0\n1000000.0\n1200000.0\n0.0\n10000000.0\n20000000.0\n30000000.0\n40000000.0\n50000000.0\n60000000.0\n70000000.0\n80000000.0\n90000000.0\n100000000.0\n110000000.0\n120000000.0\nTypes - Double\nInteger\nDouble";
    private static final String SCATTER_PLOT_NAME_QC = "QuickChartScatterPlot";
    private static final String SCATTER_PLOT_DESC_QC = "This scatter plot was created through the 'Quick Chart' column header menu option";
    @LogMethod
    private void doQuickChartScatterPlotTest()
    {
        clickProject(getProjectName());
        clickFolder(getFolderName());
        clickAndWait(Locator.linkWithText("Types"));

        createQuickChart("Dataset", "dbl");

        log("Set X Axis");
        goToAxisTab("Cohort");
        _extHelper.waitForExtDialog("X Axis");
        waitForElement(Locator.xpath(_extHelper.getExtDialogXPath("X Axis") + "//div[text()='Integer']"));
        click(Locator.xpath(_extHelper.getExtDialogXPath("X Axis") + "//div[text()='Integer']"));
        clickDialogButtonAndWaitForMaskToDisappear("X Axis", "Ok");

        clickOptionButtonAndWaitForDialog("Options", "Plot Options");
        _extHelper.selectExt4ComboBoxItem("Plot Type", "Scatter Plot");
        clickDialogButtonAndWaitForMaskToDisappear("Plot Options", "OK");

        assertSVG(SCATTER_PLOT_QC);

        savePlot(SCATTER_PLOT_NAME_QC, SCATTER_PLOT_DESC_QC);
    }

    private static final String SCATTER_PLOT_CUSTOMIZED_COLORS = "60\n70\n80\n90\n100\n110\n60\n80\n100\n120\n140\n160\n180\n200\nAPX-1: Abbreviated Physical Exam - 1. Weight\n4. Pulse\n1. Weight\n0\nNormal\nNot Done";
    private static final String SCATTER_PLOT_CUSTOMIZED_SHAPES = "60\n70\n80\n90\n100\n110\n60\n80\n100\n120\n140\n160\n180\n200\nAPX-1: Abbreviated Physical Exam - 1. Weight\n4. Pulse\n1. Weight\n0\nnormal\nabnormal/insignificant\nabnormal/significant";
    private static final String SCATTER_PLOT_CUSTOMIZED_BOTH = "60\n70\n80\n90\n100\n110\n60\n80\n100\n120\n140\n160\n180\n200\nAPX-1: Abbreviated Physical Exam - 1. Weight\n4. Pulse\n1. Weight\n0\nNormal\nNot Done\n0\nnormal\nabnormal/insignificant\nabnormal/significant";

    @LogMethod
    private void doCustomizeScatterPlotTest()
    {
        clickReportGridLink(SCATTER_PLOT_NAME_DR);
        _ext4Helper.waitForMaskToDisappear();

        // verify that we originally are in view mode and can switch to edit mode
        assertElementNotPresent(Locator.ext4Button("Grouping"));
        assertElementNotPresent(Locator.ext4Button("Save"));
        waitAndClickButton("Edit", WAIT_FOR_PAGE); // switch to edit mode
        _ext4Helper.waitForMaskToDisappear();
        assertElementNotPresent(Locator.ext4Button("Edit"));

        // Verify default styling for point at origin - blue circles

        waitForElement(Locator.css("svg > g > a > path"));
        // TODO: We changed all shapes to be paths, even circles, so we need to find a new way to determine if the colors changed.
//        assertEquals("Scatter points doin't have expected initial color", "#3366ff", getAttribute(Locator.css("svg > a > path"), "fill"));

        // Enable Grouping - Colors
        log("Group with colors");
        clickOptionButtonAndWaitForDialog("Grouping", "Grouping Options");
        click(Locator.id("colorCategory-inputEl"));
        _extHelper.selectExt4ComboBoxItem("Color Category:", "7. Neck");
        click(Locator.ext4Radio("Single shape"));
        clickDialogButtonAndWaitForMaskToDisappear("Grouping Options", "OK");

        assertSVG(SCATTER_PLOT_CUSTOMIZED_COLORS);
        // Verify custom styling for point at origin (APXpulse: 60, APXwtkg: 48) - pink triangle
//        assertEquals("Point at (70, 67) was an unexpected color", "#8da0cb", getAttribute(Locator.css("svg > g > a:nth-of-type(15) > *"), "fill"));
        // TODO: We changed all shapes to be paths, even circles, so we need to find a new way to determine if the shape changed.
//        assertTrue("Point at (70, 67) was an unexpected shape", isElementPresent(Locator.css("svg > a:nth-of-type(15) > circle")));
        // Verify custom styling for another point (APXpulse: 92, APXwtkg: 89) - teal circle
//        assertEquals("Circle at (92, 89) was an unexpected color", "#fc8d62", getAttribute(Locator.css("svg > g > a:nth-of-type(25) > *"), "fill"));
        // TODO: We changed all shapes to be paths, even circles, so we cant use circle
//        assertTrue("Circle at (92, 89) was an unexpected shape", isElementPresent(Locator.css("svg > a:nth-of-type(25) > circle")));


        // Enable Grouping - Shapes
        log("Group with shapes");
        clickOptionButtonAndWaitForDialog("Grouping", "Grouping Options");
        click(Locator.ext4Radio("With a single color"));
        click(Locator.id("shapeCategory-inputEl"));
        _extHelper.selectExt4ComboBoxItem("Point Category:", "16. Evaluation Summary");
        clickDialogButtonAndWaitForMaskToDisappear("Grouping Options", "OK");

        assertSVG(SCATTER_PLOT_CUSTOMIZED_SHAPES);
        // Verify custom styling for point at origin (APXpulse: 60, APXwtkg: 48) - pink triangle
//        assertEquals("Point at (60, 48) was an unexpected color", "#3366ff", getAttribute(Locator.css("svg > g > a:nth-of-type(26) > *"), "fill"));
//        assertEquals("Point at (60, 48) was an unexpected shape", "M-5,-5L5,-5L5,5L-5,5Z", getAttribute(Locator.css("svg > g > a:nth-of-type(26) > *"), "d"));
        // Verify custom styling for another point (APXpulse: 92, APXwtkg: 89) - teal square
//        assertEquals("Diamond at (92, 89) was an unexpected color", "#3366ff", getAttribute(Locator.css("svg > g > a:nth-of-type(25) > *"), "fill"));
        // TODO: We now use paths for all shapes, as a result width and height will never be available. Need to find another way to determine height/width.
//        assertEquals("Square at (92, 89) was an unexpected width", "10", getAttribute(Locator.css("svg > a:nth-of-type(25) > *"), "width"));
//        assertEquals("Square at (92, 89) was an unexpected height", "10", getAttribute(Locator.css("svg > a:nth-of-type(25) > *"), "height"));


        // Enable Grouping - Shapes & Colors
        log("Group with both");
        clickOptionButtonAndWaitForDialog("Grouping", "Grouping Options");
        click(Locator.id("colorCategory-inputEl"));
        click(Locator.id("shapeCategory-inputEl"));
        _extHelper.selectExt4ComboBoxItem("Point Category:", "16. Evaluation Summary");
        clickDialogButtonAndWaitForMaskToDisappear("Grouping Options", "OK");

        assertSVG(SCATTER_PLOT_CUSTOMIZED_BOTH);
        // Verify custom styling for point at origin (APXpulse: 70, APXwtkg: 67) - blue triangle
//        assertEquals("Point at (70, 67) was an unexpected color", "#8da0cb", getAttribute(Locator.css("svg > g > a:nth-of-type(15) > *"), "fill"));
//        assertEquals("Point at (70, 67) was an unexpected shape", "M0,5L5,-5L-5,-5Z", getAttribute(Locator.css("svg > g > a:nth-of-type(15) > *"), "d"));
        // Verify custom styling for another point (APXpulse: 92, APXwtkg: 89) - red diamond
//        assertEquals("Square at (92, 89) was an unexpected color", "#fc8d62", getAttribute(Locator.css("svg > g > a:nth-of-type(25) > *"), "fill"));
        // TODO: We now use paths for all shapes, as a result width and height will never be available. Need to find another way to determine height/width.
//        assertEquals("Square at (92, 89) was an unexpected width", "10", getAttribute(Locator.css("svg > a:nth-of-type(25) > *"), "width"));
//        assertEquals("Square at (92, 89) was an unexpected height", "10", getAttribute(Locator.css("svg > a:nth-of-type(25) > *"), "height"));

        savePlot(SCATTER_PLOT_NAME_DR + " Colored", SCATTER_PLOT_DESC_DR + " Colored");
    }

    @LogMethod
    private void doDeleteMeasureTest()
    {
        log("Remove color and shape measures.");
        clickProject(getProjectName());
        clickFolder(getFolderName());

        clickAndWait(Locator.linkContainingText("APX-1: Abbreviated Physical Exam"));
        clickButton("Manage Dataset");
        clickButton("Edit Definition");

        waitForText("Dataset Fields");
        _listHelper.deleteField("Dataset Fields", 12);
        _listHelper.deleteField("Dataset Fields", 31);
        clickButton("Save");

        log("Verify proper error messages for removed measures.");
        click(Locator.linkContainingText("Clinical and Assay Data"));
        waitForText(SCATTER_PLOT_NAME_DR + " Colored");
        clickAndWait(Locator.linkContainingText(SCATTER_PLOT_NAME_DR + " Colored"));
        _ext4Helper.waitForMaskToDisappear();

        waitAndClickButton("Edit", WAIT_FOR_PAGE); // switch to edit mode
        _ext4Helper.waitForMaskToDisappear();

        waitForText("\"APXneck\", is not available. It may have been deleted or renamed.");
        assertTextPresent("\"APXcemh\", is not available. It may have been deleted or renamed.");

        clickOptionButtonAndWaitForDialog("Grouping", "Grouping Options");
        click(Locator.ext4Radio("With a single color"));
        click(Locator.ext4Radio("Single shape"));
        clickDialogButtonAndWaitForMaskToDisappear("Grouping Options", "OK");

        log("Set X Axis to categorical measure.");
        goToAxisTab("4. Pulse");
        _extHelper.waitForExtDialog("X Axis");
        waitForElement(Locator.xpath(_extHelper.getExtDialogXPath("X Axis") + "//div[text()='Form Language']"));
        click(Locator.xpath(_extHelper.getExtDialogXPath("X Axis") + "//div[text()='Form Language']"));
        clickDialogButtonAndWaitForMaskToDisappear("X Axis", "Ok");

        savePlot();

        log("Remove x-axis measure.");
        clickProject(getProjectName());
        clickFolder(getFolderName());

        clickAndWait(Locator.linkContainingText("APX-1: Abbreviated Physical Exam"));
        clickButton("Manage Dataset");
        clickButton("Edit Definition");

        waitForText("Dataset Fields");
        _listHelper.deleteField("Dataset Fields", 35);
        clickButton("Save");

        log("Verify missing measure error message.");
        click(Locator.linkContainingText("Clinical and Assay Data"));
        waitForText(SCATTER_PLOT_NAME_DR + " Colored");
        clickAndWait(Locator.linkContainingText(SCATTER_PLOT_NAME_DR + " Colored"));
        _ext4Helper.waitForMaskToDisappear();

        // Issue 18186
        // When not in edit mode, there shouldnt be a pop up message.
        waitForText("The measure Form Language was not found. It may have been renamed or removed.");
        clickButton("Edit");
        waitForText("The measure Form Language was not found. It may have been renamed or removed.");
        clickButton("OK", 0);
    }

    @LogMethod
    private void doDeleteQueryTest()
    {
        log("Remove color and shape measures.");
        clickProject(getProjectName());
        clickFolder(getFolderName());

        clickAndWait(Locator.linkContainingText("APX-1: Abbreviated Physical Exam"));
        clickButton("Manage Dataset");
        prepForPageLoad();
        clickButton("Delete Dataset", 0);
        assertAlertContains("Are you sure you want to delete this dataset?");
        newWaitForPageToLoad();
        waitForText("The study schedule defines"); // text on the Manage Datasets page

        click(Locator.linkContainingText("Clinical and Assay Data"));
        waitForText(SCATTER_PLOT_NAME_DR + " Colored");
        clickAndWait(Locator.linkContainingText(SCATTER_PLOT_NAME_DR + " Colored"));
        _ext4Helper.waitForMaskToDisappear();

        clickButton("Edit");
        _ext4Helper.waitForMaskToDisappear();
        waitForText("The source dataset, list, or query may have been deleted.");

        Integer buttonsCount = getElementCount(Locator.xpath("//div[contains(@id, 'generic-report-div')]//div/a[contains(@class, 'x4-btn')]"));
        Integer disabledButtonsCount = getElementCount(Locator.xpath("//div[contains(@id, 'generic-report-div')]//div/a[contains(@class, 'x4-btn') and contains(@class, 'x4-item-disabled')]"));
        assertTrue("Only the help and export buttons should be enabled. More than two buttons were enabled.", 2 == (buttonsCount - disabledButtonsCount));
    }

    private static final String TEST_DATA_API_PATH = "server/test/data/api";

    @LogMethod
    private void doPointClickScatterPlotTest()
    {
        clickReportGridLink(SCATTER_PLOT_NAME_MV);
        _ext4Helper.waitForMaskToDisappear();

        // verify that we originally are in view mode and can switch to edit mode
        assertElementNotPresent(Locator.ext4Button("Grouping"));
        assertElementNotPresent(Locator.ext4Button("Save"));
        waitAndClickButton("Edit", WAIT_FOR_PAGE); // switch to edit mode
        _ext4Helper.waitForMaskToDisappear();
        assertElementNotPresent(Locator.ext4Button("Edit"));

        log("Check Scatter Plot Point Click Function (Developer Only)");
        // open the developer panel and verify that it is disabled by default
        assertElementPresent(Locator.ext4Button("Developer"));
        clickOptionButtonAndWaitForDialog("Developer", "Developer Options");
        assertElementPresent(Locator.ext4Button("Enable"));
        assertElementNotPresent(Locator.ext4Button("Disable"));
        // enable the feature and verify that you can switch tabs
        click(Locator.ext4Button("Enable"));
        _ext4Helper.clickTabContainingText("Help");
        assertTextPresentInThisOrder("Your code should define a single function", "data:", "measureInfo:", "clickEvent:");
        assertTextPresentInThisOrder("YAxisMeasure:", "XAxisMeasure:", "ColorMeasure:", "PointMeasure:");
        _ext4Helper.clickTabContainingText("Source");
        String fn = _extHelper.getCodeMirrorValue("point-click-fn-textarea");
        assertTrue("Default point click function not inserted in to editor", fn.startsWith("function (data, measureInfo, clickEvent) {"));
        // apply the default point click function
        clickDialogButtonAndWaitForMaskToDisappear("Developer Options", "OK");
        Locator svgCircleLoc = Locator.css("svg a path");
        waitForElement(svgCircleLoc);
        fireEvent(svgCircleLoc, SeleniumEvent.click);
        _extHelper.waitForExtDialog("Data Point Information");
        click(Locator.ext4Button("OK"));
        // open developer panel and test JS function validation
        clickOptionButtonAndWaitForDialog("Developer", "Developer Options");
        _extHelper.setCodeMirrorValue("point-click-fn-textarea", "");
        _ext4Helper.clickWindowButton("Developer Options", "OK", 0, 0);
        assertTextPresent("Error: the value provided does not begin with a function declaration.");
        _extHelper.setCodeMirrorValue("point-click-fn-textarea", "function(){");
        _ext4Helper.clickWindowButton("Developer Options", "OK", 0, 0);
        assertTextPresent("Error parsing the function:");
        click(Locator.ext4Button("Disable"));
        _extHelper.waitForExtDialog("Confirmation...");
        _ext4Helper.clickWindowButton("Confirmation...", "Yes", 0, 0);
        assertTextNotPresent("Error");
        // test use-case to navigate to query page on click
        click(Locator.ext4Button("Enable"));
        String function = getFileContents(TEST_DATA_API_PATH + "/scatterPlotPointClickTestFn.js");
        _extHelper.setCodeMirrorValue("point-click-fn-textarea", function);
        clickDialogButtonAndWaitForMaskToDisappear("Developer Options", "OK");
        savePlot(SCATTER_PLOT_NAME_MV + " PointClickFn", SCATTER_PLOT_DESC_MV + " PointClickFn");
        prepForPageLoad();
        fireEvent(svgCircleLoc, SeleniumEvent.click);
        newWaitForPageToLoad();
        waitForText("Query Schema Browser");
        assertTextPresent("APX-1: Abbreviated Physical Exam");
        // verify that only developers can see the button to add point click function
        createUser(DEVELOPER_USER, null);
        clickProject(getProjectName());
        enterPermissionsUI();
        setUserPermissions(DEVELOPER_USER, "Editor");
        impersonate(DEVELOPER_USER);
        clickProject(getProjectName());
        clickFolder(getFolderName());
        clickAndWait(Locator.linkWithText(SCATTER_PLOT_NAME_MV + " PointClickFn"));
        clickAndWait(Locator.ext4Button("Edit"), WAIT_FOR_PAGE);
        waitForText(CHART_TITLE);
        pushLocation();
        assertElementNotPresent(Locator.ext4Button("Developer"));
        prepForPageLoad();
        fireEvent(svgCircleLoc, SeleniumEvent.click);
        newWaitForPageToLoad();
        waitForText("APX-1: Abbreviated Physical Exam");
        stopImpersonating();
        // give DEVELOPER_USER developer perms and try again
        createSiteDeveloper(DEVELOPER_USER);
        impersonate(DEVELOPER_USER);
        popLocation();
        waitForText(CHART_TITLE);
        assertElementPresent(Locator.ext4Button("Developer"));
        stopImpersonating();
    }
}
