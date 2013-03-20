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
import org.labkey.test.BaseWebDriverTest;
import org.labkey.test.Locator;
import org.labkey.test.SortDirection;
import org.labkey.test.TestTimeoutException;
import org.labkey.test.WebTestHelper;
import org.labkey.test.util.DataRegionTable;
import org.labkey.test.util.EscapeUtil;
import org.labkey.test.util.ListHelper;
import org.labkey.test.util.ListHelper.ListColumn;
import org.labkey.test.util.ListHelper.LookupInfo;
import org.labkey.test.util.LogMethod;
import org.openqa.selenium.By;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.labkey.test.util.ListHelper.ListColumnType.Integer;
import static org.labkey.test.util.ListHelper.ListColumnType.String;

/**
 * User: ulberge
 * Date: Jul 13, 2007
 */
public class ListTest extends BaseWebDriverTest
{
    protected final static String PROJECT_VERIFY = "ListVerifyProject" ;//+ TRICKY_CHARACTERS_FOR_PROJECT_NAMES;
    private final static String PROJECT_OTHER = "OtherListVerifyProject";
    protected final static String LIST_NAME_COLORS = TRICKY_CHARACTERS_NO_QUOTES + "Colors";
    protected final static ListHelper.ListColumnType LIST_KEY_TYPE = ListHelper.ListColumnType.String;
    protected final static String LIST_KEY_NAME = "Key";
    protected final static String LIST_KEY_NAME2 = "Color";
    protected final static String LIST_DESCRIPTION = "A list of colors and what they are like";
    protected final static String FAKE_COL1_NAME = "FakeName";
    protected final static String ALIASED_KEY_NAME = "Material";
    protected final static String HIDDEN_TEXT = "CantSeeMe";
    protected final static String DETAILS_BUTTON_NAME = "Show Grid";

    protected final ListColumn _listCol1Fake = new ListColumn(FAKE_COL1_NAME, FAKE_COL1_NAME, ListHelper.ListColumnType.String, "What the color is like");
    protected final ListColumn _listCol1 = new ListColumn("Desc", "Description", ListHelper.ListColumnType.String, "What the color is like");
    protected final ListColumn _listCol2 = new ListColumn("Month", "Month to Wear", ListHelper.ListColumnType.DateTime, "When to wear the color", "M");
    protected final ListColumn _listCol3 = new ListColumn("JewelTone", "Jewel Tone", ListHelper.ListColumnType.Boolean, "Am I a jewel tone?");
    protected final ListColumn _listCol4 = new ListColumn("Good", "Quality", ListHelper.ListColumnType.Integer, "How nice the color is");
    protected final ListColumn _listCol5 = new ListColumn("HiddenColumn", HIDDEN_TEXT, ListHelper.ListColumnType.String, "I should be hidden!");
    protected final ListColumn _listCol6 = new ListColumn("Aliased,Column", "Element", ListHelper.ListColumnType.String, "I show aliased data.");
    protected final static String[][] TEST_DATA = {
            { "Blue", "Green", "Red", "Yellow" },
            { "Light", "Mellow", "Robust", "Zany" },
            { "true", "false", "true", "false"},
            { "1", "4", "3", "2" },
            { "10", "9", "8", "7"},
            { "Water", "Earth", "Fire", "Air"}};
    protected final static String[] CONVERTED_MONTHS = { "2000-01-01", "2000-04-04", "2000-03-03", "2000-02-02" };
    private final static String LIST_ROW1 = TEST_DATA[0][0] + "\t" + TEST_DATA[1][0] + "\t" + TEST_DATA[2][0] + "\t" + CONVERTED_MONTHS[0];
    private final static String LIST_ROW2 = TEST_DATA[0][1] + "\t" + TEST_DATA[1][1] + "\t" + TEST_DATA[2][1] + "\t" + CONVERTED_MONTHS[1];
    private final static String LIST_ROW3 = TEST_DATA[0][2] + "\t" + TEST_DATA[1][2] + "\t" + TEST_DATA[2][2] + "\t" + CONVERTED_MONTHS[2];
    private final String LIST_DATA =
            LIST_KEY_NAME2 + "\t" + FAKE_COL1_NAME + "\t" + _listCol3.getName() + "\t" + _listCol2.getName() + "\n" +
            LIST_ROW1 + "\n" +
            LIST_ROW2 + "\n" +
            LIST_ROW3;
    private final String LIST_DATA2 =
            LIST_KEY_NAME2 + "\t" + FAKE_COL1_NAME + "\t" + _listCol3.getName() + "\t" + _listCol2.getName() + "\t" + _listCol4.getName() + "\t" + ALIASED_KEY_NAME + "\t" + _listCol5.getName() + "\n" +
            LIST_ROW1 + "\t" + TEST_DATA[4][0] + "\t" + TEST_DATA[5][0] + "\t" + HIDDEN_TEXT + "\n" +
            LIST_ROW2 + "\t" + TEST_DATA[4][1] + "\t" + TEST_DATA[5][1] + "\t" + HIDDEN_TEXT + "\n" +
            LIST_ROW3 + "\t" + TEST_DATA[4][2] + "\t" + TEST_DATA[5][2] + "\t" + HIDDEN_TEXT;
    private final static String TEST_FAIL = "testfail";
    private final static String TEST_FAIL2 = "testfail\n2\n";
    private final String TEST_FAIL3 = LIST_KEY_NAME2 + "\t" + FAKE_COL1_NAME + "\t" + _listCol2.getName() + "\n" +
            LIST_ROW1 + "\t" + "String";
    private final static String TEST_VIEW = "list_view";
    private final static String LIST2_NAME_CARS = TRICKY_CHARACTERS_NO_QUOTES + "Cars";
    protected final static ListHelper.ListColumnType LIST2_KEY_TYPE = ListHelper.ListColumnType.String;
    protected final static String LIST2_KEY_NAME = "Car";

