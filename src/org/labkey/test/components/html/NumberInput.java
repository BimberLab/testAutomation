package org.labkey.test.components.html;

import org.labkey.test.util.selenium.WebDriverUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class NumberInput extends Input
{
    public NumberInput(WebElement el, WebDriver driver)
    {
        super(el, driver);
    }

    @Override
    public void set(String value)
    {
        if (WebDriverUtils.isHtml5InputTypeSupported("number", getDriver()))
        {
            setHtml5(value);
        }
        else
        {
            super.set(value);
        }
    }

    private void setHtml5(String value)
    {
        WebElement el = getComponentElement();
        el.clear();

        // Calling clear on the element doesn't reliably clear the field, if it fails try the hack.
        String valueAsNumber = el.getAttribute("valueAsNumber").trim().toLowerCase();
        if(!valueAsNumber.equals("nan"))
        {
            getWrapper().actionClear(el);
        }

        // Sometimes focus is lost before the value is set, so make sure the element has focus.
        el.click();

        el.sendKeys(value);
    }

}
