/*
 * Copyright (c) 2012-2013 LabKey Corporation
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
package org.labkey.test.util.ext4cmp;

import org.labkey.test.BaseWebDriverTest;
import org.labkey.test.util.LogMethod;
import org.labkey.test.util.LoggedParam;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
* User: markigra
* Date: 5/31/12
* Time: 10:43 PM
*/
public class Ext4CmpRefWD
{
    protected String _id;
    protected BaseWebDriverTest _test;
    protected WebElement _el;

    public Ext4CmpRefWD(String id, BaseWebDriverTest test)
    {
        this._id = id;
        this._test = test;
        this._el = test.getDriver().findElement(By.id(id));
    }

    public Ext4CmpRefWD(WebElement el, BaseWebDriverTest test)
    {
        this._id = el.getAttribute("id");
        this._test = test;
        this._el = el;
    }

    public String getId()
    {
        return _id;
    }

    public List<Ext4CmpRefWD> query(String selector)
    {
        return _test._ext4Helper.componentQuery(selector, _id, Ext4CmpRefWD.class);
    }

    public void eval(String expr, Object... args)
    {
        String script = "Ext4.getCmp('"+_id+"')." + expr + ";";
        _test.executeScript(script, args);
    }

    public Object getEval(String expr, Object... args)
    {
        String script = "return Ext4.getCmp('"+_id+"')." + expr + ";";
        return _test.executeScript(script, args);
    }

    public Object getFnEval(String expr, Object... args)
    {
        String script = "var cmp = Ext4.getCmp('" + _id + "'); return (function(){" + expr + "}).apply(cmp, arguments);";
        return _test.executeScript(script, args);
    }

    public void waitForEnabled()
    {
        _test.waitFor(new BaseWebDriverTest.Checker()
        {
            @Override
            public boolean check()
            {
                return (Boolean)getFnEval("return !this.isDisabled();");
            }
        }, "Component was not enabled", BaseWebDriverTest.WAIT_FOR_JAVASCRIPT);
    }

    @LogMethod(quiet = true)
    public static void waitForComponent(final BaseWebDriverTest test, @LoggedParam final String query)
    {
        test.waitFor(new BaseWebDriverTest.Checker()
        {
            @Override
            public boolean check()
            {
                return (Boolean)test.executeScript("return !!Ext4.ComponentQuery.query(\"" + query + "\");");
            }
        }, "Component did not appear: " + query, BaseWebDriverTest.WAIT_FOR_JAVASCRIPT);
    }
}