    protected final ListColumn _list2Col1 = new ListColumn(LIST_KEY_NAME2, LIST_KEY_NAME2, LIST2_KEY_TYPE, "The color of the car", new LookupInfo(null, "lists", LIST_NAME_COLORS));
    private final static String LIST2_KEY = "Car1";
    private final static String LIST2_FOREIGN_KEY = "Blue";
    private final static String LIST2_KEY2 = "Car2";
    private final static String LIST2_FOREIGN_KEY2 = "Green";
    private final static String LIST2_FOREIGN_KEY_OUTSIDE = "Guy";
    private final static String LIST2_KEY3 = "Car3";
    private final static String LIST2_FOREIGN_KEY3 = "Red";
    private final static String LIST2_KEY4 = "Car4";
    private final static String LIST2_FOREIGN_KEY4 = "Brown";
    private final static String LIST3_NAME_OWNERS = "Owners";
    private final static ListHelper.ListColumnType LIST3_KEY_TYPE = ListHelper.ListColumnType.String;
    private final static String LIST3_KEY_NAME = "Owner";
    private final ListColumn _list3Col2 = new ListColumn("Wealth", "Wealth", ListHelper.ListColumnType.String, "");
    protected final ListColumn _list3Col1 = new ListColumn(LIST3_KEY_NAME, LIST3_KEY_NAME, LIST3_KEY_TYPE, "Who owns the car", new LookupInfo("/" + PROJECT_OTHER, "lists", LIST3_NAME_OWNERS));
    private final static String LIST3_COL2 = "Rich";
    private final String LIST2_DATA =
            LIST2_KEY_NAME + "\t" + _list2Col1.getName()  + "\t" + LIST3_KEY_NAME + "\n" +
            LIST2_KEY + "\t" + LIST2_FOREIGN_KEY + "\n" +
            LIST2_KEY2  + "\t" + LIST2_FOREIGN_KEY2 + "\t" + LIST2_FOREIGN_KEY_OUTSIDE + "\n" +
            LIST2_KEY3  + "\t" + LIST2_FOREIGN_KEY3 + "\n" +
            LIST2_KEY4  + "\t" + LIST2_FOREIGN_KEY4;
    private final String LIST3_DATA =
            LIST3_KEY_NAME + "\t" + _list3Col2.getName() + "\n" +
            LIST2_FOREIGN_KEY_OUTSIDE + "\t" + LIST3_COL2;
    public static final String LIST_AUDIT_EVENT = "List events";

    private final String EXCEL_DATA_FILE = getLabKeyRoot() + "/sampledata/dataLoading/excel/fruits.xls";
    private final String TSV_DATA_FILE = getLabKeyRoot() + "/sampledata/dataLoading/excel/fruits.tsv";
    private final String TSV_LIST_NAME = "Fruits from TSV";

    public String getAssociatedModuleDirectory()
    {
        return "server/modules/list";
    }

    @Override
    protected String getProjectName()
    {
        return PROJECT_VERIFY;
    }

    protected void doCleanup(boolean afterTest) throws TestTimeoutException
    {
        deleteProject(getProjectName(), afterTest);
        deleteProject(PROJECT_OTHER, afterTest);
    }

    @Override
    protected boolean isFileUploadTest()
    {
        return true;
    }

    @Override
    protected Set<String> getOrphanedViews()
    {
        Set<String> views = new HashSet<java.lang.String>();
        views.add(TEST_VIEW);
        return views;
    }

    @LogMethod
    protected void setUpListFinish()
    {
        // delete existing rows
        log("Test deleting rows");
        checkCheckbox(".toggle");
        clickButton("Delete", 0);
        assertAlert("Are you sure you want to delete the selected rows?");
        waitForPageToLoad();
        // load test data
        clickImportData();
        setFormElement(Locator.name("text"), LIST_DATA2);
        submitImportTsv();
    }

