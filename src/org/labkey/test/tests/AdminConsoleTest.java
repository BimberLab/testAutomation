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

import junit.framework.Assert;
import org.labkey.test.BaseWebDriverTest;
import org.labkey.test.Locator;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Created with IntelliJ IDEA.
 * User: bimber
 * Date: 2/21/13
 * Time: 4:10 PM
 */
public class AdminConsoleTest extends BaseWebDriverTest
{
    public String getProjectName()
    {
        return null;
    }

    public void doTestSteps() throws Exception
    {
        testRibbonBar();
    }

    public void testRibbonBar() throws Exception
    {
        goToAdminConsole();
        waitAndClick(Locator.linkContainingText("site settings"));
        waitForElement(Locator.name("showRibbonMessage"));

        WebElement el = Locator.name("ribbonMessageHtml").findElement(_driver);
        el.clear();

        WebElement checkbox = Locator.checkboxByName("showRibbonMessage").findElement(getDriver());

        //only select if not already checked
        if (!("true".equals(checkbox.getAttribute("checked"))))
            clickCheckbox("showRibbonMessage");

        clickButton("Save");

        waitForElement(Locator.xpath("//div[contains(text(), 'Cannot enable the ribbon message without providing a message to show')]"));

        String linkText = "and also click this...";
        String html = "READ ME!!!  <a href='<%=contextPath%>/project/home/begin.view'>" + linkText + "</a>";

        //only check if not already checked
        checkbox = Locator.checkboxByName("showRibbonMessage").findElement(getDriver());
        if (!("true".equals(checkbox.getAttribute("checked"))))
            clickCheckbox("showRibbonMessage");

        setFormElement(Locator.name("ribbonMessageHtml"), html);
        clickButton("Save");

        waitForElement(Locator.linkContainingText("site settings"));

        Locator ribbon = Locator.xpath("//div[contains(@class, 'labkey-warning-messages')]//li[contains(text(), 'READ ME!!!')]");
        Locator ribbonLink = Locator.xpath("//div[contains(@class, 'labkey-warning-messages')]//li[contains(text(), 'READ ME!!!')]//..//a");

        assertElementPresent(ribbon);
        assertElementPresent(ribbonLink);

        el = getDriver().findElement(By.xpath("//div[contains(@class, 'labkey-warning-messages')]//..//a"));
        Assert.assertNotNull("Link not present in ribbon bar", el);

        String href = el.getAttribute("href");
        String expected = getBaseURL() + "/project/home/begin.view";
        Assert.assertEquals("Incorrect URL", expected, href);

        goToHome();
        impersonateRole("Reader");

        assertElementPresent(ribbon);
        assertElementPresent(ribbonLink);

        stopImpersonatingRole();

        goToAdminConsole();
        waitAndClick(Locator.linkContainingText("site settings"));
        waitForElement(Locator.name("showRibbonMessage"));
        clickCheckbox("showRibbonMessage");  //deactivate
        clickButton("Save");
        waitForElement(Locator.linkContainingText("site settings"));
        assertElementNotPresent(ribbon);
        assertElementNotPresent(ribbonLink);
    }

    public String getAssociatedModuleDirectory()
    {
        return null;
    }

    @Override
    public void checkQueries()
    {

    }

    @Override
    public void checkViews()
    {

    }
}
