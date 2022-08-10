/*
 * Copyright (c) 2016-2019 LabKey Corporation
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
package org.labkey.test.components.study;

import org.labkey.test.Locator;
import org.labkey.test.components.Component;
import org.labkey.test.components.ext4.Checkbox;
import org.labkey.test.util.DataRegionTable;
import org.openqa.selenium.WebElement;

import java.util.HashMap;
import java.util.Map;

import static org.labkey.test.util.Ext4Helper.getCssPrefix;

public class DatasetFacetPanel extends Component<DatasetFacetPanel.Elements>
{
    WebElement _panelEl;
    DataRegionTable _dataRegion;

    public DatasetFacetPanel(WebElement panelEl, DataRegionTable dataRegion)
    {
        _panelEl = panelEl;
        _dataRegion = dataRegion;
    }

    @Override
    public WebElement getComponentElement()
    {
        return _panelEl;
    }

    public Checkbox getGroupCheckbox(String label)
    {
        return getGroupCheckbox(label, 0);
    }

    public Checkbox getGroupCheckbox(String label, int index)
    {
        return elementCache().getGroupRow(label, index).getCheckbox();
    }

    public Checkbox getCategoryCheckbox(String label)
    {
        return getCategoryCheckbox(label, 0);
    }

    public Checkbox getCategoryCheckbox(String label, int index)
    {
        return elementCache().getCategoryRow(label, index).getCheckbox();
    }

    public void clickGroupLabel(String label)
    {
        clickGroupLabel(label, 0);
    }

    public void clickGroupLabel(String label, int index)
    {
        elementCache().getGroupRow(label, index).clickLabel();
    }

    public void toggleAll()
    {
        elementCache().allRow.getCheckbox().toggle();
    }

    public void checkAll()
    {
        elementCache().allRow.getCheckbox().check();
    }

    public void uncheckAll()
    {
        elementCache().allRow.getCheckbox().check(); // in case some are checked
        elementCache().allRow.getCheckbox().uncheck();
    }

    @Override
    protected Elements newElementCache()
    {
        return new Elements();
    }

    public class Elements extends Component<?>.ElementCache
    {
        Map<String, Map<Integer, GroupRow>> groupRows = new HashMap<>();
        Map<String, Map<Integer, CategoryRow>> categoryRows = new HashMap<>();
        GroupRow allRow = new GroupRow("All", 0);

        protected GroupRow getGroupRow(String label, int index)
        {
            if ("All".equals(label))
                index++; // 'All' looks like a group row, but skip it if you're looking for a group named 'All'

            return groupRows
                    .computeIfAbsent(label, l -> new HashMap<>())
                    .computeIfAbsent(index, i -> new GroupRow(label, i));
        }

        protected CategoryRow getCategoryRow(String label, int index)
        {
            return categoryRows
                    .computeIfAbsent(label, l -> new HashMap<>())
                    .computeIfAbsent(index, i -> new CategoryRow(label, i));
        }
    }

    private class GroupRow extends Component.Simple
    {
        final private WebElement _row;
        final private WebElement _label;
        final private Checkbox _checkbox;

        public GroupRow(String label)
        {
            this(label, 0);
        }

        public GroupRow(String label, int index)
        {
            this(label, index,
                    Locator.tagWithClass("tr", getCssPrefix() + "grid-data-row"),
                    Locator.tagWithClass("div", getCssPrefix() + "grid-row-checker"));
        }
        
        protected GroupRow(String label, int index, Locator.XPathLocator rowLoc, Locator checkboxLoc)
        {
            Locator.XPathLocator labelLoc = Locator.tagWithClass("div", "lk-filter-panel-label");
            _row = rowLoc.withDescendant(labelLoc.withText(label))
                    .index(index).findWhenNeeded(_panelEl);
            _checkbox = new Checkbox(checkboxLoc.findWhenNeeded(_row)){
                @Override
                public void toggle()
                {
                    _dataRegion.doAndWaitForUpdate(super::toggle);
                }
            };
            _label = labelLoc.findWhenNeeded(_row);
        }

        @Override
        public WebElement getComponentElement()
        {
            return _row;
        }

        public Checkbox getCheckbox()
        {
            return _checkbox;
        }

        public void clickLabel()
        {
            _dataRegion.doAndWaitForUpdate(_label::click);
        }
    }

    private class CategoryRow extends GroupRow
    {
        public CategoryRow(String label)
        {
            this(label, 0);
        }

        public CategoryRow(String label, int index)
        {
            super(label, index,
                    Locator.tagWithClass("div", getCssPrefix() + "grid-group-title"),
                    Locator.tagWithClass("div", "category-header"));
        }
    }

    public static abstract class Locators
    {
        private static Locator.XPathLocator facetPanel(String regionName)
        {
            return Locator.tagWithAttribute("div", "lk-region-facet-name", regionName);
        }

        public static Locator.XPathLocator expandedFacetPanel(String regionName)
        {
            return facetPanel(regionName).withDescendant(Locator.xpath("div").withPredicate("not(contains(@class, 'x4-panel-collapsed'))").withClass("labkey-data-region-facet"));
        }
    }
}