    @LogMethod
    protected void setUpList(String projectName)
    {

        log("Setup project and list module");
        _containerHelper.createProject(projectName, null);

        log("Add list -- " + LIST_NAME_COLORS);
        _listHelper.createList(projectName, LIST_NAME_COLORS, LIST_KEY_TYPE, LIST_KEY_NAME, _listCol1Fake, _listCol2, _listCol3);

        log("Add description and test edit");
        clickEditDesign();
        setFormElement(Locator.id("ff_description"), LIST_DESCRIPTION);
        setColumnName(0, LIST_KEY_NAME2);
        clickSave();

        log("Check that edit list definition worked");
        assertTextPresent(LIST_KEY_NAME2);
        assertTextPresent(LIST_DESCRIPTION);

        log("Test upload data");

        clickImportData();
        submitImportTsv("Form contains no data");

        setFormElement(Locator.id("tsv3"), TEST_FAIL);
        submitImportTsv("No rows were inserted.");

        setFormElement(Locator.id("tsv3"), TEST_FAIL2);
        submitImportTsv("Data does not contain required field: Color");

        setFormElement(Locator.id("tsv3"), TEST_FAIL3);
        submitImportTsv("Could not convert");
        setFormElement(Locator.id("tsv3"), LIST_DATA);
        submitImportTsv();

        log("Check upload worked correctly");
        assertTextPresent(_listCol2.getLabel());
        assertTextPresent(TEST_DATA[0][0]);
        assertTextPresent(TEST_DATA[1][1]);
        assertTextPresent(TEST_DATA[3][2]);

        DataRegionTable table = new DataRegionTable("query", this);
        Assert.assertEquals(TEST_DATA[2][0],  table.getDataAsText(table.getRow(TEST_DATA[0][0]), _listCol3.getLabel()));
        Assert.assertEquals(TEST_DATA[2][1], table.getDataAsText(table.getRow(TEST_DATA[0][1]), _listCol3.getLabel()));
        Assert.assertEquals(TEST_DATA[2][2],  table.getDataAsText(table.getRow(TEST_DATA[0][2]), _listCol3.getLabel()));

        log("Test check/uncheck of checkboxes");
        // Second row (Green)
        Assert.assertEquals(1,table.getRow(TEST_DATA[0][1]));
        clickAndWait(Locator.linkWithText("edit", 1));
        setFormElement(Locator.name("quf_" + _listCol2.getName()), CONVERTED_MONTHS[1]);  // Has a funny format -- need to post converted date
        checkCheckbox("quf_JewelTone");
        submit();
        // Third row (Red)
        Assert.assertEquals(2,table.getRow(TEST_DATA[0][2]));
        clickAndWait(Locator.linkWithText("edit", 2));
        setFormElement(Locator.name("quf_" + _listCol2.getName()), CONVERTED_MONTHS[2]);  // Has a funny format -- need to post converted date
        uncheckCheckbox("quf_JewelTone");
        submit();

        table = new DataRegionTable("query", this);
        Assert.assertEquals(TEST_DATA[2][0],  table.getDataAsText(table.getRow(TEST_DATA[0][0]), _listCol3.getLabel()));
        Assert.assertEquals("true", table.getDataAsText(table.getRow(TEST_DATA[0][1]), _listCol3.getLabel()));
        Assert.assertEquals("false",  table.getDataAsText(table.getRow(TEST_DATA[0][2]), _listCol3.getLabel()));

        log("Test edit and adding new field with imported data present");
        clickTab("List");
        clickAndWait(Locator.linkWithText("view design"));
        clickEditDesign();
        setColumnName(1,_listCol1.getName());
        setColumnLabel(1, _listCol1.getLabel());
        clickButton("Add Field", 0);
        setColumnName(4,_listCol4.getName());
        setColumnLabel(4, _listCol4.getLabel());
        setColumnType(4, _listCol4.getType());
        setFormElement(Locator.id("propertyDescription"), _listCol4.getDescription());


        // Create "Hidden Field" and remove from all views.
        clickButton("Add Field", 0);
        setColumnName(5, _listCol5.getName());
        setColumnLabel(5,_listCol5.getLabel());
        setColumnType(5,_listCol5.getType());
        uncheckCheckbox(Locator.xpath("//span[@id='propertyShownInGrid']/input"));
        uncheckCheckbox(Locator.xpath("//span[@id='propertyShownInInsert']/input"));
        uncheckCheckbox(Locator.xpath("//span[@id='propertyShownInUpdate']/input"));
        uncheckCheckbox(Locator.xpath("//span[@id='propertyShownInDetail']/input"));

        clickButton("Add Field", 0);
        setColumnName(6, _listCol6.getName());
        setColumnLabel(6,_listCol6.getLabel());
        setColumnType(6,_listCol6.getType());
        selectPropertyTab("Advanced");
        waitForElement(Locator.id("importAliases"), WAIT_FOR_JAVASCRIPT);
        setFormElement(Locator.id("importAliases"), ALIASED_KEY_NAME);

        click(Locator.id("partdown_2"));

        clickSave();

        log("Check new field was added correctly");
        assertTextPresent(_listCol4.getName());

        log("Set title field of 'Colors' to 'Desc'");
        clickEditDesign();
        selectOptionByText(Locator.id("ff_titleColumn"), "Desc");
        clickDone();

        clickAndWait(Locator.linkWithText(LIST_NAME_COLORS));
        assertTextPresent(TEST_DATA[0][0]);
        assertTextPresent(TEST_DATA[1][1]);
        assertTextPresent(TEST_DATA[3][2]);

        assertTextNotPresent(HIDDEN_TEXT); // Hidden from Grid view.
        if(!getBrowserType().contains("iexplore"))
            assertTextBefore(_listCol3.getLabel(), _listCol2.getLabel()); // Columns swapped. Doesn't work in IE

        setUpListFinish();

        log("Check that data was added correctly");
        assertTextPresent(TEST_DATA[0][0]);
        assertTextPresent(TEST_DATA[1][1]);
        assertTextPresent(TEST_DATA[3][2]);
        assertTextPresent(TEST_DATA[4][0]);
        assertTextPresent(TEST_DATA[4][1]);
        assertTextPresent(TEST_DATA[4][2]);
        assertTextPresent(TEST_DATA[5][0]);
        assertTextPresent(TEST_DATA[5][1]);
        assertTextPresent(TEST_DATA[5][2]);

        log("Check that hidden column is hidden.");
        clickAndWait(Locator.linkWithText("details"));
        assertTextNotPresent(HIDDEN_TEXT); // Hidden from details view.
        if(!getBrowserType().contains("iexplore"))
            assertTextBefore(_listCol3.getLabel(), _listCol2.getLabel());
        clickButton("Edit");
        assertTextNotPresent(HIDDEN_TEXT); // Hidden from update view.
        if(!getBrowserType().contains("iexplore"))
            assertTextBefore(_listCol3.getLabel(), _listCol2.getLabel());
        clickButton("Cancel");
        clickButton(DETAILS_BUTTON_NAME);

        log("Test inserting new row");
        clickButton("Insert New");
        assertTextNotPresent(HIDDEN_TEXT); // Hidden from insert view.
        if(!getBrowserType().contains("iexplore"))
            assertTextBefore(_listCol3.getLabel(), _listCol2.getLabel());
        String html = getHtmlSource();
        Assert.assertTrue("Description \"" + _listCol1.getDescription() + "\" not present.", html.contains(_listCol1.getDescription()));
        Assert.assertTrue("Description \"" + _listCol3.getDescription() + "\" not present.", html.contains(_listCol3.getDescription()));
        setFormElement(Locator.name("quf_" + _listCol1.getName()), TEST_DATA[1][3]);
        setFormElement(Locator.name("quf_" + _listCol2.getName()), "wrong type");
        // Jewel Tone checkbox is left blank -- we'll make sure it's posted as false below
        setFormElement(Locator.name("quf_" + _listCol4.getName()), TEST_DATA[4][3]);
        submit();
        assertTextPresent("This field is required");
        setFormElement(Locator.name("quf_" + LIST_KEY_NAME2), TEST_DATA[0][3]);
        submit();
        assertTextPresent("Could not convert");
        setFormElement(Locator.name("quf_" + _listCol2.getName()), CONVERTED_MONTHS[3]);
        submit();

        log("Check new row was added");
        assertTextPresent(TEST_DATA[0][3]);
        assertTextPresent(TEST_DATA[1][3]);
        assertTextPresent(TEST_DATA[2][3]);
        assertTextPresent(TEST_DATA[3][3]);
        table = new DataRegionTable("query", this);
        Assert.assertEquals(TEST_DATA[2][2], table.getDataAsText(2, _listCol3.getLabel()));
        Assert.assertEquals(3,table.getRow(TEST_DATA[0][3]));
        Assert.assertEquals(TEST_DATA[2][3], table.getDataAsText(3, _listCol3.getLabel()));

        log("Check hidden field is hidden only where specified.");
        dataregionToEditDesign();

        click(Locator.id("partdown_2"));
        click(Locator.id("name5")); // Select Hidden field.
        checkCheckbox(Locator.xpath("//span[@id='propertyShownInGrid']/input"));
        waitForElement(Locator.xpath("//img[@id = 'partstatus_5'][contains(@src, 'partchanged')]"));
        clickDone();

        log("Check that hidden column is hidden.");
        assertTextPresent(HIDDEN_TEXT); // Not hidden from grid view.
        clickAndWait(Locator.linkWithText("details"));
        assertTextNotPresent(HIDDEN_TEXT); // Hidden from details view.
        assertTextBefore(_listCol2.getLabel(), _listCol3.getLabel());
        clickButton("Edit");
        assertTextNotPresent(HIDDEN_TEXT); // Hidden from update view.
        assertTextBefore(_listCol2.getLabel(), _listCol3.getLabel());
        clickButton("Cancel");
        clickButton(DETAILS_BUTTON_NAME);
        clickButton("Insert New");
        assertTextNotPresent(HIDDEN_TEXT); // Hidden from insert view.
        assertTextBefore(_listCol2.getLabel(), _listCol3.getLabel());
        clickButton("Cancel");

        dataregionToEditDesign();

        click(Locator.id("name5")); // Select Hidden field.
        uncheckCheckbox(Locator.xpath("//span[@id='propertyShownInGrid']/input"));
        checkCheckbox(Locator.xpath("//span[@id='propertyShownInInsert']/input"));
        waitForElement(Locator.xpath("//img[@id = 'partstatus_5'][contains(@src, 'partchanged')]"));
        clickDone();

        assertTextNotPresent(HIDDEN_TEXT); // Hidden from grid view.
        clickAndWait(Locator.linkWithText("details"));
        assertTextNotPresent(HIDDEN_TEXT); // Hidden from details view.
        clickButton("Edit");
        assertTextNotPresent(HIDDEN_TEXT); // Hidden from update view.
        clickButton("Cancel");
        clickButton(DETAILS_BUTTON_NAME);
        clickButton("Insert New");
        assertTextPresent(HIDDEN_TEXT); // Not hidden from insert view.
        clickButton("Cancel");

        dataregionToEditDesign();

        click(Locator.id("name5")); // Select Hidden field.
        uncheckCheckbox(Locator.xpath("//span[@id='propertyShownInInsert']/input"));
        checkCheckbox(Locator.xpath("//span[@id='propertyShownInUpdate']/input"));
        waitForElement(Locator.xpath("//img[@id = 'partstatus_5'][contains(@src, 'partchanged')]"));
        clickDone();

        assertTextNotPresent(HIDDEN_TEXT); // Hidden from grid view.
        clickAndWait(Locator.linkWithText("details"));
        assertTextNotPresent(HIDDEN_TEXT); // Hidden from details view.
        clickButton("Edit");
        assertTextPresent(HIDDEN_TEXT); // Not hidden from update view.
        clickButton("Cancel");
        clickButton(DETAILS_BUTTON_NAME);
        clickButton("Insert New");
        assertTextNotPresent(HIDDEN_TEXT); // Hidden from insert view.
        clickButton("Cancel");

        dataregionToEditDesign();

        click(Locator.id("name5")); // Select Hidden field.
        uncheckCheckbox(Locator.xpath("//span[@id='propertyShownInUpdate']/input"));
        checkCheckbox(Locator.xpath("//span[@id='propertyShownInDetail']/input"));
        waitForElement(Locator.xpath("//img[@id = 'partstatus_5'][contains(@src, 'partchanged')]"));
        clickDone();

        assertTextNotPresent(HIDDEN_TEXT); // Hidden from grid view.
        clickAndWait(Locator.linkWithText("details"));
        assertTextPresent(HIDDEN_TEXT); // Not hidden from details view.
        clickButton("Edit");
        assertTextNotPresent(HIDDEN_TEXT); // Hidden from update view.
        clickButton("Cancel");
        clickButton(DETAILS_BUTTON_NAME);
        clickButton("Insert New");
        assertTextNotPresent(HIDDEN_TEXT); // Hidden from insert view.
        clickButton("Cancel");
    }

