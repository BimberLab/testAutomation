/*
 * Copyright (c) 2014-2019 LabKey Corporation
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

import org.labkey.test.BaseWebDriverTest;

public class BoxPlotReportHelper extends GenericChartHelper
{
    /**
     * Creating a plot from manage views page
     */
    public BoxPlotReportHelper(BaseWebDriverTest test)
    {
        super(test, null, "Cohort", null);
    }

    public BoxPlotReportHelper(BaseWebDriverTest test, String sourceDataset)
    {
        super(test, null, "Cohort", null);
        _sourceQuery = sourceDataset;
    }

    public BoxPlotReportHelper(BaseWebDriverTest test, String yMeasure, String xMeasure, String title)
    {
        super(test, yMeasure, xMeasure, title);
    }
}
