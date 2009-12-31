/*
 * Copyright (c) 2007-2009 LabKey Corporation
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
package org.labkey.test.pipeline;

import junit.framework.Assert;
import org.apache.commons.lang.StringUtils;
import org.labkey.test.BaseSeleniumWebTest;
import org.labkey.test.Locator;
import org.labkey.test.util.EmailRecordTable;
import org.labkey.test.util.ExperimentRunTable;
import org.labkey.test.util.PasswordUtil;
import org.labkey.test.util.PipelineStatusTable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * MS2TestParams class
* <p/>
* Created: Aug 15, 2007
*
* @author bmaclean
*/
abstract public class AbstractPipelineTestParams implements PipelineTestParams
{
    protected PipelineWebTestBase _test;
    private String _dataPath;
    private String _protocolType;
    private String _parametersFile;
    private String _protocolName;
    private String[] _sampleNames;
    private String[] _inputExtensions = new String[0];
    private String[] _outputExtensions = new String[0];
    private String[] _experimentLinks;
    protected PipelineFolder.MailSettings _mailSettings;
    private boolean _expectError;    
    private boolean _valid;

    public AbstractPipelineTestParams(PipelineWebTestBase test, String dataPath,
                                      String protocolType, String protocolName, String... sampleNames)
    {
        _test = test;
        _dataPath = dataPath;
        _sampleNames = sampleNames;
        _protocolType = protocolType;
        _protocolName = protocolName;
        _valid = true;
    }

    public PipelineWebTestBase getTest()
    {
        return _test;
    }

    public String getDataPath()
    {
        return _dataPath;
    }

    public String getDataDirName()
    {
        String[] parts = StringUtils.split(_dataPath, '/');
        return parts[parts.length - 1]; 
    }

    public String getProtocolName()
    {
        return _protocolName;
    }

    public String getProtocolType()
    {
        return _protocolType;
    }

    public String[] getSampleNames()
    {
        return _sampleNames;
    }

    public String getParametersFile()
    {
        return _parametersFile != null ? _parametersFile : _protocolType + ".xml";
    }

    public void setParametersFile(String parametersFile)
    {
        _parametersFile = parametersFile;
    }

    public String[] getInputExtensions()
    {
        return _inputExtensions;
    }

    public void setInputExtensions(String... inputExtensions)
    {
        _inputExtensions = inputExtensions;
    }

    public String[] getOutputExtensions()
    {
        return _outputExtensions;
    }

    public void setOutputExtensions(String... outputExtensions)
    {
        _outputExtensions = outputExtensions;
    }

    public String getRunKey()
    {
        return _dataPath + " (" + _protocolType + "/" + _protocolName + ")";
    }

    public String getDirStatusDesciption()
    {
        return getDataDirName() + " (" + _protocolName + ")";
    }

    public String[] getExperimentLinks()
    {
        if (_experimentLinks == null)
        {
            String[] dirs = _dataPath.split("/");
            String dataDirName = dirs[dirs.length - 1];

            if (_sampleNames.length == 0)
                _experimentLinks = new String[] { dataDirName + " (" + _protocolName + ")" };
            else
            {
                ArrayList<String> listLinks = new ArrayList<String>();
                for (String name : _sampleNames)
                    listLinks.add(dataDirName + '/' + name + " (" + _protocolName + ")");
                _experimentLinks = listLinks.toArray(new String[listLinks.size()]);
            }
        }
        return _experimentLinks;
    }

    public void setExperimentLinks(String[] experimentLinks)
    {
        this._experimentLinks = experimentLinks;
    }

    public PipelineFolder.MailSettings getMailSettings()
    {
        return _mailSettings;
    }

    public void setMailSettings(PipelineFolder.MailSettings mailSettings)
    {
        _mailSettings = mailSettings;
    }

    public boolean isExpectError()
    {
        return _expectError;
    }

    public void setExpectError(boolean expectError)
    {
        _expectError = expectError;
    }

    public void validate()
    {
        if (_mailSettings != null)
        {
            if (_expectError)
                validateEmailError();
            else
                validateEmailSuccess();
        }
        else
        {
            // Default fails to allow manual analysis of the run.
            // Override to do actual automated validation of the resulting
            // MS2 run data.

            validateTrue("No automated validation", false);
        }
    }

    public void validateTrue(String message, boolean condition)
    {
        if (!condition)
        {
            _test.log("INVALID: " + message);
            _valid = false;
        }
    }

    public boolean isValid()
    {
        return _valid;
    }

    public void verifyClean(File rootDir)
    {
        File analysisDir = new File(rootDir, getDataPath() + File.separatorChar + getProtocolType());
        if (analysisDir.exists())
            BaseSeleniumWebTest.fail("Pipeline files were not cleaned up; "+ analysisDir.toString() + " directory still exists");
    }

    public void clean(File rootDir) throws IOException
    {
        delete(new File(rootDir, getDataPath() + File.separatorChar + getProtocolType()));
    }

    protected void delete(File file) throws IOException
    {
        if (file.isDirectory())
        {
            for (File child : file.listFiles())
            {
                delete(child);
            }
        }
        System.out.println("Deleting " + file.getPath() + "\n");
        file.delete();
    }

    public void startProcessing()
    {
        _test.log("Start analysis of " + getDataPath());
        _test.clickNavButton("Process and Import Data");
        String[] dirs = getDataPath().split("/");
        for (String dir : dirs)
            _test.waitAndClick(Locator.fileTreeByName(dir));

        clickActionButton();

        int wait = BaseSeleniumWebTest.WAIT_FOR_GWT;
        _test.log("Choose existing protocol " + getProtocolName());
        _test.waitForElement(Locator.xpath("//select[@name='protocol']/option[.='" + getProtocolName() + "']" ),
                wait * 12);
        _test.selectOptionByText("protocol", getProtocolName());
        _test.sleep(wait);

        _test.log("Start data processing");
        clickSubmitButton();
        _test.sleep(wait);
    }