    protected void doTestSteps()
    {
        setUpList(PROJECT_VERIFY);

        log("Test Sort and Filter in Data View");
        setSort("query", _listCol1.getName(), SortDirection.ASC);
        assertTextBefore(TEST_DATA[0][0], TEST_DATA[0][1]);

        clearSortTest();

        setFilter("query", _listCol4.getName(), "Is Greater Than", "7");
        assertTextNotPresent(TEST_DATA[0][3]);

        log("Test Customize View");
        executeScript("LABKEY.DataRegions['query'].clearAllFilters();");
        waitForElementToDisappear(Locator.css(".labkey-dataregion-msg"), WAIT_FOR_JAVASCRIPT);
        //clickButton("Clear All"); // Can't trigger :hover pseudo-class with webdriver
        _customizeViewsHelper.openCustomizeViewPanel();
        _customizeViewsHelper.removeCustomizeViewColumn(_listCol4.getName());
        _customizeViewsHelper.addCustomizeViewFilter(_listCol4.getName(), _listCol4.getLabel(), "Is Less Than", "10");
        _customizeViewsHelper.addCustomizeViewSort(_listCol2.getName(), _listCol2.getLabel(), "Ascending");
        _customizeViewsHelper.saveCustomView(TEST_VIEW);

        log("Check Customize View worked");
        assertTextPresent(TEST_DATA[0][3]);
        assertTextPresentInThisOrder(TEST_DATA[0][3], TEST_DATA[0][2], TEST_DATA[0][1]);
        assertTextNotPresent(TEST_DATA[0][0]);
        assertTextNotPresent(_listCol4.getLabel());

        log("4725: Check Customize View can't remove all fields");
        _customizeViewsHelper.openCustomizeViewPanel();
        _customizeViewsHelper.removeCustomizeViewColumn(LIST_KEY_NAME2);
        _customizeViewsHelper.removeCustomizeViewColumn(_listCol1.getName());
        _customizeViewsHelper.removeCustomizeViewColumn(_listCol2.getName());
        _customizeViewsHelper.removeCustomizeViewColumn(_listCol3.getName());
        _customizeViewsHelper.removeCustomizeViewColumn(EscapeUtil.fieldKeyEncodePart(_listCol6.getName()));
        _customizeViewsHelper.applyCustomView(0);
        assertAlert("You must select at least one field to display in the grid.");
        _customizeViewsHelper.closeCustomizeViewPanel();

        log("Test Export");
        addUrlParameter("exportAsWebPage=true");
        pushLocation();
        waitForElement(Locator.navButton("Export"), WAIT_FOR_JAVASCRIPT);
        clickExportToText();
        assertTextPresent(TEST_DATA[0][3]);
        assertTextPresentInThisOrder(TEST_DATA[0][3], TEST_DATA[0][2], TEST_DATA[0][1]);
        assertTextNotPresent(TEST_DATA[0][0]);
        assertTextNotPresent(_listCol4.getLabel());
        popLocation();

        filterTest();

        clickFolder(getProjectName());

        log("Test that sort only affects one web part");
        setSort("qwp2", _listCol4.getName(), SortDirection.ASC);
        String source = getHtmlSource();
        int index;
        Assert.assertTrue(source.indexOf(TEST_DATA[1][2]) < (index = source.indexOf(TEST_DATA[1][1])) &&
                source.indexOf(TEST_DATA[1][1], index) < source.indexOf(TEST_DATA[1][2], index));

        log("Test list history");
        clickAndWait(Locator.linkWithText("manage lists"));
        clickAndWait(Locator.linkWithText("view history"));
        assertTextPresent(":History");
        assertTextPresent("modified", 10);
        assertTextPresent("Bulk inserted", 2);
        assertTextPresent("A new list record was inserted", 1);
        assertTextPresent("created", 1);
        Assert.assertEquals("details Links", 6, countLinksWithText("DETAILS"));
        Assert.assertEquals("Project Links", 17 + 3, countLinksWithText(PROJECT_VERIFY)); // Table links + header & sidebar links
        Assert.assertEquals("List Links", 17 + 1, countLinksWithText(LIST_NAME_COLORS)); // Table links + header link
        clickAndWait(Locator.linkWithText("DETAILS"));
        assertTextPresent("List Item Details");
        assertTextNotPresent("No details available for this event.");
        assertTextNotPresent("Unable to find the audit history detail for this event");

        clickButton("Done");
        clickAndWait(Locator.linkWithText(PROJECT_VERIFY, 3));

        log("Test single list web part");
        addWebPart("List - Single");
        setFormElement(Locator.name("title"), "This is my single list web part title");
        //TODO:  This didn't work with the convetional _ext4Helper.selectComboBoxItem(), so we are using two clicks instead.
        click(Locator.xpath("//input[@name='listName']"));
        click(Locator.xpath("//li[contains(@class, 'x4-boundlist-item') and contains( text(), '>')]"));
        //This will only work so long as it's the only button on the page.
        click(Locator.xpath("//button"));
        waitForText("Import Data");
        assertTextPresent("View Design");
        clickAndWait(Locator.linkWithSpan("This is my single list web part title"), WAIT_FOR_PAGE);
        assertTextPresent("Colors");
        assertTextPresent("Views");

        log("Create second project");
        _containerHelper.createProject(PROJECT_OTHER, null);

        String project2_url = getCurrentRelativeURL();

        log("Add List -- " + LIST3_NAME_OWNERS);
        _listHelper.createList(PROJECT_OTHER, LIST3_NAME_OWNERS, LIST3_KEY_TYPE, LIST3_KEY_NAME, _list3Col2);
        assertTextPresent("<AUTO> (Owner)");

        log("Upload data to second list");
        _listHelper.uploadData(PROJECT_OTHER, LIST3_NAME_OWNERS, LIST3_DATA);

        log("Navigate back to first project");
        log("Add list -- " + LIST2_NAME_CARS);
        _listHelper.createList(PROJECT_VERIFY, LIST2_NAME_CARS, LIST2_KEY_TYPE, LIST2_KEY_NAME, _list2Col1, _list3Col1);

        log("Upload data to second list");
        _listHelper.uploadData(PROJECT_VERIFY, LIST2_NAME_CARS, LIST2_DATA);

        log("Check that upload worked");
        assertTextPresent(LIST2_KEY);
        assertTextPresent(LIST2_KEY2);
        assertTextPresent(LIST2_KEY3);
        assertTextPresent(LIST2_KEY4);

        log("Check that reference worked");
        _customizeViewsHelper.openCustomizeViewPanel();
        _customizeViewsHelper.addCustomizeViewColumn(_list2Col1.getName() + "/" +  _listCol1.getName(), _list2Col1.getLabel() + " " +  _listCol1.getLabel());
        _customizeViewsHelper.addCustomizeViewColumn(_list2Col1.getName() + "/" +  _listCol2.getName(), _list2Col1.getLabel() + " " +  _listCol2.getLabel());
        _customizeViewsHelper.addCustomizeViewColumn(_list2Col1.getName() + "/" +  _listCol4.getName(), _list2Col1.getLabel() + " " + _listCol4.getLabel());
        _customizeViewsHelper.addCustomizeViewFilter(_list2Col1.getName() + "/" +  _listCol4.getName(),  _listCol4.getLabel(), "Is Less Than", "10");
        _customizeViewsHelper.addCustomizeViewSort(_list2Col1.getName() + "/" +  _listCol4.getName(),  _listCol4.getLabel(), "Ascending");
        _customizeViewsHelper.addCustomizeViewColumn(_list3Col1.getName() + "/" +  _list3Col1.getName(), _list3Col1.getLabel() + " " +  _list3Col1.getLabel());
        _customizeViewsHelper.addCustomizeViewColumn(_list3Col1.getName() + "/" +  _list3Col2.getName(), _list3Col1.getLabel() + " " +  _list3Col2.getLabel());
        _customizeViewsHelper.saveCustomView(TEST_VIEW);

        log("Check adding referenced fields worked");
        waitForText(_listCol1.getLabel(), WAIT_FOR_JAVASCRIPT);
        assertTextPresent(_listCol1.getLabel());
        assertTextPresent(_listCol2.getLabel());
        assertTextPresent(_listCol4.getLabel());
        assertTextPresent(LIST2_FOREIGN_KEY_OUTSIDE);
        assertTextPresent(LIST3_COL2);
        assertTextNotPresent(LIST2_KEY);
        assertTextBefore(LIST2_KEY3, LIST2_KEY2);
        assertTextNotPresent(LIST2_KEY4);

        log("Test export");
        addUrlParameter("exportAsWebPage=true");
        pushLocation();
        waitForElement(Locator.navButton("Export"), WAIT_FOR_JAVASCRIPT);
        clickExportToText();
        assertTextPresent(LIST_KEY_NAME2.toLowerCase() + _listCol1.getName());
        assertTextPresent(LIST_KEY_NAME2.toLowerCase() + _listCol2.getName());
        assertTextPresent(LIST_KEY_NAME2.toLowerCase() + _listCol4.getName());
        assertTextPresent(LIST2_FOREIGN_KEY_OUTSIDE);
        assertTextPresent(LIST3_COL2);
        assertTextNotPresent(LIST2_KEY);
        assertTextBefore(LIST2_KEY3, LIST2_KEY2);
        assertTextNotPresent(LIST2_KEY4);
        popLocation();

        log("Test edit row");
        clickAndWait(Locator.linkWithText("edit", 0));
        selectOptionByText(Locator.name("quf_Color"), TEST_DATA[1][1]);
        selectOptionByText(Locator.name("quf_Owner"), LIST2_FOREIGN_KEY_OUTSIDE);
        submit();

        clickMenuButton("Views", "default");
        assertTextPresent(TEST_DATA[1][1], 2);

        log("Test deleting rows");
        checkCheckbox(".toggle");
        clickButton("Delete", 0);
        assertAlert("Are you sure you want to delete the selected rows?");
        waitForTextToDisappear(LIST2_KEY);
        assertTextNotPresent(LIST2_KEY2);
        assertTextNotPresent(LIST2_KEY3);
        assertTextNotPresent(LIST2_KEY4);

        log("Get URL to test exporting deleted list.");
        clickTab("List");
        clickAndWait(Locator.linkWithText(LIST_NAME_COLORS));
        clickButton("Export", 0);
        _extHelper.clickSideTab("Text");
        String exportButtonScript = getAttribute(Locator.xpath(Locator.navButton("Export to Text").getPath() + "/..") , "onclick");
        String exportUrl = exportButtonScript.substring(exportButtonScript.indexOf("window.location=") + 17, exportButtonScript.indexOf("document.getElementById") - 11);
        clickAndWait(Locator.linkWithText("View Design"));

        log("Test deleting data");
        clickDeleteList();
        clickButton("OK");

        log("Test that deletion happened");
        assertTextNotPresent(LIST_NAME_COLORS);
        clickAndWait(Locator.linkWithText(LIST2_NAME_CARS));
        pushLocation();
        _customizeViewsHelper.openCustomizeViewPanel();
        assertElementNotPresent(Locator.xpath("//div[contains(@class, 'x-tree-node') and @*='" + LIST_KEY_NAME + "']"));
        assertElementPresent(Locator.xpath("//div[contains(@class, 'x-tree-node') and @*='" + LIST3_KEY_NAME + "']"));
        popLocation();
        clickAndWait(Locator.linkWithText(PROJECT_VERIFY));
        assertTextPresent("query not found");

        log("Test exporting a nonexistent list returns a 404");
        beginAt(exportUrl.substring(WebTestHelper.getContextPath().length()));
        Assert.assertEquals("Incorrect response code", 404, getResponseCode());
        assertTextPresent("Query '" + LIST_NAME_COLORS + "' in schema 'lists' doesn't exist.");

        clickButton("Folder");
        AuditLogTest.verifyAuditEvent(this, LIST_AUDIT_EVENT, AuditLogTest.COMMENT_COLUMN, "The domain " + LIST_NAME_COLORS + " was deleted", 5);
        AuditLogTest.verifyAuditEvent(this, LIST_AUDIT_EVENT, AuditLogTest.COMMENT_COLUMN, "An existing list record was deleted", 5);
        AuditLogTest.verifyAuditEvent(this, LIST_AUDIT_EVENT, AuditLogTest.COMMENT_COLUMN, "An existing list record was modified", 10);

        doRenameFieldsTest();
        doUploadTest();
        customFormattingTest();
        customizeURLTest();
        crossContainerLookupTest();
     }

