/*
 * Copyright (c) 2007-2012 LabKey Corporation
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

import junit.framework.Assert;
import org.apache.commons.lang3.StringUtils;
import org.labkey.test.BaseSeleniumWebTest;
import org.labkey.test.Locator;
import org.labkey.test.SortDirection;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * DataRegionTable class
 * <p/>
 * Created: Feb 19, 2007
 *
 * @author bmaclean
 */
public class DataRegionTable
{
    protected final String _tableName;
    protected BaseSeleniumWebTest _test;
    protected final boolean _selectors;
    protected final Map<String, Integer> _mapColumns = new HashMap<String, Integer>();
    protected final Map<String, Integer> _mapRows = new HashMap<String, Integer>();
    protected final int _columnCount;
    protected final int _headerRows;

    public DataRegionTable(String tableName, BaseSeleniumWebTest test)
    {
        this(tableName, test, true, true);
    }

    public DataRegionTable(String tableName, BaseSeleniumWebTest test, boolean selectors)
    {
        this(tableName, test, selectors, true);
    }

    public DataRegionTable(String tableName, BaseSeleniumWebTest test, boolean selectors, boolean floatingHeaders)
    {
        _tableName = tableName;
        _selectors = selectors;
        reload(test);
        _columnCount = _test.getTableColumnCount(getHtmlName());
        _headerRows = 2 + (floatingHeaders?2:0);
        _test.assertElementPresent(Locator.xpath("//table[@id=" + Locator.xq(getHtmlName()) + "]"));
    }

    public String getTableName()
    {
        return _tableName;
    }

    public String getHtmlName()
    {
        return "dataregion_" + _tableName;
    }

    public void reload(BaseSeleniumWebTest test)
    {
        _test = test;
    }

    private boolean bottomBarPresent()
    {
        return _test.isElementPresent(Locator.xpath("//table[starts-with(@id, 'dataregion_footer_')]"));
    }

    private String getJSParam(String func, int i, boolean bStr)
    {
        int lParen = func.indexOf("(");
        int rParen = func.indexOf(")");
        if (lParen == -1 || lParen > rParen)
            return null;

        String[] params = func.substring(lParen + 1, rParen).split(",");
        String param = params[i].trim();
        if (!bStr)
            return param;

        if ((param.charAt(0) != '"' || param.charAt(param.length() - 1) != '"') &&
                (param.charAt(0) != '\'' || param.charAt(param.length() - 1) != '\''))
            return null;

        return param.substring(1, param.length() - 1);
    }

    public int getDataRowCount()
    {
        int rows = 0;
        rows = _test.getTableRowCount(getHtmlName()) - (_headerRows + (bottomBarPresent()?1:0));

        if (rows == 1 && hasNoDataToShow())
            rows = 0;

        return rows;
    }

    public int getRow(String columnLabel, String value)
    {
        return getRow(getColumn(columnLabel), value);
    }

    public int getRow(int columnIndex, String value)
    {
        int rowCount = getDataRowCount();
        for(int i=0; i<rowCount; i++)
        {
            if(value.equals(getDataAsText(i, columnIndex)))
                return i;
        }
          return -1;
    }

    public String getTotal(String columnLabel)
    {
        return getTotal(getColumn(columnLabel));
    }

    public String getTotal(int columnIndex)
    {
        return _test.getText(Locator.css("#" + getHtmlName() + " tr.labkey-col-total > td:nth-of-type("+(columnIndex +(_selectors?2:1))+")"));
    }

    /**
     * do nothing if column is already present, add it if it is not
     * @param columnName   name of column to add, if necessary
     */
    public void ensureColumnPresent(String columnName)
    {
        if(getColumn(columnName) > -1)
            return;
        else
        {
            _test._customizeViewsHelper.openCustomizeViewPanel();
            _test._customizeViewsHelper.addCustomizeViewColumn(columnName);
            _test._customizeViewsHelper.applyCustomView();
        }
    }

    /**
     * check for presence of columns, add them if they are not already present
     * requires columns actually exist
     * @param names names of columns to add
     */
    public void ensureColumnsPresent(String... names)
    {
        boolean opened = false;
        for(String name: names)
        {
            if(getColumn(name) == -1)
            {
                if(!opened)
                {
                    _test._customizeViewsHelper.openCustomizeViewPanel();
                    opened = true;
                }
                _test._customizeViewsHelper.addCustomizeViewColumn(name);
            }
        }
        if(opened)
            _test._customizeViewsHelper.applyCustomView();
    }

