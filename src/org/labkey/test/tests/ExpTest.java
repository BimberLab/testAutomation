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

import org.junit.experimental.categories.Category;
import org.labkey.test.BaseSeleniumWebTest;
import org.labkey.test.Locator;
import org.labkey.test.TestTimeoutException;
import org.labkey.test.categories.BVT;
import org.labkey.test.util.FileBrowserHelper;
import org.labkey.test.util.ListHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * User: brittp
 * Date: Nov 22, 2005
 * Time: 1:31:42 PM
 */
@Category(BVT.class)
public class ExpTest extends BaseSeleniumWebTest
{
    private static final String PROJECT_NAME = "ExpVerifyProject";
    private static final String FOLDER_NAME = "verifyfldr";
    private static final String EXPERIMENT_NAME = "Tutorial Examples";
    private static final String RUN_NAME = "Example 5 Run (XTandem peptide search)";
    private static final String RUN_NAME_IMAGEMAP = "Example 5 Run (XTandem peptide search)";
    private static final String DATA_OBJECT_TITLE = "Data: CAexample_mini.mzXML";

    public String getAssociatedModuleDirectory()
    {
        return "server/modules/experiment";
    }

    @Override
    protected String getProjectName()
    {
        return PROJECT_NAME;
    }

    protected void doCleanup(boolean afterTest) throws TestTimeoutException
    {
        deleteProject(getProjectName(), afterTest);
    }

    protected void doTestSteps() throws InterruptedException
    {
        _containerHelper.createProject(PROJECT_NAME, null);
        createSubfolder(PROJECT_NAME, FOLDER_NAME, new String[] { "Experiment", "Query" });
        addWebPart("Data Pipeline");
        addWebPart("Run Groups");
        clickButton("Setup");
        setPipelineRoot(getLabKeyRoot() + "/sampledata/xarfiles/expVerify");
        clickFolder(FOLDER_NAME);
        clickButton("Process and Import Data");

        FileBrowserHelper fileBrowserHelper = new FileBrowserHelper(this);
        fileBrowserHelper.importFile("experiment.xar.xml", "Import Experiment");
        clickAndWait(Locator.linkWithText("Data Pipeline"));
        assertLinkNotPresentWithText("ERROR");
        int seconds = 0;
        while (!isLinkPresentWithText("COMPLETE") && seconds++ < MAX_WAIT_SECONDS)
        {
            sleep(1000);
            clickTab("Pipeline");
        }

        if (!isLinkPresentWithText("COMPLETE"))
            fail("Import did not complete.");

        clickFolder(FOLDER_NAME);
        clickAndWait(Locator.linkWithText(EXPERIMENT_NAME));
        assertTextPresent("Example 5 Run");
        clickAndWait(Locator.linkWithText(RUN_NAME));
        clickAndWait(Locator.linkWithText("Graph Summary View"));
        clickAndWait(Locator.imageMapLinkByTitle("graphmap", RUN_NAME_IMAGEMAP));
        clickAndWait(Locator.imageMapLinkByTitle("graphmap", DATA_OBJECT_TITLE));
        assertTextPresent("CAexample_mini.mzXML");
        assertTextPresent("Not available on disk");

        // Write a simple custom query that wraps the data table
        clickTab("Query");
        createNewQuery("exp");
        setFormElement(Locator.name("ff_newQueryName"), "dataCustomQuery");
        selectOptionByText(Locator.name("ff_baseTableName"), "Data");
        clickButton("Create and Edit Source");
        setCodeEditorValue("queryText", "SELECT Datas.Name AS Name,\n" +
                "Datas.RowId AS RowId,\n" +
                "Datas.Run AS Run,\n" +
                "Datas.DataFileUrl AS DataFileUrl,\n" +
                "substring(Datas.DataFileUrl, 0, 7) AS DataFileUrlPrefix,\n" +
                "Datas.Created AS Created\n" +
                "FROM Datas");
        clickButton("Execute Query", 0);        

        // Check that it contains the date format we expect
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        waitForText(dateFormat.format(new Date()), WAIT_FOR_JAVASCRIPT);
        assertTextPresent("file:/", 10);

        // Edit the metadata to use a special date format
        _extHelper.clickExtTab("Source");
        clickButton("Save", 0);
        waitForText("Saved", WAIT_FOR_JAVASCRIPT);
        clickButton("Edit Metadata");
        waitForElement(Locator.name("ff_label5"), WAIT_FOR_JAVASCRIPT);
        _listHelper.setColumnLabel(5, "editedCreated");
        _extHelper.clickExtTab("Format");
        setFormElement(Locator.id("propertyFormat"), "ddd MMM dd yyyy");
        clickButton("Save", 0);
        waitForText("Save successful.", WAIT_FOR_JAVASCRIPT);

        // Verify that it ended up in the XML version of the metadata
        clickButton("Edit Source");
        sleep(1000);
        _extHelper.clickExtTab("XML Metadata");
        assertTextPresent("<columnTitle>editedCreated</columnTitle>");
        assertTextPresent("<formatString>ddd MMM dd yyyy</formatString>");

        // Run it and see if we used the format correctly
        _extHelper.clickExtTab("Data");
        waitForText("editedCreated", WAIT_FOR_JAVASCRIPT);
        dateFormat = new SimpleDateFormat("ddd MMM dd yyyy");
        waitForText(dateFormat.format(new Date()), WAIT_FOR_JAVASCRIPT);

        // Add a new wrapped column to the exp.Datas table
        clickTab("Query");
        selectQuery("exp", "Data"); // Select the one we want to edit
        waitForElement(Locator.linkWithText("edit metadata"), WAIT_FOR_JAVASCRIPT); //on Ext panel
        clickAndWait(Locator.linkWithText("edit metadata"));
        waitForElement(Locator.xpath("//span[contains(text(), 'Reset to Default')]"), defaultWaitForPage);
        click(Locator.xpath("//span").append(Locator.navButton("Alias Field")));
        selectOptionByText(Locator.name("sourceColumn"), "RowId");
        click(Locator.xpath("//span").append(Locator.navButton("OK")));

        // Make it a lookup into our custom query
        int fieldCount = getXpathCount(Locator.xpath("//input[contains(@name, 'ff_type')]"));
        assertTrue(fieldCount > 0);
        _listHelper.setColumnType(fieldCount - 1, new ListHelper.LookupInfo(null, "exp", "dataCustomQuery"));
        mouseClick(Locator.name("ff_type" + (fieldCount - 1)).toString());

        // Save it
        clickButton("Save", 0);
        waitForText("Save successful.", WAIT_FOR_JAVASCRIPT);
        clickButton("View Data");

        // Customize the view to add the newly joined column
        _customizeViewsHelper.openCustomizeViewPanel();
        _customizeViewsHelper.addCustomizeViewColumn("WrappedRowId/Created", "Wrapped Row Id editedCreated");
        _customizeViewsHelper.applyCustomView();
        // Verify that it was joined and formatted correctly
        assertTextPresent(dateFormat.format(new Date()), 5);

        // Since this metadata is shared, clear it out 
        clickAndWait(Locator.linkWithText("exp Schema"));
        // Wait for query to load
        waitForText("edit metadata");
        clickAndWait(Locator.linkWithText("edit metadata"));
        waitForElement(Locator.xpath("//span[contains(text(), 'Reset to Default')]"), defaultWaitForPage);
        click(Locator.xpath("//span").append(Locator.navButton("Reset to Default")));
        click(Locator.xpath("//span").append(Locator.navButton("OK")));
        waitForText("Reset successful", WAIT_FOR_JAVASCRIPT);
    }
}