    String crossContainerLookupList = "CCLL";
    @LogMethod
    private void crossContainerLookupTest()
    {
        //create list with look up A
        String lookupColumn = "lookup";
        _listHelper.createList(PROJECT_OTHER, crossContainerLookupList, ListHelper.ListColumnType.AutoInteger, "Key",  col(PROJECT_VERIFY, lookupColumn, Integer, "A" ));
        clickImportData();
        setListImportAsTestDataField(lookupColumn + "\n1");

        log("verify look column set properly");
        assertTextPresent("one A");
        _customizeViewsHelper.openCustomizeViewPanel();
        _customizeViewsHelper.addCustomizeViewColumn("lookup/Bfk/Cfk/title");
        _customizeViewsHelper.saveCustomView();

        clickLink(Locator.linkContainingText("one C"));
        assertElementPresent(Locator.xpath("//input[@type='submit']"));
        goBack();


        //add columns to look all the way to C
    }

    @LogMethod
    private void filterTest()
    {
        log("Filter Test");
        clickFolder(PROJECT_VERIFY);
        addWebPart("Query");
        selectOptionByText(Locator.name("schemaName"), "lists");
        click(Locator.id("selectQueryContents"));
        submit();
        addWebPart("Query");
        selectOptionByText(Locator.name("schemaName"), "lists");
        click(Locator.id("selectQueryContents"));
        submit();

        log("Test that the right filters are present for each type");
        runMenuItemHandler("qwp3:" + _listCol4.getName() + ":filter");
        _extHelper.waitForExtDialog("Show Rows Where " + _listCol4.getLabel());
        _extHelper.clickExtTab("Choose Filters");
        click(Locator.xpath("//div["+Locator.NOT_HIDDEN+" and ./label/span[text()='Filter Type:']]/div/div//img[contains(@class, 'x-form-arrow-trigger')]"));

        assertElementNotPresent(Locator.xpath("//div["+Locator.NOT_HIDDEN+" and contains(@class, 'x-combo-list-item') and text()='Starts With']"));
        assertElementPresent(Locator.xpath("//div["+Locator.NOT_HIDDEN+" and contains(@class, 'x-combo-list-item') and text()='Is Blank']"));
        _extHelper.clickExtButton("Show Rows Where " + _listCol4.getLabel(), "CANCEL", 0);

        log("Test that filters don't affect multiple web parts");
        assertTextPresent(TEST_DATA[1][0], 2);
        setFilter("qwp3", _listCol4.getName(), "Is Less Than", "10");
        assertTextPresent(TEST_DATA[1][0], 1);

        clickAndWait(Locator.linkContainingText(LIST_NAME_COLORS));
    }

