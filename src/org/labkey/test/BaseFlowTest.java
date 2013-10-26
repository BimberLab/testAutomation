/*
 * Copyright (c) 2007-2013 LabKey Corporation
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

package org.labkey.test;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.labkey.test.util.DataRegionTable;
import org.labkey.test.util.LogMethod;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

abstract public class BaseFlowTest extends BaseSeleniumWebTest
{
    protected static final String PROJECT_NAME = "Flow Verify Project";
    protected static final String PIPELINE_PATH = "/sampledata/flow";

    public String getAssociatedModuleDirectory()
    {
        return "server/modules/flow";
    }

    //need not fill all three, but must be the same length.  If you wish to skip a field, set it to an empty string,
    // or the default in the case of op
    public void setFlowFilter(String[] fields, String[] ops, String[] values)
    {
        goToFlowDashboard();
        clickAndWait(Locator.linkWithText("Other settings"));
        clickAndWait(Locator.linkWithText("Edit FCS Analysis Filter"));

        for(int i=0; i<fields.length; i++)
        {
            selectOptionByValue(Locator.xpath("//select[@name='ff_field']").index(i),  fields[i]);
            selectOptionByValue(Locator.xpath("//select[@name='ff_op']").index(i), ops[i]);
            setFormElement(Locator.xpath("//input[@name='ff_value']").index(i), values[i]);
        }
        submit();
    }

    @Override
    protected String getProjectName()
    {
        return PROJECT_NAME;
    }

    protected String getFolderName()
    {
        return getClass().getSimpleName();
    }

    private String _containerPath = null;

    protected String getContainerPath()
    {
        if (_containerPath == null)
            _containerPath = "/" + getProjectName() + "/" + getFolderName();
        return _containerPath;
    }


    protected void setFlowPipelineRoot(String rootPath)
    {
        setPipelineRoot(rootPath);
    }

    protected File getPipelineWorkDirectory()
    {
        return new File(getLabKeyRoot() + "/sampledata/flow/work");
    }

    protected void deletePipelineWorkDirectory()
    {
        File dir = getPipelineWorkDirectory();
        if (dir.exists())
        {
            try
            {
                log("Deleting pipeline work directory: " + dir);
                FileUtils.deleteDirectory(dir);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    protected void waitForPipeline(String containerPath)
    {
        pushLocation();
        beginAt("/Flow" + containerPath + "/showJobs.view");

        long startTime = System.currentTimeMillis();
        do
        {
            log("Waiting for flow pipeline jobs to complete...");
            sleep(1500);
            refresh();
        }
        while (!isTextPresent("There are no running or pending flow jobs") && System.currentTimeMillis() - startTime < 300000);

        popLocation(longWaitForPage);
    }

    protected void doCleanup(boolean afterTest) throws TestTimeoutException
    {
        deleteProject(getProjectName(), afterTest);
        try
        {
            beginAt("/admin/begin.view");
            clickAndWait(Locator.linkWithText("flow cytometry"));
            setFormElement("workingDirectory", "");
            clickButton("update");
        }
        catch (Throwable ignored) {}
        deletePipelineWorkDirectory();
    }

    @Override
    protected void doTestSteps() throws Exception
    {
        init();
        _doTestSteps();
        after();
    }

    protected abstract void _doTestSteps() throws Exception;

    protected boolean requiresNormalization()
    {
        return false;
    }

    protected void init()
    {
        beginAt("/admin/begin.view");
        clickAndWait(Locator.linkWithText("flow cytometry"));
        deletePipelineWorkDirectory();
        setFormElement("workingDirectory", getPipelineWorkDirectory().toString());
        clickButton("update");
        assertTextPresent("Path does not exist");
        getPipelineWorkDirectory().mkdir();
        setFormElement("workingDirectory", getPipelineWorkDirectory().toString());

        boolean normalizationEnabled = requiresNormalization();
        if (normalizationEnabled)
            checkCheckbox(Locator.id("normalizationEnabled"));
        else
            uncheckCheckbox(Locator.id("normalizationEnabled"));

        clickButton("update");
        assertTextNotPresent("Path does not exist");
        if (normalizationEnabled)
        {
            assertTextNotPresent("The R script engine is not available.");
            assertTextNotPresent("Please install the flowWorkspace R library");
        }

        _containerHelper.createProject(getProjectName(), null);
        createSubfolder(getProjectName(), getProjectName(), getFolderName(), "Flow", null);

        setFlowPipelineRoot(getLabKeyRoot() + PIPELINE_PATH);
    }

    protected void after() throws Exception
    {
        if (!TestProperties.isTestCleanupSkipped())
            deleteAllRuns();
    }

    //Issue 12597: Need to delete exp.data objects when deleting a flow run
    protected void deleteAllRuns() throws Exception
    {
        if (!isLinkPresentWithText(getProjectName()))
            goToHome();
        if (!isLinkPresentWithText(getProjectName()))
            return;

        clickProject(getProjectName());
        if (!isLinkPresentWithText(getFolderName()))
            return;

        clickFolder(getFolderName());

        beginAt("/query/" + getProjectName() + "/" + getFolderName() + "/executeQuery.view?schemaName=exp&query.queryName=Runs");
        DataRegionTable table = new DataRegionTable("query", this);
        if (table.getDataRowCount() > 0)
        {
            // Delete all runs
            table.checkAllOnPage();
            selenium.chooseOkOnNextConfirmation();
            clickButton("Delete", 0);
            Assert.assertTrue(selenium.getConfirmation().contains("Are you sure you want to delete the selected row"));
            waitForPageToLoad();
            Assert.assertEquals("Expected all experiment Runs to be deleted", 0, table.getDataRowCount());

            // Check all DataInputs were deleted
            beginAt("/query/" + getProjectName() + "/" + getFolderName() + "/executeQuery.view?schemaName=exp&query.queryName=DataInputs");
            Assert.assertEquals("Expected all experiment DataInputs to be deleted", 0, table.getDataRowCount());

            // Check all Datas were deleted except for flow analysis scripts (FlowDataType.Script)
            beginAt("/query/" + getProjectName() + "/" + getFolderName() + "/executeQuery.view?schemaName=exp&query.queryName=Datas&query.LSID~doesnotcontain=Flow-AnalysisScript");
            Assert.assertEquals("Expected all experiment Datas to be deleted", 0, table.getDataRowCount());
        }
    }

    // if we aren't already on the Flow Dashboard, try to get there.
    protected void goToFlowDashboard()
    {
        String title = selenium.getTitle();
        if (!title.startsWith("Flow Dashboard: "))
        {
            // All flow pages have a link back to the Flow Dashboard
            if (isLinkPresentWithText("Flow Dashboard"))
            {
                clickAndWait(Locator.linkWithText("Flow Dashboard"));
            }
            else
            {
                // If we are elsewhere, get back to the current test folder
                clickProject(getProjectName());
                clickFolder(getFolderName());
            }
        }
    }

    protected void createQuery(String container, String name, String sql, String xml, boolean inheritable)
    {
        super.createQuery(container, name, "flow", sql, xml, inheritable);
        goToSchemaBrowser();
    }

    protected void uploadSampleDescriptions(String sampleFilePath, String [] idCols, String[] keywordCols)
    {
        log("** Uploading sample set");
        goToFlowDashboard();
        clickAndWait(Locator.linkWithText("Upload Sample Descriptions"));
        setFormElement("data", getFileContents(sampleFilePath));
        for (int i = 0; i < idCols.length; i++)
            selectOptionByText("idColumn" + (i+1), idCols[i]);
        submit();

        log("** Join sample set with FCSFile keywords");
        clickAndWait(Locator.linkWithText("Flow Dashboard"));
        clickAndWait(Locator.linkWithText("Define sample description join fields"));
        for (int i = 0; i < idCols.length; i++)
            selectOptionByText(Locator.name("ff_samplePropertyURI", i), idCols[i]);
        for (int i = 0; i < keywordCols.length; i++)
            selectOptionByText(Locator.name("ff_dataField", i), keywordCols[i]);
        submit();
    }

    protected void setProtocolMetadata(String specimenIdColumn, String participantColumn, String dateColumn, String visitColumn, boolean setBackground)
    {
        log("** Specify ICS metadata");
        goToFlowDashboard();
        clickAndWait(Locator.linkWithText("Other settings"));
        clickAndWait(Locator.linkWithText("Edit ICS Metadata"));

        // specify PTID and Visit/Date columns
        if (specimenIdColumn != null)
            selectOptionByText("ff_specimenIdColumn", specimenIdColumn);
        if (participantColumn != null)
            selectOptionByText("ff_participantColumn", participantColumn);
        if (dateColumn != null)
            selectOptionByText("ff_dateColumn", dateColumn);
        if (visitColumn != null)
            selectOptionByText("ff_visitColumn", visitColumn);

        if (setBackground)
        {
            // specify forground-background match columns
            assertFormElementEquals(Locator.name("ff_matchColumn", 0), "Run");
            selectOptionByText(Locator.name("ff_matchColumn", 1), "Sample Sample Order");

            // specify background values
            selectOptionByText(Locator.name("ff_backgroundFilterField", 0), "Sample Stim");
            assertFormElementEquals(Locator.name("ff_backgroundFilterOp", 0), "eq");
            setFormElement(Locator.name("ff_backgroundFilterValue", 0), "Neg Cont");
        }

        submit();
    }

    protected void importExternalAnalysis(String containerPath, String analysisZipPath)
    {
        goToFlowDashboard();
        clickAndWait(Locator.linkContainingText("FCS files to be imported"));
        _fileBrowserHelper.importFile(analysisZipPath, "Import External Analysis");

        importAnalysis_selectFCSFiles(containerPath, SelectFCSFileOption.None, null);
        importAnalysis_reviewSamples(containerPath, false, null, null);
        String analysisFolder = new File(analysisZipPath).getName();
        importAnalysis_analysisFolder(containerPath, analysisFolder, false);
        // UNDONE: use importAnalysis_confim step
        clickButton("Finish");

        waitForPipeline(containerPath);
    }

    protected void importAnalysis(String containerPath, String workspacePath, SelectFCSFileOption selectFCSFilesOption, List<String> keywordDirs, String analysisName, boolean existingAnalysisFolder, boolean viaPipeline)
    {
        ImportAnalysisOptions options = new ImportAnalysisOptions(containerPath, workspacePath, selectFCSFilesOption, keywordDirs, analysisName, existingAnalysisFolder, viaPipeline);
        importAnalysis(options);
    }

    protected void importAnalysis(ImportAnalysisOptions options)
    {
        if (options.isViaPipeline())
        {
            importAnalysis_viaPipeline(options.getWorkspacePath());
        }
        else
        {
            importAnalysis_begin(options.getContainerPath());
            importAnalysis_uploadWorkspace(options.getContainerPath(), options.getWorkspacePath());
        }
        importAnalysis_selectFCSFiles(options.getContainerPath(), options.getSelectFCSFilesOption(), options.getKeywordDirs());
        assertFormElementEquals(Locator.name("selectFCSFilesOption"), options.getSelectFCSFilesOption().name());

        boolean resolving = options.getSelectFCSFilesOption() == SelectFCSFileOption.Previous;
        importAnalysis_reviewSamples(options.getContainerPath(), resolving, options.getSelectedGroupNames(), options.getSelectedSampleIds());

        // R Analysis engine can only be selected when using Mac FlowJo workspaces
        if (!options.getWorkspacePath().endsWith(".wsp"))
            importAnalysis_analysisEngine(options.getContainerPath(), options.getAnalysisEngine());

        // Analysis option page only shown when using R normalization.
        if (options.getAnalysisEngine() == AnalysisEngine.R && options.isREngineNormalization())
            importAnalysis_analysisOptions(options.getContainerPath(), options.isREngineNormalization(), options.getREngineNormalizationReference(), options.getREngineNormalizationSubsets(), options.getREngineNormalizationParameters());

        importAnalysis_analysisFolder(options.getContainerPath(), options.getAnalysisName(), options.isExistingAnalysisFolder());

        importAnalysis_confirm(
                options.getContainerPath(),
                options.getWorkspacePath(),
                options.getSelectFCSFilesOption(),
                options.getKeywordDirs(),
                options.getSelectedGroupNames(),
                options.getSelectedSampleIds(),
                options.getAnalysisEngine(),
                options.isREngineNormalization(),
                options.getREngineNormalizationReference(),
                options.getREngineNormalizationSubsets(),
                options.getREngineNormalizationParameters(),
                options.getAnalysisName(),
                options.isExistingAnalysisFolder());

        importAnalysis_checkErrors(options.getExpectedErrors());
    }

    @LogMethod
    protected void importAnalysis_viaPipeline(String workspacePath)
    {
        log("browse pipeline to begin import analysis wizard");
        goToFlowDashboard();
        clickAndWait(Locator.linkContainingText("FCS files to be imported"));
        _fileBrowserHelper.importFile(workspacePath, "Import FlowJo Workspace");
    }

    @LogMethod
    protected void importAnalysis_begin(String containerPath)
    {
        log("begin import analysis wizard");
        goToFlowDashboard();
        clickAndWait(Locator.linkWithText("Import FlowJo Workspace Analysis"));
        assertTitleEquals("Import Analysis: Select Analysis: " + containerPath);
    }

    @LogMethod
    protected void importAnalysis_uploadWorkspace(String containerPath, String workspacePath)
    {
        assertTitleEquals("Import Analysis: Select Analysis: " + containerPath);
        _fileBrowserHelper.selectFileBrowserItem(workspacePath);
        clickButton("Next");
    }

    @LogMethod
    protected void importAnalysis_selectFCSFiles(String containerPath, final SelectFCSFileOption selectFCSFilesOption, List<String> keywordDirs)
    {
        waitForExtReady();
        if (isChecked(Locator.id(SelectFCSFileOption.Browse.name())))
            _fileBrowserHelper.waitForFileGridReady();

        assertTitleEquals("Import Analysis: Select FCS Files: " + containerPath);
        switch (selectFCSFilesOption)
        {
            case None:
                clickRadioButtonById("None");
                break;

            case Included:
                Assert.fail("Not yet implemented");
                //clickRadioButtonById("Included");
                break;

            case Previous:
                clickRadioButtonById("Previous");
                break;

            case Browse:
                clickRadioButtonById("Browse");
                _fileBrowserHelper.waitForFileGridReady();
                // UNDONE: Currently, only one file path supported
                _fileBrowserHelper.selectFileBrowserItem(keywordDirs.get(0));
                break;

            default:
                Assert.fail();
        }
        waitFor(new Checker() {
            @Override
            public boolean check()
            {
                boolean checked = isChecked(Locator.id(selectFCSFilesOption.name()));
                log("selectedFCSFilesOption is checked: " + checked);
                return checked;
            }
        }, "selectFCSFilesOption", 2000);
        clickButton("Next");
    }

    @LogMethod
    protected void importAnalysis_reviewSamples(String containerPath, boolean resolving, List<String> selectedGroupNames, List<String> selectedSampleIds)
    {
        assertTitleEquals("Import Analysis: Review Samples: " + containerPath);

        if (resolving)
        {
            // UNDONE: Test resolving files
            assertTextPresent("Matched");
        }

        if (selectedGroupNames != null && selectedGroupNames.size() > 0)
        {
            setFormElement(Locator.id("importGroupNames"), StringUtils.join(selectedGroupNames, ","));
        }
        else if (selectedSampleIds != null && selectedSampleIds.size() > 0)
        {
            // UNDONE: Select individual rows for import
        }

        clickButton("Next");
    }

    @LogMethod
    protected void importAnalysis_analysisEngine(String containerPath, AnalysisEngine engine)
    {
        assertTitleEquals("Import Analysis: Analysis Engine: " + containerPath);
        waitForElement(Locator.id(engine.name()), defaultWaitForPage);
        clickRadioButtonById(engine.name());
        clickButton("Next");
    }

    @LogMethod
    protected void importAnalysis_analysisOptions(String containerPath, boolean rEngineNormalization, String rEngineNormalizationReference, List<String> rEngineNormalizationSubsets, List<String> rEngineNormalizationParameters)
    {
        assertTitleEquals("Import Analysis: Analysis Options: " + containerPath);

        // R normalization options only present if rEngine as selected
        if (isElementPresent(Locator.id("rEngineNormalization")))
        {
            log("Setting normalization options");
            assertTextNotPresent("Normalization is current disabled");
            if (rEngineNormalization)
            {
                checkCheckbox("rEngineNormalization");
                if (rEngineNormalizationReference != null)
                {
                    selectOptionByText(Locator.id("rEngineNormalizationReference"), rEngineNormalizationReference);
                    String formValue = getFormElement(Locator.id("rEngineNormalizationReference"));
                    Assert.assertEquals(rEngineNormalizationReference, getText(Locator.xpath("id('rEngineNormalizationReference')/option[@value='" + formValue + "']")));
                }

                if (rEngineNormalizationSubsets != null)
                    setFormElement("rEngineNormalizationSubsets", StringUtils.join(rEngineNormalizationSubsets, ImportAnalysisOptions.PARAMETER_SEPARATOR));

                if (rEngineNormalizationParameters != null)
                    setFormElement("rEngineNormalizationParameters", StringUtils.join(rEngineNormalizationParameters, ImportAnalysisOptions.PARAMETER_SEPARATOR));
            }
            else
            {
                uncheckCheckbox("rEngineNormalization");
            }
        }
        else
        {
            if (rEngineNormalization)
                Assert.fail("Expected to find R normalization options");
            log("Not setting normalization options");
        }
        clickButton("Next");
    }

    @LogMethod
    protected void importAnalysis_analysisFolder(String containerPath, String analysisName, boolean existing)
    {
        assertTitleEquals("Import Analysis: Analysis Folder: " + containerPath);
        if (existing)
        {
            selectOptionByText("existingAnalysisId", analysisName);
        }
        else
        {
            setFormElement("newAnalysisName", analysisName);
        }
        clickButton("Next");
    }

    protected void importAnalysis_confirm(String containerPath, String workspacePath,
                                          SelectFCSFileOption selectFCSFilesOption, List<String> keywordDirs,
                                          AnalysisEngine analysisEngine,
                                          String analysisFolder, boolean existingAnalysisFolder)
    {
        importAnalysis_confirm(containerPath, workspacePath,
                selectFCSFilesOption, keywordDirs,
                Arrays.asList("All Samples"),
                null,
                analysisEngine,
                false, null, null, null,
                analysisFolder, existingAnalysisFolder);
    }

    @LogMethod
    protected void importAnalysis_confirm(String containerPath, String workspacePath,
                                          SelectFCSFileOption selectFCSFilesOption,
                                          List<String> keywordDirs,
                                          List<String> selectedGroupNames,
                                          List<String> selectedSampleIds,
                                          AnalysisEngine analysisEngine,
                                          boolean rEngineNormalization,
                                          String rEngineNormalizationReference,
                                          List<String> rEngineNormalizationSubsets,
                                          List<String> rEngineNormalizationParameters,
                                          String analysisFolder,
                                          boolean existingAnalysisFolder)
    {
        assertTitleEquals("Import Analysis: Confirm: " + containerPath);

        assertTextPresent("Workspace: " + workspacePath);

        if (analysisEngine.equals("FlowJoWorkspace"))
            assertTextPresent("Analysis Engine: No analysis engine selected");
        else if (analysisEngine.equals("R"))
            assertTextPresent("Analysis Engine: External R analysis engine");

        if (rEngineNormalization)
        {
            assertTextPresent("Reference Sample: " + rEngineNormalizationReference);
            assertTextPresent("Normalize Subsets: " + (rEngineNormalizationSubsets == null ? "All subsets" : StringUtils.join(rEngineNormalizationSubsets, ", ")));
            assertTextPresent("Normalize Parameters: " + (rEngineNormalizationParameters == null ? "All parameters" : StringUtils.join(rEngineNormalizationParameters, ", ")));
        }

        if (existingAnalysisFolder)
            assertTextPresent("Existing Analysis Folder: " + analysisFolder);
        else
            assertTextPresent("New Analysis Folder: " + analysisFolder);

        // XXX: assert fcsPath is present: need to normalize windows path backslashes
        if (selectFCSFilesOption == SelectFCSFileOption.Browse && keywordDirs == null)
            assertTextPresent("FCS File Path: none set");

        clickButton("Finish");
        waitForPipeline(containerPath);
        log("finished import analysis wizard");
    }

    protected void importAnalysis_checkErrors(List<String> expectedErrors)
    {
        log("Checking for errors after importing");
        pushLocation();
        if (expectedErrors == null || expectedErrors.isEmpty())
        {
            checkErrors();
        }
        else
        {
            goToFlowDashboard();
            clickAndWait(Locator.linkContainingText("Show Jobs"));
            clickAndWait(Locator.linkWithText("ERROR"));

            for (String errorText : expectedErrors)
                assertTextPresent(errorText);

            int errorCount = countText("ERROR");
            checkExpectedErrors(errorCount);
        }
        popLocation();
    }

    protected enum SelectFCSFileOption { None, Included, Previous, Browse }
    protected enum AnalysisEngine { FlowJoWorkspace, R }

    protected static class ImportAnalysisOptions
    {
        public static final String PARAMETER_SEPARATOR = "\ufe50";

        private final String _containerPath;
        private final String _workspacePath;
        private SelectFCSFileOption _selectFCSFilesOption;
        private final List<String> _keywordDirs;
        private final List<String> _selectedGroupNames;
        private final List<String> _selectedSampleIds;
        private final AnalysisEngine _analysisEngine;
        private final boolean _rEngineNormalization;
        private final String _rEngineNormalizationReference;
        private final List<String> _rEngineNormalizationSubsets;
        private final List<String> _rEngineNormalizationParameters;
        private final String _analysisName;
        private final boolean _existingAnalysisFolder;
        private final boolean _viaPipeline;
        private final List<String> _expectedErrors;


        public ImportAnalysisOptions(
                String containerPath,
                String workspacePath,
                SelectFCSFileOption selectFCSFilesOption,
                List<String> keywordDirs,
                String analysisName,
                boolean existingAnalysisFolder,
                boolean viaPipeline)
        {
            _containerPath = containerPath;
            _workspacePath = workspacePath;
            _selectFCSFilesOption = selectFCSFilesOption;
            _keywordDirs = keywordDirs;
            _selectedGroupNames = Arrays.asList("All Samples");
            _selectedSampleIds = null;
            _analysisEngine = AnalysisEngine.FlowJoWorkspace;
            _rEngineNormalization = false;
            _rEngineNormalizationReference = null;
            _rEngineNormalizationSubsets = null;
            _rEngineNormalizationParameters = null;
            _analysisName = analysisName;
            _existingAnalysisFolder = existingAnalysisFolder;
            _viaPipeline = viaPipeline;
            _expectedErrors = new ArrayList<>();
        }

        public ImportAnalysisOptions(
                String containerPath,
                String workspacePath,
                SelectFCSFileOption selectFCSFilesOption,
                List<String> keywordDirs,
                List<String> selectGroupNames,
                List<String> selectSampleIds,
                AnalysisEngine analysisEngine,
                boolean rEngineNormalization,
                String rEngineNormalizationReference,
                List<String> rEngineNormalizationSubsets,
                List<String> rEngineNormalizationParameters,
                String analysisName,
                boolean existingAnalysisFolder,
                boolean viaPipeline,
                List<String> expectedErrors)
        {
            _containerPath = containerPath;
            _workspacePath = workspacePath;
            _selectFCSFilesOption = selectFCSFilesOption;
            _keywordDirs = keywordDirs;
            _selectedGroupNames = selectGroupNames;
            _selectedSampleIds = selectSampleIds;
            _analysisEngine = analysisEngine;
            _rEngineNormalization = rEngineNormalization;
            _rEngineNormalizationReference = rEngineNormalizationReference;
            _rEngineNormalizationSubsets = rEngineNormalizationSubsets;
            _rEngineNormalizationParameters = rEngineNormalizationParameters;
            _analysisName = analysisName;
            _existingAnalysisFolder = existingAnalysisFolder;
            _viaPipeline = viaPipeline;
            _expectedErrors = expectedErrors;
        }

        public String getContainerPath()
        {
            return _containerPath;
        }

        public String getWorkspacePath()
        {
            return _workspacePath;
        }

        public SelectFCSFileOption getSelectFCSFilesOption()
        {
            return _selectFCSFilesOption;
        }
        public List<String> getKeywordDirs()
        {
            return _keywordDirs;
        }

        public List<String> getSelectedGroupNames()
        {
            return _selectedGroupNames;
        }

        public List<String> getSelectedSampleIds()
        {
            return _selectedSampleIds;
        }

        public AnalysisEngine getAnalysisEngine()
        {
            return _analysisEngine;
        }

        public boolean isREngineNormalization()
        {
            return _rEngineNormalization;
        }

        public String getREngineNormalizationReference()
        {
            return _rEngineNormalizationReference;
        }

        public List<String> getREngineNormalizationSubsets()
        {
            return _rEngineNormalizationSubsets;
        }

        public List<String> getREngineNormalizationParameters()
        {
            return _rEngineNormalizationParameters;
        }

        public String getAnalysisName()
        {
            return _analysisName;
        }

        public boolean isExistingAnalysisFolder()
        {
            return _existingAnalysisFolder;
        }

        public boolean isViaPipeline()
        {
            return _viaPipeline;
        }

        public List<String> getExpectedErrors()
        {
            return _expectedErrors;
        }

    }
}