    /**
     * returns index of the row of the first appearance of the specified data, in the specified column
     * @param data
     * @param column
     * @return
     */
    public int getIndexWhereDataAppears(String data, String column)
    {
        List<String> allData = getColumnDataAsText(column);
        return allData.indexOf(data);
    }

    public int getDataRowCount(int div)
    {
        int rows = 0;
        try
        {
            while (getDataAsText(rows, 0) != null)
                rows += div;
        }
        catch (Exception e)
        {
            // Throws an exception, if row is out of bounds.
        }

        if (rows == 1 && "No data to show.".equals(getDataAsText(0, 0)))
            rows = 0;

        return rows;
    }

    public Locator.XPathLocator detailsXpath(int row)
    {
        return Locator.xpath("//table[@id=" + Locator.xq(getHtmlName()) + "]/tbody/tr[" + (row + _headerRows + 1) + "]/td[contains(@class, 'labkey-details')]");
    }

    public Locator.XPathLocator detailsLink(int row)
    {
        Locator.XPathLocator cell = detailsXpath(row);
        return cell.child("a[1]");
    }

    public Locator.XPathLocator updateXpath(int row)
    {
        return Locator.xpath("//table[@id=" + Locator.xq(getHtmlName()) + "]/tbody/tr[" + (row + _headerRows + 1) + "]/td[contains(@class, 'labkey-update')]");
    }

    public Locator.XPathLocator updateLink(int row)
    {
        Locator.XPathLocator cell = updateXpath(row);
        return cell.child("a[1]");
    }

    public Locator.XPathLocator xpath(int row, int col)
    {
        return Locator.xpath("//table[@id=" + Locator.xq(getHtmlName()) + "]/tbody/tr[" + (row+_headerRows+1) + "]/td[" + (col + 1 + (_selectors ? 1 : 0)) + "]");
    }

    public Locator.XPathLocator link(int row, int col)
    {
        Locator.XPathLocator cell = xpath(row, col);
        return cell.child("a[1]");
    }

    public void clickLink(int row, int col)
    {
        _test.clickAndWait(link(row, col));
    }

    public void clickLink(int row, String columnName)
    {
        int col = getColumn(columnName);
        if (col == -1)
            Assert.fail("Couldn't find column '" + columnName + "'");
        clickLink(row, col);
    }

    public int getColumn(String name)
    {
        name = name.replaceAll(" ", "");
        Integer colIndex = _mapColumns.get(name);
        if (colIndex != null)
            return colIndex.intValue();
        
        try
        {
            for (int col = 0; col < _columnCount; col++)
            {
                String header = getDataAsText(-(_headerRows/2), col);
                if( header != null )
                {
                    String headerName = header.split("\n")[0];
                    headerName = headerName.replaceAll(" ", "");
                    if (!StringUtils.isEmpty(headerName))
                        _mapColumns.put(headerName, col);
                    if (headerName.equals(name))
                    {
                        return col;
                    }
                }
            }
        }
        catch (Exception e)
        {            
            // _test.log("Failed to get column named " + name);
        }

        _test.log("Column '" + name + "' not found");
        return -1;
    }

    public List<String> getColumnDataAsText(int col)
    {
        int rowCount = getDataRowCount();
        List<String> columnText = new ArrayList<String>();

        if (col >= 0)
        {
            for (int row = 0; row < rowCount; row++)
            {
                columnText.add(getDataAsText(row, col));
            }
        }

        return columnText;

    }
    public List<String> getColumnDataAsText(String name)
    {
        int col = getColumn(name);
        return  getColumnDataAsText(col);
    }

    /** Find the row number for the given primary key. */
    public int getRow(String pk)
    {
        Assert.assertTrue("Need the selector checkbox's value to find the row with the given pk", _selectors);

        Integer cached = _mapRows.get(pk);
        if (cached != null)
            return cached.intValue();

        int row = 0;
        try
        {
            while (true)
            {
                String value = _test.getAttribute(Locator.xpath("//table[@id=" + Locator.xq(getHtmlName()) +"]//tr[" + (row+5) + "]//input[@name='.select']/"), "value");
                _mapRows.put(value, row);
                if (value.equals(pk))
                    return row;
                row += 1;
            }
        }
        catch (Exception e)
        {
            // Throws an exception, if row is out of bounds.
        }

        return -1;
    }

    private boolean hasNoDataToShow()
    {
        return "No data to show.".equals(_getDataAsText(_headerRows, 0));
    }

    public String getDataAsText(int row, int column)
    {
        return _getDataAsText(row + _headerRows, column + (_selectors ? 1 : 0));
    }