    /*  Issue 11825: Create test for "Clear Sort"
        Issue 15567: Can't sort DataRegion by column name that has comma

        sort by a parameter, than clear sort.
        Verify that reverts to original sort and the dropdown menu disappears

        preconditions:  table already sorted by description
     */
    @LogMethod
    private void clearSortTest()
    {
        //make sure elements are ordered the way they should be
        assertTextPresentInThisOrder(TEST_DATA[5][0], TEST_DATA[5][1],TEST_DATA[5][2]);

        String encodedName = EscapeUtil.fieldKeyEncodePart(_listCol6.getName());

        //sort  by element and verify it worked
        setSort("query", encodedName, SortDirection.DESC);
        assertTextPresentInThisOrder(TEST_DATA[5][0], TEST_DATA[5][2], TEST_DATA[5][1]);

        //remove sort and verify we return to initial state
        clearSort("query", encodedName);
        assertTextPresentInThisOrder(TEST_DATA[5][0], TEST_DATA[5][1],TEST_DATA[5][2]);
    }

    @LogMethod
    private void doUploadTest()
    {
        if (!isFileUploadAvailable())
            return;

        log("Infer from excel file, then import data");
        File excelFile = new File(EXCEL_DATA_FILE);
        _listHelper.createListFromFile(PROJECT_VERIFY, "Fruits from Excel", excelFile);
        waitForElement(Locator.linkWithText("pomegranate"));
        assertNoLabkeyErrors();

        File tsvFile = new File(TSV_DATA_FILE);
        //Cancel test disabled because teamcity is too slow to run it successfully
        /*log("Infer from tsv file, but cancel before completion");
        clickAndWait(Locator.linkWithText(PROJECT_NAME));
        clickAndWait(Locator.linkWithText("manage lists"));
        clickButton("Create New List");
        waitForElement(Locator.id("ff_name"), BaseSeleniumWebTest.WAIT_FOR_JAVASCRIPT);
        setFormElement(Locator.id("ff_name"),  TSV_LIST_NAME);
        checkCheckbox(Locator.xpath("//span[@id='fileImport']/input[@type='checkbox']"));
        clickButton("Create List", 0);
        waitForElement(Locator.xpath("//input[@name='uploadFormElement']"), BaseSeleniumWebTest.WAIT_FOR_JAVASCRIPT);
        setFormElement("uploadFormElement", tsvFile);
        waitForElement(Locator.xpath("//span[@id='button_Import']"), BaseSeleniumWebTest.WAIT_FOR_JAVASCRIPT);
        clickButton("Import", 0);
        waitForElement(Locator.xpath("//div[text()='Creating columns...']"), WAIT_FOR_JAVASCRIPT);
        clickButton("Cancel");
        assertTextNotPresent(TSV_LIST_NAME);*/

        log("Infer from a tsv file, then import data");
        _listHelper.createListFromFile(PROJECT_VERIFY, TSV_LIST_NAME, tsvFile);
        waitForElement(Locator.linkWithText("pomegranate"));
        assertNoLabkeyErrors();
        log("Verify correct types are inferred from file");
        clickButton("View Design");
        waitForElement(Locator.xpath("//tr[./td/div[text()='BoolCol'] and ./td/div[text()='Boolean']]"), WAIT_FOR_JAVASCRIPT);
        assertElementPresent(Locator.xpath("//tr[./td/div[text()='IntCol'] and ./td/div[text()='Integer']]"));
        assertElementPresent(Locator.xpath("//tr[./td/div[text()='NumCol'] and ./td/div[text()='Number (Double)']]"));
        assertElementPresent(Locator.xpath("//tr[./td/div[text()='DateCol'] and ./td/div[text()='DateTime']]"));
    }

