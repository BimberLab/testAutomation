/*
 * Copyright (c) 2009-2013 LabKey Corporation
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

import org.labkey.test.BaseWebDriverTest;
import org.labkey.test.Locator;
import org.labkey.test.TestTimeoutException;
import org.labkey.test.util.PortalHelper;

import java.io.File;

/**
 * User: marki
 * Date: May 18, 2009
 * Time: 12:44:03 PM
 */
public class MenuBarTest extends BaseWebDriverTest
{

    private static final String PROJECT_NAME = "MenuBarVerifyProject";
    private static final String WIKI_PAGE_TITLE = "A Wiki Menu" + TRICKY_CHARACTERS_FOR_PROJECT_NAMES;
    private static final String WIKI_PAGE_CONTENT = "This is a fancy wiki";

    private static final String STUDY_ZIP = "/sampledata/study/LabkeyDemoStudy.zip";
    private static final String DEM_STUDY_FOLDER = "DemStudyFolder";
    private static final String STUDY_FOLDER = "StudyFolder";

    public String getAssociatedModuleDirectory()
    {
        return "server/modules/core";
    }

    @Override
    protected String getProjectName()
    {
        return PROJECT_NAME;
    }

//    @Override
//    protected BrowserType bestBrowser()
//    {
//        return BrowserType.CHROME;
//    }

