/*
 * Copyright (c) 2008-2012 LabKey Corporation
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
package org.labkey.test.ms1.params;

import org.labkey.test.pipeline.PipelineWebTestBase;
import org.labkey.test.util.ExtHelper;

/**
 * InspectTestParams class
* <p/>
* Created: Aug 18, 2008
*
* @author bmaclean
*/
public class FeaturesTestParams extends AbstractInspectTestParams
{
    public FeaturesTestParams(PipelineWebTestBase test, String dataPath, String protocolName,
                             boolean cachesExist, String... sampleNames)
    {
        super(test, dataPath, "inspect", protocolName, ".peptides.tsv", cachesExist, sampleNames);
    }

    public void clickActionButton()
    {
        String[] names = getSampleNames();
        if (names.length != 0)
        {
            _test._extHelper.waitForImportDataEnabled();
            for (String name : names)
            {
                _test._extHelper.clickFileBrowserFileCheckbox(name + ".mzXML");
                getTest().sleep(1000);
            }
        }
        _test.log("msInspect run");
        _test.selectImportDataAction("msInspect Find Features");
    }
}