    @LogMethod
    private void customFormattingTest()
    {
        // Assumes we are at the list designer after doUploadTest()
        clickButton("Edit Design", 0);

        // Set conditional format on boolean column. Bold, italic, strikethrough, cyan text, red background
        click(Locator.name("ff_name3")); // BoolCol
        click(Locator.xpath("//span[text()='Format']"));
        clickButton("Add Conditional Format", 0);
        _extHelper.waitForExtDialog("Apply Conditional Format Where BoolCol", WAIT_FOR_JAVASCRIPT);
        setFormElement(Locator.id("value_1"), "true");
        _extHelper.clickExtButton("Apply Conditional Format Where BoolCol", "OK", 0);
        checkCheckbox("Bold");
        checkCheckbox("Italic");
        checkCheckbox("Strikethrough");
        click(Locator.xpath("//div[@title='Color']"));
        waitForElement(Locator.xpath("//div[contains(@class, 'gwt-DialogBox')]//div[contains(@class, 'Caption') and text()='Conditional Format Colors']"), WAIT_FOR_JAVASCRIPT);
        setFormElement(Locator.xpath("//fieldset[./legend/span[text()='Background']]//input"), "FF0000"); // Red background
        click(Locator.id("button_OK"));
        waitForElementToDisappear(Locator.xpath("//div[contains(@class, 'gwt-DialogBox')]//div[contains(@class, 'Caption') and text()='Conditional Format Colors']"), WAIT_FOR_JAVASCRIPT);
        // Regression test for Issue 11435: reopen color dialog to set text color
        click(Locator.xpath("//div[@title='Color']"));
        waitForElement(Locator.xpath("//div[contains(@class, 'gwt-DialogBox')]//div[contains(@class, 'Caption') and text()='Conditional Format Colors']"), WAIT_FOR_JAVASCRIPT);
        setFormElement(Locator.xpath("//fieldset[./legend/span[text()='Foreground']]//input"), "00FFFF"); // Cyan text
        click(Locator.id("button_OK"));
        waitForElementToDisappear(Locator.xpath("//div[contains(@class, 'gwt-DialogBox')]//div[contains(@class, 'Caption') and text()='Conditional Format Colors']"), WAIT_FOR_JAVASCRIPT);

        // Set multiple conditional formats on int column.
        click(Locator.name("ff_name4")); // IntCol
        click(Locator.xpath("//span[text()='Format']"));
        // If greater than 7, strikethrough //TODO: Set after (>5) format. Blocked: 12865
        clickButton("Add Conditional Format", 0);
        _extHelper.waitForExtDialog("Apply Conditional Format Where IntCol", WAIT_FOR_JAVASCRIPT);
        _extHelper.selectComboBoxItem("Filter Type:", "Is Greater Than");
        setFormElement(Locator.id("value_1"), "7");
        _extHelper.clickExtButton("Apply Conditional Format Where IntCol", "OK", 0);
        checkCheckbox("Strikethrough");
        // If greater than 5, Bold  //TODO: Set before (>7) format. Blocked: 12865
        clickButton("Add Conditional Format", 0);
        _extHelper.waitForExtDialog("Apply Conditional Format Where IntCol", WAIT_FOR_JAVASCRIPT);
        _extHelper.selectComboBoxItem("Filter Type:", "Is Greater Than");
        setFormElement(Locator.id("value_1"), "5");
        _extHelper.clickExtButton("Apply Conditional Format Where IntCol", "OK", 0);
        checkCheckbox("Bold", 1);

        // TODO: Blocked: 12865: ListTest failing to reorder conditional formats
        // Switch the order of filters so that >7 takes precedence over >5
//        dragAndDrop(Locator.xpath("//div[text()='Is Greater Than 5']"), Locator.xpath("//div[text()='Is Greater Than 7']"));
        assertTextBefore("Is Greater Than 7", "Is Greater Than 5");

        clickButton("Save", 0);
        waitAndClickButton("Done");

        // Verify conditional format of boolean column
        // look for cells that do not match the
        assertTextPresent(TSV_LIST_NAME);
        assertElementNotPresent(Locator.xpath("//td[text() = 'true' and not(contains(@style, 'line-through'))]"));
        assertElementNotPresent(Locator.xpath("//td[text() = 'true' and not(contains(@style, 'bold'))]"));
        assertElementNotPresent(Locator.xpath("//td[text() = 'true' and not(contains(@style, 'italic'))]"));
        assertElementNotPresent(Locator.xpath("//td[text() = 'true' and not(contains(@style, 'color: rgb(0, 255, 255)') or contains(@style, 'color: #00FFFF'))]")); // Cyan text
        assertElementNotPresent(Locator.xpath("//td[text() = 'true' and not(contains(@style, 'background-color: rgb(255, 0, 0)') or contains(@style, 'color: #FF0000'))]")); // Red background
        assertElementNotPresent(Locator.xpath("//td[text() = 'false' and @style]")); // No style on false items
        assertElementPresent(Locator.xpath("//td[text()='5' and not(contains(@style, 'bold')) and not(contains(@style, 'line-through'))]"));
        assertElementPresent(Locator.xpath("//td[text()='6' and contains(@style, 'bold') and not(contains(@style, 'line-through'))]"));
        assertElementPresent(Locator.xpath("//td[text()='8' and contains(@style, 'line-through') and not(contains(@style, 'bold'))]"));

        // Check for appropriate tooltips
        assertElementNotPresent(Locator.id("helpDivBody")
                        .withText("Formatting applied because column > 5."));
        Actions builder = new Actions(_driver);
        builder.moveToElement(Locator.xpath("//td[text()='6' and contains(@style, 'bold')]").findElement(_driver)).build().perform();
        // Tooltip doesn't show instantly, so wait for a bit
        _shortWait.until(ExpectedConditions.visibilityOf(Locator.id("helpDivBody")
                .withText("Formatting applied because column > 5.").waitForElmement(_driver, WAIT_FOR_JAVASCRIPT)));
        click(Locator.css("img[alt=close]"));
        // Tooltip doesn't hide instantly, so wait for a bit
        _shortWait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("helpDiv")));

        assertElementNotPresent(Locator.id("helpDivBody")
                        .withText("Formatting applied because column = true."));
        builder.moveToElement(Locator.xpath("//td[text()='true']").findElement(_driver)).build().perform();
        // Tooltip doesn't show instantly, so wait for a bit
        _shortWait.until(ExpectedConditions.visibilityOf(Locator.id("helpDivBody")
                .withText("Formatting applied because column = true.").waitForElmement(_driver, WAIT_FOR_JAVASCRIPT)));
        click(Locator.css("img[alt=close]"));
        // Tooltip doesn't hide instantly, so wait for a bit
        _shortWait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("helpDiv")));
    }

    @LogMethod
    private void doRenameFieldsTest()
    {
        log("8329: Test that renaming a field then creating a new field with the old name doesn't result in awful things");
        _listHelper.createList(PROJECT_VERIFY, "new", ListHelper.ListColumnType.AutoInteger, "key", new ListColumn("BarBar", "BarBar", ListHelper.ListColumnType.String, "Some new column"));
        assertTextPresent("BarBar");
        clickEditDesign();
        setColumnName(1,"FooFoo");
        setColumnLabel(1,"");
        clickSave();
        assertTextPresent("FooFoo");
        assertTextNotPresent("BarBar");
        clickEditDesign();
        clickButton("Add Field", 0);
        setColumnName(2,"BarBar");
        clickSave();
        assertTextPresent("FooFoo");
        assertTextPresent("BarBar");
        assertTextBefore("FooFoo", "BarBar");
    }



    //
    // CUSTOMIZE URL tests
    //

    ListHelper.ListColumn col(String name, ListHelper.ListColumnType type)
    {
        return new ListHelper.ListColumn(name, "", type, "");
    }

    ListHelper.ListColumn col(String name, ListHelper.ListColumnType type, String table)
    {
        return col(null, name, type, table);
    }

    ListHelper.ListColumn col(String folder, String name, ListHelper.ListColumnType type, String table)
    {
        return new ListHelper.ListColumn(name, "", type, "", new ListHelper.LookupInfo(folder, "lists", table));
    }

    ListHelper.ListColumn colURL(String name, ListHelper.ListColumnType type, String url)
    {
        ListColumn c  = new ListHelper.ListColumn(name, "", type, "");
        c.setURL(url);
        return c;
    }

    List<ListColumn> Acolumns = Arrays.asList(
            col("A", Integer),
            colURL("title", String, "/junit/echoForm.view?key=${A}&title=${title}&table=A"),
            col("Bfk", Integer, "B")
    );
    String[][] Adata = new String[][]
    {
        {"1", "one A", "1"},
    };

    List<ListHelper.ListColumn> Bcolumns = Arrays.asList(
            col("B", Integer),
            colURL("title", String, "org.labkey.core.junit.JunitController$EchoFormAction.class?key=${B}&title=${title}&table=B"),
            col("Cfk", Integer, "C")
    );
    String[][] Bdata = new String[][]
    {
        {"1", "one B", "1"},
    };

    List<ListHelper.ListColumn> Ccolumns = Arrays.asList(
            col("C", Integer),
            colURL("title", String, "/junit/echoForm.view?key=${C}&title=${title}&table=C")
    );
    String[][] Cdata = new String[][]
    {
        {"1", "one C"},
    };


    String toTSV(List<ListHelper.ListColumn> cols, String[][] data)
    {
        StringBuilder sb = new StringBuilder();
        String tab = "";
        for (ListHelper.ListColumn c : cols)
        {
            sb.append(tab);
            sb.append(c.getName());
            tab = "\t";
        }
        tab = "\n";
        for (String[] row : data)
        {
            for (String cell : row)
            {
                sb.append(tab);
                sb.append(cell);
                tab = "\t";
            }
            tab = "\n";
        }
        sb.append(tab);
        return sb.toString();
    }


    void submitImportTsv(String error)
    {
        _listHelper.submitImportTsv_error(error);
    }

    void submitImportTsv()
    {
        _listHelper.submitImportTsv_success();
    }


    void createList(String name, List<ListHelper.ListColumn> cols, String[][] data)
    {
        log("Add List -- " + name);
        _listHelper.createList(PROJECT_VERIFY, name, cols.get(0).getType(), cols.get(0).getName(),
                cols.subList(1, cols.size()).toArray(new ListHelper.ListColumn[cols.size() - 1]));
        clickEditDesign();
        selectOptionByText(Locator.id("ff_titleColumn"), cols.get(1).getName());    // Explicitly set to the PK (auto title will pick wealth column)
        clickSave();
        clickImportData();
        setListImportAsTestDataField(toTSV(cols,data));
    }

    private void setListImportAsTestDataField(String data)
    {
        setFormElement(Locator.name("text"), data);
        submitImportTsv();
    }


    Locator inputWithValue(String name, String value)
    {
        return Locator.xpath("//input[@name='" + name + "' and @value='" + value + "']");
    }

    @LogMethod
    protected void customizeURLTest()
    {
        this.pushLocation();
        {
            createList("C", Ccolumns, Cdata);
            createList("B", Bcolumns, Bdata);
            createList("A", Acolumns, Adata);

            beginAt("/query/" + EscapeUtil.encode(PROJECT_VERIFY) + "/executeQuery.view?schemaName=lists&query.queryName=A");

            pushLocation();
            {
                clickAndWait(Locator.linkWithText("one A"));
                assertElementPresent(inputWithValue("table","A"));
                assertElementPresent(inputWithValue("title","one A"));
                assertElementPresent(inputWithValue("key","1"));
            }
            popLocation();

            pushLocation();
            {
                clickAndWait(Locator.linkWithText("one B"));
                assertLinkPresentWithText("one B");
                assertLinkPresentWithText("one C");
            }
            popLocation();

            // show all columns
            _customizeViewsHelper.openCustomizeViewPanel();
            _customizeViewsHelper.addCustomizeViewColumn("Bfk/B", "Bfk B");
            _customizeViewsHelper.addCustomizeViewColumn("Bfk/title", "Bfk Title");
            _customizeViewsHelper.addCustomizeViewColumn("Bfk/Cfk", "Bfk Cfk");
            _customizeViewsHelper.addCustomizeViewColumn("Bfk/Cfk/C", "Bfk Cfk C");
            _customizeViewsHelper.addCustomizeViewColumn("Bfk/Cfk/title", "Bfk Cfk Title");
            _customizeViewsHelper.saveCustomView("allColumns");

            clickAndWait(Locator.linkWithText("one C", 1));
            assertElementPresent(inputWithValue("key","1"));
            assertElementPresent(inputWithValue("table","C"));
            assertElementPresent(inputWithValue("title","one C"));
            Assert.assertTrue(getCurrentRelativeURL().contains("/junit/" + EscapeUtil.encode(PROJECT_VERIFY) + "/echoForm.view"));
        }
        popLocation();
    }



    void dataregionToEditDesign()
    {
        clickButton("View Design");
        clickEditDesign();
    }

    void clickDone()
    {
        if (isElementPresent(Locator.navButton("Save")))
            clickSave();
        clickButton("Done");
    }

    void clickImportData()
    {
        _listHelper.clickImportData();
    }

    void clickEditDesign()
    {
        _listHelper.clickEditDesign();
    }

    void clickSave()
    {
        _listHelper.clickSave();
    }

    void clickDeleteList()
    {
        _listHelper.clickDeleteList();
    }

    void selectPropertyTab(String name)
    {
        click(Locator.xpath("//span[contains(@class,'x-tab-strip-text') and text()='" + name + "']"));
    }

    void setColumnName(int index, String name)
    {
        Locator nameLoc = Locator.name("ff_name"+index);
        click(nameLoc);
        setFormElement(nameLoc, name);
        pressTab(nameLoc);
    }
    void setColumnLabel(int index, String label)
    {
        Locator labelLoc = Locator.name("ff_label"+index);
        click(labelLoc);
        setFormElement(labelLoc, label);
        pressTab(labelLoc);
    }
    void setColumnType(int index, ListHelper.ListColumnType type)
    {
        Locator typeLoc = Locator.name("ff_type"+index);
        click(typeLoc);
        setFormElement(typeLoc, type.toString());
        pressTab(typeLoc);
    }
}
