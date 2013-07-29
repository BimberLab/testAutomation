/*
 * Copyright (c) 2011-2012 LabKey Corporation
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

/**
 * User: elvan
 * Date: 6/22/11
 * Time: 11:40 AM
 */

import org.junit.experimental.categories.Category;
import org.labkey.test.categories.Weekly;

/**This class is for security related tests that should be run only weekly,
 * because they are time consuming, unlikely to fail, or otherwise do not need
 */
@Category({Weekly.class})
public class SecurityTestExtended extends SecurityTest
{

    protected void doTestSteps()
    {
        enableEmailRecorder();

        clonePermissionsTest();
        cantReachAdminToolFromUserAccount(true);
    }
}
