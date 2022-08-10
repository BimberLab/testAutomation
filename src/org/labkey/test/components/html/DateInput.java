package org.labkey.test.components.html;

import org.junit.Assert;
import org.labkey.test.WebDriverWrapper;
import org.labkey.test.util.selenium.WebDriverUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateInput extends Input
{
    public DateInput(WebElement el, WebDriver driver)
    {
        super(el, driver);
    }

    @Override
    public void set(String value)
    {
        String inputFormat = "yyyy-MM-dd";
        SimpleDateFormat inputFormatter = new SimpleDateFormat(inputFormat);

        try
        {
            set(inputFormatter.parse(value));
        }
        catch (ParseException e)
        {
            throw new IllegalArgumentException("Unable to parse date " + value + ". Format should be " + inputFormat);
        }
    }

    public void set(Date date)
    {
        // Firefox requires ISO date format (yyyy-MM-dd)
        String formFormat = WebDriverUtils.isFirefox(getDriver()) ? "yyyy-MM-dd" : "MMddyyyy";
        SimpleDateFormat formFormatter = new SimpleDateFormat(formFormat);
        String formDate = formFormatter.format(date);

        getWrapper().fireEvent(getComponentElement(), WebDriverWrapper.SeleniumEvent.focus);
        getWrapper().executeScript("arguments[0].value = ''", getComponentElement());
        getComponentElement().sendKeys(formDate);
    }

    @Override
    protected void assertElementType(WebElement el)
    {
        super.assertElementType(el);
        String type = el.getAttribute("type");
        Assert.assertEquals("Not a date input: " + el, "date", type);
    }
}
