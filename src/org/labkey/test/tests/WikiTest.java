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
import org.labkey.test.BaseWebDriverTest;
import org.labkey.test.Locator;
import org.labkey.test.categories.BVT;
import org.labkey.test.categories.Wiki;
import org.labkey.test.util.UIContainerHelper;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import java.io.File;

/**
 * User: brittp
 * Date: Nov 15, 2005
 * Time: 1:55:56 PM
 */
@Category({BVT.class, Wiki.class})
public class WikiTest extends BaseWebDriverTest
{
    private static final String PROJECT_NAME = TRICKY_CHARACTERS_FOR_PROJECT_NAMES +  "WikiVerifyProject";

    private static final String WIKI_PAGE_ALTTITLE = "PageBBB has HTML";
    private static final String WIKI_PAGE_WEBPART_ID = "qwp999";
    private static final String WIKI_PAGE_TITLE = "Test Wiki";
    private static final String WIKI_PAGE_CONTENT =
            "<b>Some HTML content</b>\n" +
                    "<b>${labkey.webPart(partName='Query', title='My Proteins', schemaName='ms2', " +
                    "queryName='Sequences', allowChooseQuery='true', allowChooseView='true')}</b>\n";
    private static final String WIKI_CHECK_CONTENT = "More HTML content";

    public WikiTest()
    {
        setContainerHelper(new UIContainerHelper(this));
    }
    public String getAssociatedModuleDirectory()
    {
        return "server/modules/wiki";
    }

    @Override
    protected String getProjectName()
    {
        return PROJECT_NAME;
    }

    protected String getSubolderName()
    {
          return "Subfolder";
    }

    @Override
    protected boolean isFileUploadTest()
    {
        return true;
    }

    protected void doTestSteps()
    {
        log("Create Project");
        _containerHelper.createProject(PROJECT_NAME, null);
        goToFolderManagement();
        clickAndWait(Locator.linkWithText("Folder Type"));
        checkCheckbox(Locator.checkboxByTitle("Wiki"));
        submit();

        enableModule(PROJECT_NAME, "MS2");

        goToAdminConsole();
        clickAndWait(Locator.linkWithText("full-text search"));
        if (isTextPresent("pause crawler"))
            clickButton("pause crawler");
        beginAt(getDriver().getCurrentUrl().replace("admin.view","waitForIdle.view"), 10*defaultWaitForPage);

        clickProject(PROJECT_NAME);
        addWebPart("Wiki");
        addWebPart("Search");

        log("test create new html page with a webpart");
        createNewWikiPage("HTML");

        setFormElement(Locator.name("name"), WIKI_PAGE_TITLE);
        setFormElement(Locator.name("title"), WIKI_PAGE_TITLE);
        setWikiBody(WIKI_PAGE_CONTENT);

        log("test attachments in wiki");
        click(Locator.linkWithText("Attach a file"));
        File file = new File(getLabKeyRoot() + "/common.properties");
        setFormElement(Locator.name("formFiles[0]"), file);
        saveWikiPage();

        waitForElement(Locator.id(WIKI_PAGE_WEBPART_ID));
        assertTextPresent("common.properties");
        assertTextPresent("Some HTML content");

        log("test search wiki");
        searchFor(PROJECT_NAME, "Wiki", 1, WIKI_PAGE_TITLE);

        log("test edit wiki");
        clickAndWait(Locator.linkWithText("Edit"));
        setFormElement(Locator.name("title"), WIKI_PAGE_ALTTITLE);
        String wikiPageContentEdited =
            "<b>Some HTML content</b><br>\n" +
            "<b>" + WIKI_CHECK_CONTENT + "</b><br>\n";
        setWikiBody(wikiPageContentEdited);
        switchWikiToVisualView();
        saveWikiPage();
        verifyWikiPagePresent();

        doTestInlineEditor();

        log("Verify fix for issue 13937: NotFoundException when attempting to display a wiki from a different folder which has been deleted");
        createSubfolder(getProjectName(), getSubolderName(), new String[] {});
        addWebPart("Wiki");
        clickWebpartMenuItem("Wiki", "Customize");
        selectOptionByText(Locator.name("webPartContainer"), "/"+getProjectName());
        waitForElement(Locator.xpath("//option[@value='Test Wiki']"));
        clickButton("Submit");
        verifyWikiPagePresent();

        log("test delete wiki");
        goToProjectHome();
        _extHelper.clickExtMenuButton(true, Locator.tagWithAttribute("img", "title", "More"), "Edit");
        clickButton("Delete Page");
        clickButton("Delete");
        assertTextNotPresent(WIKI_PAGE_ALTTITLE);

        log("verify second wiki part pointing to first handled delete well");
        clickFolder(getSubolderName());
        assertTextPresent("This folder does not currently contain any wiki pages to display");
    }

    protected void verifyWikiPagePresent()
    {
        waitForText(WIKI_CHECK_CONTENT);
        assertTextPresent(WIKI_PAGE_ALTTITLE);
    }

    protected void doTestInlineEditor()
    {
        Locator.XPathLocator inlineEditor = Locator.xpath("//div[@class='labkey-inline-editor']");

        log("** test inline wiki webpart editor");
        goToProjectHome();
        click(Locator.tagWithAttribute("img", "title", "Edit"));
        waitForElement(inlineEditor);
        String editorId = getAttribute(inlineEditor.child("textarea"), "id");

        String addedContent = "Inline edited content";
        setInlineEditorContent(editorId, addedContent);
        clickButton("Save", 0);
        waitForElementToDisappear(inlineEditor);
        assertTextPresent(addedContent);
        assertTextNotPresent(WIKI_CHECK_CONTENT);
        assertNavButtonNotPresent("Save");

        log("** test second edit on inline wiki webpart editor");
        click(Locator.tagWithAttribute("img", "title", "Edit"));
        waitForElement(inlineEditor);
        addedContent = "Second inline edited content: " + WIKI_CHECK_CONTENT;
        setInlineEditorContent(editorId, addedContent);
        clickButton("Save", 0);
        waitForElementToDisappear(inlineEditor);
        assertTextPresent(addedContent);

        log("** test cancel on inline wiki webpart editor");
        click(Locator.tagWithAttribute("img", "title", "Edit"));
        waitForElement(inlineEditor);
        setInlineEditorContent(editorId, addedContent);
        clickButton("Cancel", 0);
        waitForElementToDisappear(inlineEditor);
        assertTextPresent(addedContent);
        assertTextNotPresent("SHOULD NOT BE SAVED");

        // check that the content was actually saved in the previous steps
        log("** check inline wiki webpart edit is persisted");
        refresh();
        assertTextPresent(addedContent);
    }

    protected void setInlineEditorContent(String editorId, String content)
    {
        // swtich to the tinymce iframe
        getDriver().switchTo().frame(editorId + "_ifr");

        // locate the tinymce body element
        Locator l = Locator.id("tinymce");

        // send keypress to the element
        WebElement el = l.findElement(getDriver());

        // select all then delete previous content (can't seem to get "select all" via Ctrl-A to work)
        el.sendKeys(Keys.HOME);
        for (int i = 0; i < 10; i++)
            el.sendKeys(Keys.chord(Keys.SHIFT, Keys.DOWN));
        el.sendKeys(Keys.BACK_SPACE);

        // enter new content + newline
        el.sendKeys(content);

        // switch back to parent window
        getDriver().switchTo().defaultContent();
    }

    @Override public BrowserType bestBrowser()
    {
        return BrowserType.CHROME;
    }
}
