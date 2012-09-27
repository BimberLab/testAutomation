/*
 * Copyright (c) 2012 LabKey Corporation
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

import org.labkey.test.BaseSeleniumWebTest;
import org.labkey.test.Locator;
import org.labkey.test.util.Ext4Helper;

/**
 * User: elvan
 * Date: 6/28/12
 * Time: 6:20 PM
 */
public class Ext4FileFieldRef extends Ext4CmpRef
{
    public Ext4FileFieldRef(String id, BaseSeleniumWebTest test)
    {
        super(id, test);
    }

    //often there is only one file field on screen, so we'll just grab that
    public static Ext4FileFieldRef create(BaseSeleniumWebTest test)
    {
        return test._ext4Helper.queryOne("filefield", Ext4FileFieldRef.class);
    }

    public void setToFile(String file)
    {
        _test.setFormElement(Locator.id(eval("this.fileInputEl.dom.id")), file);
        this.eval("this.onFileChange()");
    }
}
