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
package org.labkey.test.util;

import org.junit.Assert;
import org.labkey.test.BaseSeleniumWebTest;
import org.labkey.test.Locator;

/**
 * Created by IntelliJ IDEA.
 * User: elvan
 * Date: 9/12/12
 * Time: 12:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class UIUserHelper extends AbstractUserHelper
{
    public UIUserHelper(BaseSeleniumWebTest test)
    {
        super(test);
    }

    @Override
    public void createUser(String userName, boolean verifySuccess)
    {
            _test.goToHome();
            _test.ensureAdminMode();
            _test.goToSiteUsers();
            _test.clickButton("Add Users");

            _test.setFormElement(Locator.name("newUsers"), userName);
            _test.uncheckCheckbox("sendMail");
//            if (cloneUserName != null)
//            {
//                checkCheckbox("cloneUserCheck");
//                setFormElement("cloneUser", cloneUserName);
//            }
            _test.clickButton("Add Users");

            if (verifySuccess)
                Assert.assertTrue("Failed to add user " + userName, _test.isTextPresent(userName + " added as a new user to the system"));

    }
}