    protected void doTestSteps()
    {
        PortalHelper portalHelper = new PortalHelper(this);

        log("Open new project");
        _containerHelper.createProject(PROJECT_NAME, "Collaboration");
        goToProjectSettings();
        clickAndWait(Locator.linkWithText("Menu Bar"));

        portalHelper.addWebPart("AssayList2");
        portalHelper.addWebPart("Study List");
        portalHelper.addWebPart("Wiki Menu");

        clickProject(PROJECT_NAME);

        goToModule("Wiki");
        createNewWikiPage("HTML");
        setFormElement(Locator.name("name"), WIKI_PAGE_TITLE);
        setFormElement(Locator.name("title"), WIKI_PAGE_TITLE);
        setWikiBody(WIKI_PAGE_CONTENT);
        saveWikiPage();
        goToProjectSettings();
        clickAndWait(Locator.linkWithText("Menu Bar"));

        log("Test wiki customization");
        portalHelper.clickWebpartMenuItem("Wiki", "Customize");
        selectOptionByText(Locator.name("webPartContainer"), "/" + PROJECT_NAME);
        waitForElement(Locator.tagWithText("option", WIKI_PAGE_TITLE + " (" + WIKI_PAGE_TITLE + ")"), 2000);
        selectOptionByText(Locator.name("name"), WIKI_PAGE_TITLE + " (" + WIKI_PAGE_TITLE + ")");
        clickButton("Submit");
        
        clickProject(PROJECT_NAME);

        //Make sure that the menus are shown, but the content is not yet loaded.
        assertElementPresent(Locator.id("folderBar"));
        assertElementPresent(Locator.menuBarItem("Assays"));
        assertElementPresent(Locator.menuBarItem("Studies"));
        assertElementPresent(Locator.menuBarItem(WIKI_PAGE_TITLE));

        log("Assert wiki, assay, and study portals not loaded");
        assertTextNotPresent(WIKI_PAGE_CONTENT);
        assertNavButtonNotPresent("Manage Assays");
        assertTextNotPresent("No Studies Found");

        hoverMenu(WIKI_PAGE_TITLE);
        waitForElement(Locator.xpath("//div").withClass("labkey-wiki").withText(WIKI_PAGE_CONTENT));
        hoverMenu("Assays");
        waitForElement(Locator.navButton("Manage Assays"), 3000);

        _assayHelper.uploadXarFileAsAssayDesign(getSampledataPath() + "/menubar/Test Assay.xar", 1, "Test Assay.xar");
        clickProject(PROJECT_NAME);

        assertTextNotPresent("Test Assay");
        hoverMenu("Assays");
        waitForElement(Locator.linkWithText("Test Assay"));

        createSubfolder(PROJECT_NAME, PROJECT_NAME, "StudyFolder", "Study", null);
        createDefaultStudy();
        clickProject(getProjectName());

        hoverMenu("Studies");
        waitForElement(Locator.linkWithText("StudyFolder Study"), WAIT_FOR_JAVASCRIPT);


        // Custom Menu
        log("Test Custom Menu WebPart");
        goToProjectSettings();
        clickAndWait(Locator.linkWithText("Menu Bar"));
        portalHelper.addWebPart("Custom Menu");

        // Schema/Query/etc
        _extHelper.setExtFormElementByLabel("Title", "Wiki Render Types");
        _extHelper.waitForLoadingMaskToDisappear(WAIT_FOR_JAVASCRIPT);
        waitForElementToDisappear(Locator.css(".ext-el-mask"));
        _extHelper.selectComboBoxItem("Schema", "wiki");
        waitForElementToDisappear(Locator.css(".ext-el-mask"));
        _extHelper.selectComboBoxItem("Query", "renderertype");
        waitForElementToDisappear(Locator.css(".ext-el-mask"));
        _extHelper.selectComboBoxItem("Title Column", "Value");
        _extHelper.clickExtButton("Submit");

        clickProject(getProjectName());
        hoverMenu("Wiki Render Types");
        waitForElement(Locator.xpath("//div[@id='Custom Menu7$Header_menu']").containing("HTML"));
        assertElementPresent(Locator.xpath("//div[@id='Custom Menu7$Header_menu']").containing("RADEOX"));
        assertElementPresent(Locator.xpath("//div[@id='Custom Menu7$Header_menu']").containing("TEXT_WITH_LINKS"));

        // Another custom menu with links to Participant Report
        createSubfolder(PROJECT_NAME, PROJECT_NAME, DEM_STUDY_FOLDER, "Study", null);
        importStudyFromZip(new File(getLabKeyRoot(), STUDY_ZIP));

        goToProjectSettings();
        clickAndWait(Locator.linkWithText("Menu Bar"));
        portalHelper.addWebPart("Custom Menu");
        _extHelper.waitForLoadingMaskToDisappear(WAIT_FOR_JAVASCRIPT);
        _extHelper.setExtFormElementByLabel("Title", "Participant Reports");
        _extHelper.selectComboBoxItem("Folder", DEM_STUDY_FOLDER);
        waitForElementToDisappear(Locator.css(".ext-el-mask"));
        _extHelper.selectComboBoxItem("Schema", "study");
        waitForElementToDisappear(Locator.css(".ext-el-mask"));
        _extHelper.selectComboBoxItem("Query", "Participant");
        waitForElementToDisappear(Locator.css(".ext-el-mask"));
        _extHelper.selectComboBoxItem("Title Column", "ParticipantId");
        _extHelper.setExtFormElementByLabel("URL", "/study-samples/typeParticipantReport.view?participantId=${participantId}");
        _extHelper.clickExtButton("Submit");

        // Should take us to participant report page
        clickProject(getProjectName());
        hoverMenu("Participant Reports");
        waitAndClickAndWait(Locator.linkWithText("249320107"));
        assertTextPresent("Specimen Report: Participant 249320107");

        // Another custom Menu with links to folders
        goToProjectSettings();
        clickAndWait(Locator.linkWithText("Menu Bar"));
        portalHelper.addWebPart("Custom Menu");
        _extHelper.setExtFormElementByLabel("Title", "Folders");

        Locator radioFolder = Locator.radioButtonById("folder-radio");
        click(radioFolder);
        _extHelper.selectComboBoxItem("Folder Types", "Study");
        clickButton("Submit");

        clickFolder(DEM_STUDY_FOLDER);
        assertTextPresent("Demo Study", "Study Overview");
        hoverMenu("Folders");
        waitForElement(Locator.linkWithText(DEM_STUDY_FOLDER));
        assertElementPresent(Locator.linkWithText(STUDY_FOLDER));
    }

    protected void hoverMenu(String menuText)
    {
        Locator menuItem = Locator.css("#menubar .labkey-main-menu-item").withText(menuText);
        String menuId = menuItem.findElement(getDriver()).getAttribute("id");
        String hoverNavigationPart = "_" + menuId.split("\\$")[0];
        executeScript("HoverNavigation.Parts[\"" +hoverNavigationPart + "\"].show();");
    }

    protected void doCleanup(boolean afterTest) throws TestTimeoutException
    {
        deleteProject(getProjectName(), afterTest);
    }

    @Override
    protected BrowserType bestBrowser()
    {
        return BrowserType.CHROME;
    }
}