    protected abstract void clickSubmitButton();

    public void remove()
    {
        ExperimentRunTable tableExp = getExperimentRunTable();

        for (String name : getExperimentLinks())
        {
            _test.log("Removing " + name);

            _test.pushLocation();
            tableExp.clickGraphLink(name);
            String id = _test.getURL().getQuery();
            id = id.substring(id.indexOf('=') + 1);
            _test.popLocation();

            _test.checkCheckbox(".select", id);
            _test.clickNavButton("Delete");
            _test.clickNavButton("Confirm Delete");
        }
    }

    private ExperimentRunTable getExperimentRunTable()
    {
        return new ExperimentRunTable(getExperimentRunTableName(), _test, false);
    }
    
    private void validateExperiment()
    {
        _test.clickNavButton("Data");
        ExperimentGraph graph = new ExperimentGraph(_test);
        graph.validate(this);
    }

    public void validateEmailSuccess()
    {
        Assert.assertNotNull("Email validation requires mail settings", _mailSettings);

        validateEmail("COMPLETE", getDirStatusDesciption(), _mailSettings.isNotifyOnSuccess(),
                _mailSettings.getNotifyUsersOnSuccess());

        if (_test.isNavButtonPresent("Data"))
        {
            validateExperiment();
        }
        else
        {
            int split = 1;
            while (_test.isLinkPresentWithText("COMPLETE", split))
            {
                _test.pushLocation();
                _test.clickLinkWithText("COMPLETE", split++);
                validateExperiment();
                _test.popLocation();
            }
        }
    }

    public void validateEmailError()
    {
        Assert.assertNotNull("Email validation requires mail settings", _mailSettings);

        for (String sampleExp : getExperimentLinks())
        {
            _test.pushLocation();
            validateEmail("ERROR", sampleExp, _mailSettings.isNotifyOnError(),
                    _mailSettings.getNotifyUsersOnError());
            _test.popLocation();
        }
    }

    private void validateEmail(String status, String description, boolean notifyOwner, String[] notifyOthers)
    {
        if (!notifyOwner && notifyOthers.length == 0)
            return; // No email expected.

        String userEmail = PasswordUtil.getUsername();
        EmailRecordTable emailTable = new EmailRecordTable(_test);
        EmailRecordTable.EmailMessage message = emailTable.getMessage(description);
        Assert.assertNotNull("No email message found for " + description, message);
        emailTable.clickMessage(message);
        // Reload after the message has been expanded so Selenium can get the text that it's showing
        emailTable = new EmailRecordTable(_test);
        message = emailTable.getMessage(description);
        validateTrue("The test " + description + " does not have expected status " + status,
                message.getBody().indexOf("Status: " + status) != -1);
        // The return address comes from the Look and Feel settings, and may vary from installation to installation, so
        // don't check it for now.
//        validateTrue("Unexpected message sender: " + StringUtils.join(message.getFrom(), ',' + ", expected it to be " + userEmail),
//                message.getFrom().length == 1 && message.getFrom()[0].equals(userEmail));
        List<String> recipients = Arrays.asList(message.getTo());
        if (notifyOwner)
        {
            validateTrue("Message not sent to owner " + userEmail, recipients.contains(userEmail));
        }
        for (String notify : notifyOthers)
        {
            validateTrue("Message not sent to " + notify, recipients.contains(notify));
        }

        // The link in this message uses the IP address.  Avoid clicking it, and
        // possibly changing hostnames.
        for (String line : StringUtils.split(message.getBody(), "\n"))
        {
            if (line.startsWith("http://"))
            {
                _test.beginAt(line.substring(line.indexOf('/', 7)));
                break;
            }
        }

        // Make sure we made it to a status page.
        _test.assertTextPresent("Job Status");
        _test.assertTextPresent(status);
    }

    public void validateEmailEscalation(int sampleIndex)
    {
        Assert.assertNotNull("Email validation requires mail settings", _mailSettings);

        String userEmail = PasswordUtil.getUsername();
        String escalateEmail = _mailSettings.getEscalateUsers()[0];
        String messageText = "I have no idea why this job failed.  Please help me.";

        String sampleExp = getExperimentLinks()[sampleIndex];

        _test.log("Escalate an error");
        EmailRecordTable emailTable = new EmailRecordTable(_test);
        PipelineStatusTable statusTable = new PipelineStatusTable(_test, false, false);
        _test.pushLocation();
        statusTable.clickStatusLink(sampleExp);
        _test.clickNavButton("Escalate Job Failure");
        _test.setFormElement("escalateUser", escalateEmail);
        _test.setFormElement("escalationMessage", messageText);
        // DetailsView adds a useless form.
        //_test.submit();
        _test.clickNavButton("Send");
        _test.popLocation();

        EmailRecordTable.EmailMessage message = emailTable.getMessage(sampleExp);
        Assert.assertNotNull("Escalation message not sent", message);
        Assert.assertTrue("Escalation not sent to " + escalateEmail, escalateEmail.equals(message.getTo()[0]));

        // Not sure why the angle-brackets are added...
        String escalateFrom = '<' + userEmail + '>';
        //Assert.assertTrue("Escalation not sent from " + escalateFrom, message.getFrom()[0].contains(escalateFrom));
    }
}