    // Doesn't adjust for header rows or selector columns.
    private String _getDataAsText(int row, int column)
    {
        String ret = null;

        try
        {
            ret = _test.getTableCellText(getHtmlName(), row, column);
        }
        catch(Exception ignore) {}

        return ret;
    }

    public String getDataAsText(int row, String columnName)
    {
        int col = getColumn(columnName);
        if (col == -1)
            return null;
        return getDataAsText(row, col);
    }

    public String getDataAsText(String pk, String columnName)
    {
        int row = getRow(pk);
        if (row == -1)
            return null;
        int col = getColumn(columnName);
        if (col == -1)
            return null;
        return getDataAsText(row, col);
    }

    public String getDetailsHref(int row)
    {
        Locator l = detailsLink(row);
        return _test.getAttribute(l, "href");
    }

    public String getUpdateHref(int row)
    {
        Locator l = updateLink(row);
        return _test.getAttribute(l, "href");
    }

    public String getHref(int row, int column)
    {
        // headerRows and selector offsets are applied in link() locator
        return _getHref(row, column);
    }

    private String _getHref(int row, int column)
    {
        Locator l = link(row, column);
        return _test.getAttribute(l, "href");
    }

    public String getHref(int row, String columnName)
    {
        int col = getColumn(columnName);
        if (col == -1)
            return null;
        return getHref(row, col);
    }

    public String getHref(String pk, String columnName)
    {
        int row = getRow(pk);
        if (row == -1)
            return null;
        int col = getColumn(columnName);
        if (col == -1)
            return null;
        return getHref(row, col);
    }

    public void setSort(String columnName, SortDirection direction)
    {
        _test.setSort(_tableName, columnName, direction);
    }

    public void setFilter(String columnName, String filterType, String filter)
    {
        setFilter(columnName, filterType, filter, BaseSeleniumWebTest.WAIT_FOR_PAGE);
    }

    public void setFilter(String columnName, String filterType, String filter, int waitMillis)
    {
        _test.setFilter(_tableName, columnName, filterType, filter, waitMillis);
    }

    public void clearFilter(String columnName)
    {
        _test.clearFilter(_tableName, columnName);
    }

    public void clearAllFilters(String columnName)
    {
        _test.clearAllFilters(_tableName, columnName);
    }

    public void checkAllOnPage()
    {
        _test.checkAllOnPage(_tableName);
    }

    public void uncheckAllOnPage()
    {
        _test.uncheckAllOnPage(_tableName);
    }

    public void checkAll()
    {
        throw new UnsupportedOperationException("Not Yet Implemented");
    }

    public void uncheckAll()
    {
        throw new UnsupportedOperationException("Not Yet Implemented");
    }

    public void checkCheckbox(String value)
    {
        _test.checkDataRegionCheckbox(_tableName, value);
    }

    public void checkCheckbox(int index)
    {
        _test.checkDataRegionCheckbox(_tableName, index);
    }

    public void uncheckCheckbox(int index)
    {
        _test.uncheckDataRegionCheckbox(_tableName, index);
    }

    public void pageFirst()
    {
        _test.dataRegionPageFirst(_tableName);
    }

    public void pageLast()
    {
        _test.dataRegionPageLast(_tableName);
    }

    public void pageNext()
    {
        _test.dataRegionPageNext(_tableName);
    }

    public void pagePrev()
    {
        _test.dataRegionPagePrev(_tableName);
    }

    public void showAll()
    {
        _test.clickLinkWithText("Show All");
    }

    public void setPageSize(int size)
    {
        _test.clickLinkWithText(size + " per page");
    }

    /**
     * Set the current offset by manipulating the url rather than using the pagination buttons.
     * @param offset
     */
    public void setOffset(int offset)
    {
        String url = replaceParameter(_tableName + ".offset", String.valueOf(offset));
        _test.beginAt(url);
    }

    /**
     * Set the page size by manipulating the url rather than using the "XXX per page" menu items.
     * @param size new page size
     */
    public void setMaxRows(int size)
    {
        String url = replaceParameter(_tableName + ".maxRows", String.valueOf(size));
        _test.beginAt(url);
    }

    private String replaceParameter(String param, String newValue)
    {
        URL url = _test.getURL();
        String file = url.getFile();
        String encodedParam = EscapeUtil.encode(param);
        file = file.replaceAll("&" + Pattern.quote(encodedParam) + "=\\p{Alnum}+?", "");
        if (newValue != null)
            file += "&" + encodedParam + "=" + EscapeUtil.encode(newValue);

        try
        {
            url = new URL(url.getProtocol(), url.getHost(), url.getPort(), file);
        }
        catch (MalformedURLException mue)
        {
            throw new RuntimeException(mue);
        }
        return url.getFile();
    }
}
