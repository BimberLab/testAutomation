/*
 * Copyright (c) 2017-2019 LabKey Corporation
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
package org.labkey.test.util.selenium;

import org.jetbrains.annotations.NotNull;
import org.labkey.test.Locator;
import org.labkey.test.Locators;
import org.labkey.test.components.Component;
import org.labkey.test.util.DataRegionTable;
import org.labkey.test.util.TestLogger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsDriver;
import org.openqa.selenium.WrapsElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Locatable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WebDriverUtils
{
    private WebDriverUtils()
    {
        // Do not instantiate
    }

    public static class ScrollUtil
    {
        private final WebDriver _webDriver;

        public ScrollUtil(WebDriver webDriver)
        {
            _webDriver = webDriver;
        }

        public boolean scrollUnderFloatingHeader(WebElement blockedElement)
        {
            List<WebElement> floatingHeaders = Locator.findElements(_webDriver,
                    Locators.floatingHeaderContainer(),
                    Locators.appFloatingHeader(),
                    DataRegionTable.Locators.floatingHeader().notHidden());

            int headerHeight = 0;
            for (WebElement floatingHeader : floatingHeaders)
            {
                headerHeight += floatingHeader.getSize().getHeight();
            }
            if (headerHeight > 0 && headerHeight > ((Locatable) blockedElement).getCoordinates().inViewPort().getY())
            {
                int elHeight = blockedElement.getSize().getHeight();
                scrollBy(0, -(headerHeight + elHeight));
                return true;
            }
            return false;
        }

        public WebElement scrollIntoView(WebElement el)
        {
            ((JavascriptExecutor)_webDriver).executeScript("arguments[0].scrollIntoView();", el);
            return el;
        }

        public WebElement scrollIntoView(WebElement el, Boolean alignToTop)
        {
            ((JavascriptExecutor)_webDriver).executeScript("arguments[0].scrollIntoView(arguments[1]);", el, alignToTop);
            return el;
        }

        public void scrollBy(Integer x, Integer y)
        {
            ((JavascriptExecutor)_webDriver).executeScript("window.scrollBy(" + x.toString() +", " + y.toString() + ");");
        }
    }

    public static boolean isFirefox(WebDriver driver)
    {
        return extractWrappedDriver(driver) instanceof FirefoxDriver;
    }

    /**
     * Extract a WebDriver instance from an arbitrarily wrapped object
     * @param wrapper Object that wraps a WebDriver. Typically, a Component, SearchContext, or WebElement
     * @return WebDriver instance or null if none is found
     */
    public static WebDriver extractWrappedDriver(Object wrapper)
    {
        Object peeling = wrapper;
        if (peeling instanceof Component<?> cmp)
        {
            peeling = cmp.getComponentElement();
        }
        while (peeling instanceof WrapsElement we)
        {
            peeling = we.getWrappedElement();
        }
        while (peeling instanceof WrapsDriver wd)
        {
            peeling = wd.getWrappedDriver();
        }
        if (peeling instanceof WebDriver wd)
        {
            return wd;
        }
        else
        {
            TestLogger.warn("Unable to extract WebDriver from: " +
                    (peeling == null ? "null" : (peeling.getClass() +
                            (peeling == wrapper ? "" :
                                    " Unpeeled from: " + wrapper.getClass()))));
            return null;
        }
    }

    private static final List<String> html5InputTypes = Arrays.asList("color", "date", "datetime-local", "email", "month", "number", "range", "search", "tel", "time", "url", "week");
    private static final Map<Class<? extends WebDriver>, Map<String, Boolean>> html5InputSupport = new HashMap<>();
    public static boolean isHtml5InputTypeSupported(@NotNull final String inputType, @NotNull final WebDriver driver)
    {
        if (!html5InputTypes.contains(inputType))
        {
            return false;
        }
        Class<? extends WebDriver> driverClass = extractWrappedDriver(driver).getClass();
        Map<String, Boolean> currentSupport = html5InputSupport.computeIfAbsent(driverClass, k -> new HashMap<>());
        return currentSupport.computeIfAbsent(inputType, k -> (Boolean) ((JavascriptExecutor) driver)
                .executeScript("""
                                var i = document.createElement('input');
                                i.setAttribute('type', arguments[0]);
                                return i.type === arguments[0];
                                """
                , k));
    }

}
