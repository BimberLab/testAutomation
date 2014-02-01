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

package org.labkey.test.tests;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpStatus;
import org.junit.experimental.categories.Category;
import org.labkey.test.Locator;
import org.labkey.test.TestTimeoutException;
import org.labkey.test.WebTestHelper;
import org.labkey.test.categories.DailyA;
import org.labkey.test.util.DataRegionTable;
import org.labkey.test.util.LabKeyExpectedConditions;
import org.labkey.test.util.LogMethod;
import org.labkey.test.util.LoggedParam;
import org.labkey.test.util.PasswordUtil;
import org.labkey.test.util.PortalHelper;
import org.labkey.test.util.ext4cmp.Ext4FieldRefWD;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;

import static org.junit.Assert.*;

/**
 * User: brittp
 * Date: Mar 9, 2006
 * Time: 1:54:57 PM
 */
@Category({DailyA.class})
public class SpecimenTest extends SpecimenBaseTest
{
    protected static final String PROJECT_NAME = "SpecimenVerifyProject";
    private final File REQUEST_ATTACHMENT = new File(getPipelinePath() + "specimens", "labs.txt");
    private final PortalHelper _portalHelper = new PortalHelper(this);

    public String getAssociatedModuleDirectory()
    {
        return "server/modules/study";
    }

    @Override
    protected String getProjectName()
    {
        return PROJECT_NAME;
    }

    @Override
    protected void doCleanup(boolean afterTest) throws TestTimeoutException
    {
        deleteUsers(afterTest, USER1, USER2);
        super.doCleanup(afterTest);
    }

    @Override
    protected boolean isFileUploadTest()
    {
        return true;
    }

    @Override
    protected BrowserType bestBrowser()
    {
        return BrowserType.CHROME;
    }

    @Override
    @LogMethod(category = LogMethod.MethodType.SETUP)
    protected void doCreateSteps()
    {
        enableEmailRecorder();
        initializeFolder();

        clickButton("Create Study");
        setFormElement(Locator.name("label"), getStudyLabel());
        click(Locator.radioButtonByNameAndValue("simpleRepository", "false"));
        clickButton("Create Study");

        setPipelineRoot(getPipelinePath());

        setupRequestabilityRules();
        startSpecimenImport(1);
        waitForSpecimenImport();
        _studyHelper.createCustomParticipantGroup(getProjectName(), getFolderName(), "Category1", "Participant", null, false, PTIDS[0], PTIDS[1]);
        checkTubeType();
        setupRequestStatuses();
        setupActorsAndGroups();
        setupDefaultRequirements();
        setupRequestForm();
        setupActorNotification();
        uploadSpecimensFromFile();
    }

    @Override
    @LogMethod(category = LogMethod.MethodType.VERIFICATION)
    protected void doVerifySteps() throws IOException, HttpException
    {
        verifyActorDetails();
        createRequest();
        verifyViews();
        verifyAdditionalRequestFields();
        verifyNotificationEmails();
        verifyInactiveUsersInRequests();
        verifyRequestCancel();
        verifyReports();
        exportSpecimenTest();
        verifyRequestingLocationRestriction();
        verifySpecimenTableAttachments();
        searchTest();
        verifySpecimenGroupings();
        verifyRequestEnabled();
        disableRequests();
        verifyRequestsDisabled();
        verifyDrawTimestamp();
    }

    private void disableRequests()
    {
        clickFolder(getFolderName());
        assertTextPresent("Specimen Requests");
        waitAndClick(Locator.linkWithText("Manage Study"));
        waitAndClick(Locator.linkWithText("Change Repository Type"));
        Locator disableRequestsRadio = Locator.radioButtonByNameAndValue("enableRequests", "false");
        waitForElement(disableRequestsRadio);
        checkRadioButton(disableRequestsRadio);
        clickButton("Submit");
    }

    private void verifyRequestEnabled()
    {
        clickFolder(getFolderName());
        waitForElement(Locator.linkWithText("By Individual Vial"));
        assertTextPresent("Specimen Requests");
        clickAndWait(Locator.linkWithText("By Individual Vial"));
        assertElementPresent(Locator.navButton("Request Options"));
        assertTextPresent("Locked In Request", "Requestable", "Available", "Availability Reason", "Locked In Request Count", "Available Count", "Expected Available Count");
        clickAndWait(Locator.linkWithText("Reports"));
        assertTextPresent("Requested Vials by Type and Timepoint", "Request Summary");
        clickButton("View");
        assertTextPresent("Availability status");
    }

    private void verifyRequestsDisabled()
    {
        clickFolder(getFolderName());
        waitForElement(Locator.linkWithText("By Individual Vial"));
        assertTextNotPresent("Specimen Requests");
        clickAndWait(Locator.linkWithText("By Individual Vial"));
        assertElementNotPresent(Locator.navButton("Request Options"));
        assertTextNotPresent("Locked In Request", "Requestable", "Available", "Availability Reason", "Locked In Request Count", "Available Count", "Expected Available Count");
        clickAndWait(Locator.linkWithText("Reports"));
        assertTextNotPresent("Requested Vials by Type and Timepoint", "Request Summary");
        clickButton("View");
        assertTextNotPresent("Availability status");
    }

    @LogMethod
    private void checkTubeType()
    {
        // Field check for Tube Type column (including conflict)
        clickTab("Overview");
        _portalHelper.addWebPart("Specimens");
        waitAndClickAndWait(Locator.linkWithText("By Individual Vial"));
        setFilter(SPECIMEN_DETAIL, "PrimaryType", "Is Blank");
        // Verify that there's only one vial of unknown type:
        assertLinkPresentWithTextCount("[history]", 1);
        // There's a conflict in TubeType for this vial's events; verify that no TubeType is populated at the vial level
        assertTextNotPresent("Cryovial");
        clickAndWait(Locator.linkWithText("[history]"));
        // This vial has three events, each of which list a different tube type:
        assertTextPresent("15ml Cryovial");
        assertTextPresent("20ml Cryovial");
        assertTextPresent("25ml Cryovial");
        clickAndWait(Locator.linkWithText("Specimen Overview"));
        waitAndClick(Locator.xpath("//span[text()='Vials by Derivative Type']/../img"));
        waitAndClickAndWait(Locator.linkWithText("Tear Flo Strips"));
        // For these three vials, there should be no conflict in TubeType, so we should see the text once for each of three vials:
        assertLinkPresentWithTextCount("[history]", 3);
        assertTextPresent("15ml Cryovial", 3);
    }

    @LogMethod
    private void uploadSpecimensFromFile()
    {
        log("Check Upload Specimen List from file");
        clickTab("Specimen Data");
        waitForVialSearch();
        click(Locator.imageWithAltText("New Request Icon", true));
        waitForElement(Locator.name("destinationLocation"));
        selectOptionByText(Locator.name("destinationLocation"), DESTINATION_SITE);
        setFormElement(Locator.id("input0"), "Assay Plan");
        setFormElement(Locator.id("input2"), "Comments");
        setFormElement(Locator.id("input1"), "Shipping");
        setFormElement(Locator.id("input3"), "Last one");
        clickButton("Create and View Details");
        clickAndWait(Locator.linkWithText("Upload Specimen Ids"));
        setFormElement(Locator.xpath("//textarea[@id='tsv3']"), "AAA07XK5-01");     // add specimen
        clickButton("Submit");    // Submit button

        waitAndClick(Locator.linkWithText("Upload Specimen Ids"));
        waitForElement(Locator.xpath("//textarea[@id='tsv3']"));
        setFormElement(Locator.xpath("//textarea[@id='tsv3']"), "AAA07XK5-01");     // try to add again
        clickButton("Submit", 0);    // Submit button
        waitForText("Specimen AAA07XK5-01 is unavailable", 20000);
        setFormElement(Locator.xpath("//textarea[@id='tsv3']"), "AAA07XK5-02");     // try to add one that doesn't exist
        clickButton("Submit", 0);    // Submit button
        waitForText("Specimen AAA07XK5-02 is unavailable", 20000);
        setFormElement(Locator.xpath("//textarea[@id='tsv3']"), "AAA07XK5-04\nAAA07XK5-06\nAAA07XSF-03");     // add different one
        clickButton("Submit");    // Submit button
    }

    @LogMethod (quiet = true)
    private void verifyActorDetails()
    {
        // Check each Actor's Details for "Default Actor Notification" feature;
        // In Details, for each actor the ether Notify checkbox should be set or disabled, because we set Notifications to All
        Locator detailsLink = Locator.xpath("//td[a='Details']/a");
        int linkCount = getElementCount(detailsLink);
        for (int i = 0; i < linkCount; i++)
        {
            clickAndWait(detailsLink.index(i));
            int allCheckBoxes = getElementCount(Locator.xpath("//input[@type='checkbox' and @name='notificationIdPairs']"));
            int checkedCheckBoxes = getElementCount(Locator.xpath("//input[@type='checkbox' and @name='notificationIdPairs' and @checked]"));
            int disabledCheckBoxes = getElementCount(Locator.xpath("//input[@type='checkbox' and @name='notificationIdPairs' and @disabled]"));
            assertTrue("Actor Notification: All actors should be notified if addresses configured.", allCheckBoxes == checkedCheckBoxes + disabledCheckBoxes);
            clickButton("Cancel");
        }

        waitForElement(Locator.css("span.labkey-wp-title-text").withText("Associated Specimens"));
        assertTextPresent("AAA07XK5-01", "AAA07XK5-04", "AAA07XK5-06", "AAA07XSF-03");


        clickButton("Cancel Request", 0);
        assertAlert("Canceling will permanently delete this pending request.  Continue?");
        waitForElement(Locator.id("dataregion_SpecimenRequest"));
    }

    @LogMethod
    private void createRequest()
    {
        clickTab("Specimen Data");
        waitForVialSearch();
        click(Locator.xpath("//span[text()='Vials by Derivative Type']/../img"));
        waitForElement(Locator.linkWithText("Plasma, Unknown Processing"));
        clickAndWait(Locator.linkWithText("Plasma, Unknown Processing"));
        // Verify unavailable sample
        assertElementPresent(Locator.xpath("//input[@id='check_" + UNREQUESTABLE_SAMPLE + "' and @disabled]"));
        assertElementPresent(Locator.xpath("//input[@id='check_" + UNREQUESTABLE_SAMPLE + "']/../../td[contains(text(), 'This vial is unavailable because it was found in the set called \"" + REQUESTABILITY_QUERY + "\".')]"));
        assertElementPresent(Locator.xpath("//input[@id='check_" + UNREQUESTABLE_SAMPLE + "']/../a[contains(@onmouseover, 'This vial is unavailable because it was found in the set called \\\"" + REQUESTABILITY_QUERY + "\\\".')]"));
        checkCheckbox(".toggle");

        clickMenuButton("Page Size", "Show All");
         clickAndWait(Locator.linkContainingText("history"));
        assertTextPresent("Vial History");
        goBack();

        clickMenuButton("Request Options", "Create New Request");
        selectOptionByText(Locator.name("destinationLocation"), DESTINATION_SITE);
        setFormElement(Locator.id("input0"), "Assay Plan");
        setFormElement(Locator.id("input2"), "Comments");
        setFormElement(Locator.id("input1"), "Shipping");
        clickButton("Create and View Details");
        assertTextPresent("Please provide all required input.");
        setFormElement(Locator.id("input3"), "sample last one input");
        clickButton("Create and View Details");
        assertTextPresent("sample last one input");
        assertTextPresent("IRB");
        assertTextPresent("KCMC, Moshi, Tanzania");
        assertTextPresent("Originating IRB Approval");
        assertTextPresent(SOURCE_SITE);
        assertTextPresent("Providing IRB Approval");
        assertTextPresent(DESTINATION_SITE);
        assertTextPresent("Receiving IRB Approval");
        assertTextPresent("SLG");
        assertTextPresent("SLG Approval");
        assertTextPresent("BAA07XNP-01");
        assertTextNotPresent(UNREQUESTABLE_SAMPLE);
        // verify that the swab specimen isn't present yet
        assertTextNotPresent("DAA07YGW-01");
        assertTextNotPresent("Complete");

        // add additional specimens
        clickTab("Specimen Data");
        waitForVialSearch();
        click(Locator.xpath("//span[text()='Vials by Derivative Type']/../img"));
        waitForElement(Locator.linkWithText("Swab"));
        clickAndWait(Locator.linkWithText("Swab"));
        checkCheckbox(".toggle");
        clickMenuButtonAndContinue("Request Options", "Add To Existing Request");
        _extHelper.waitForExtDialog("Request Vial", WAIT_FOR_JAVASCRIPT);
        waitForElement(Locator.css("#request-vial-details .x-grid3-row"));
        clickButton("Add 8 Vials to Request", 0);
        _extHelper.waitForExtDialog("Success", WAIT_FOR_JAVASCRIPT * 5);
        clickButton("OK", 0);
        clickMenuButton("Request Options", "View Existing Requests");
        clickButton("Details");
        assertTextPresent("sample last one input");
        assertTextPresent("IRB");
        assertTextPresent("KCMC, Moshi, Tanzania");
        assertTextPresent("Originating IRB Approval");
        assertTextPresent(SOURCE_SITE);
        assertTextPresent("Providing IRB Approval");
        assertTextPresent(DESTINATION_SITE);
        assertTextPresent("Receiving IRB Approval");
        assertTextPresent("SLG");
        assertTextPresent("SLG Approval");
        assertTextPresent("BAA07XNP-01");
        assertTextPresent("DAA07YGW-01");

        // submit request
        assertTextPresent("Not Yet Submitted");
        assertTextNotPresent("New Request");
        clickButton("Submit Request", 0);
        assertAlert("Once a request is submitted, its specimen list may no longer be modified.  Continue?");
        assertTextNotPresent("Not Yet Submitted");
        assertTextPresent("New Request");

        // Add request attachment
        click(Locator.linkWithText("Update Request"));
        waitForElement(Locator.name("formFiles[0]"));
        setFormElement(Locator.name("formFiles[0]"), REQUEST_ATTACHMENT);
        clickButton("Save Changes and Send Notifications");
        waitForElement(Locator.linkWithText(" " + REQUEST_ATTACHMENT.getName()));

        // modify request
        selectOptionByText(Locator.name("newActor"), "SLG");
        setFormElement(Locator.name("newDescription"), "Other SLG Approval");
        clickButton("Add Requirement");
        clickAndWait(Locator.linkWithText("Details"));
        checkCheckbox("complete");
        checkCheckbox("notificationIdPairs");
        checkCheckbox("notificationIdPairs", 1);
        clickButton("Save Changes and Send Notifications");
        waitForElement(Locator.css(".labkey-message").withText("Complete"));
    }

    @LogMethod
    private void verifyViews()
    {
        clickAndWait(Locator.linkWithText("View History"));
        assertTextPresent("Request submitted for processing.");
        assertTextPresent("Notification Sent", 2);
        assertTextPresent(USER1);
        assertTextPresent(USER2);
        clickAndWait(Locator.linkWithText("View Request"));
        clickAndWait(Locator.linkWithText("Originating Location Specimen Lists"));
        // Ordering of locations is nondeterministic
        if (isPresentInThisOrder("The McMichael Lab, Oxford, UK", "KCMC, Moshi, Tanzania") == null)
        {
            checkCheckbox("notify"); // MCMichael - SLG
            _specimen_McMichael = getText(Locator.xpath("//tr[@class = 'labkey-alternate-row']/td[3]//td"));
            checkCheckbox("notify", 4); // KCMC - IRB, Aurum Health KOSH
            _specimen_KCMC = getText(Locator.xpath("//tr[@class = 'labkey-row']/td[3]//td"));
        }
        else
        {
            checkCheckbox("notify", 1); // KCMC - IRB, Aurum Health KOSH
            _specimen_KCMC = getText(Locator.xpath("//tr[@class = 'labkey-alternate-row']/td[3]//td"));
            checkCheckbox("notify", 3); // MCMichael - SLG
            _specimen_McMichael = getText(Locator.xpath("//tr[@class = 'labkey-row']/td[3]//td"));
        }
        checkCheckbox("sendXls");
        checkCheckbox("sendTsv");
        clickButton("Send Email");
        _requestId = Integer.parseInt(getUrlParam(getURL().toString(), "id", false));
        clickAndWait(Locator.linkWithText("Providing Location Specimen Lists"));
        assertTextPresent(SOURCE_SITE);
        clickButton("Cancel");
    }

    @LogMethod
    private void verifyAdditionalRequestFields()
    {
        log("verifying addtional freezer fields from the exports");
        clickAndWait(Locator.linkWithText("Originating Location Specimen Lists"));
        addUrlParameter("exportAsWebPage=true");
        refresh();

        pushLocation();
         clickAndWait(Locator.linkContainingText("Export to text file"));

        // verify the additional columns
        assertTextPresent("Freezer", "Fr Container", "Fr Position", "Fr Level1", "Fr Level2");
        popLocation();

        // customize the locationSpecimenListTable then make sure changes are propogated to the exported lists
        log("customizing the locationSpecimenList default view");
        pushLocation();
        goToSchemaBrowser();
        selectQuery("study", "LocationSpecimenList");
        waitForText("view data");
         clickAndWait(Locator.linkContainingText("view data"));

        _customizeViewsHelper.openCustomizeViewPanel();
        _customizeViewsHelper.removeCustomizeViewColumn("Freezer");
        _customizeViewsHelper.removeCustomizeViewColumn("Fr_Container");
        _customizeViewsHelper.removeCustomizeViewColumn("Fr_Position");
        _customizeViewsHelper.removeCustomizeViewColumn("Fr_Level1");
        _customizeViewsHelper.saveCustomView();
        popLocation();

        log("verifying column changes");
        clickTab("Specimen Data");
        waitForVialSearch();
        click(Locator.xpath("//span[text() = 'Specimen Requests']/../img")); // expand node in Specimens webpart
        waitForElement(Locator.linkWithText("View Current Requests"));
        clickAndWait(Locator.linkWithText("View Current Requests"));

        clickButton("Details");
        clickAndWait(Locator.linkWithText("Originating Location Specimen Lists"));
        addUrlParameter("exportAsWebPage=true");
        refresh();
        pushLocation();
         clickAndWait(Locator.linkContainingText("Export to text file"));

        // verify the additional columns
        assertTextNotPresent("Freezer", "Fr Container", "Fr Position", "Fr Level1");
        assertTextPresent("Fr Level2");
        popLocation();

        clickButton("Cancel");
        clickAndWait(Locator.linkWithText("Providing Location Specimen Lists"));
        addUrlParameter("exportAsWebPage=true");
        refresh();
        pushLocation();
         clickAndWait(Locator.linkContainingText("Export to text file"));

        // verify the additional columns
        assertTextNotPresent("Freezer", "Fr Container", "Fr Position", "Fr Level1");
        assertTextPresent("Fr Level2");
        popLocation();

        clickButton("Cancel");
    }

    private String _specimen_McMichael;
    private String _specimen_KCMC;
    private final static String ATTACHMENT1 = "KCMC_Moshi_Ta_to_Aurum_Health_.tsv";
    private final static String ATTACHMENT2 = "KCMC_Moshi_Ta_to_Aurum_Health_KO_%s.xls"; // Params: date(yyyy-MM-dd)
    private final String NOTIFICATION_TEMPLATE = // Params: Study Name, requestId, Study Name, requestId, Username, Date(yyyy-MM-dd)
            "Specimen request #%s was updated in %s.\n" +
            "\n" +
            "Request Details\n" +
            "Specimen Request %s\n" +
            "Destination " + DESTINATION_SITE+"\n" +
            "Status New Request\n" +
            "Modified by %s\n" +
            "Action Originating location notification of specimen shipment to "+DESTINATION_SITE+"\n" +
            "Attachments KCMC_Moshi_Ta_to_Aurum_Health_.tsv\n" +
            "KCMC_Moshi_Ta_to_Aurum_Health_KO_%s.xls\n" +
            "Assay Plan:\n" +
            "Assay Plan\n" +
            "\n" +
            "Shipping Information:\n" +
            "Shipping\n" +
            "\n" +
            "Comments:\n" +
            "Comments\n" +
            "\n" +
            "Last One:\n" +
            "sample last one input\n" +
            "Specimen List (Request Link)\n" +
            "\n" +
            "  Participant Id Global Unique Id Visit Description Sequence Num Visit Volume Volume Units Primary Type Derivative Type Additive Type Derivative Type2 Sub Additive Derivative Draw Timestamp Draw Date Draw Time Clinic Processing Location First Processed By Initials Sal Receipt Date Class Id Protocol Number Primary Volume Primary Volume Units Total Cell Count Tube Type Comments Locked In Request Requestable Site Name Site Ldms Code At Repository Available Availability Reason Quality Control Flag Quality Control Comments Collection Cohort Vial Count Locked In Request Count At Repository Count Available Count Expected Available Count\n" +
            "1 999320824 BAA07XNP-01 Vst 501 501.0 1.0 ML Blood (Whole) Plasma, Unknown Processing EDTA   N/A 2005-12-23 10:05 2005-12-23 10:05:00 KCMC, Moshi, Tanzania Contract Lab Services, Johannesburg, South Africa LK 2005-12-23 LABK 39 15ml Cryovial true Contract Lab Services, Johannesburg, South Africa 350 true false This vial is unavailable because it is locked in a specimen request. false   2 1 2 0 1\n" +
            "2 999320087 CAA07XN8-01 Vst 301 301.0 1.0 ML Vaginal Swab Swab None   N/A 2005-12-22 12:50 2005-12-22 12:50:00 KCMC, Moshi, Tanzania Contract Lab Services, Johannesburg, South Africa LK 2005-12-22 LABK 39 15ml Cryovial true Contract Lab Services, Johannesburg, South Africa 350 true false This vial is unavailable because it is locked in a specimen request. false   1 1 1 0 0\n" +
            "3 999320706 DAA07YGW-01 Vst 301 301.0 1.0 ML Vaginal Swab Swab None   N/A 2006-01-05 10:00 2006-01-05 10:00:00 KCMC, Moshi, Tanzania Contract Lab Services, Johannesburg, South Africa LK 2006-01-05 LABK 39 15ml Cryovial true Contract Lab Services, Johannesburg, South Africa 350 true false This vial is unavailable because it is locked in a specimen request. false   1 1 1 0 0\n" +
            "4 999320898 FAA07XLJ-01 Vst 301 301.0 1.0 ML Vaginal Swab Swab None   N/A 2005-12-20 12:05 2005-12-20 12:05:00 KCMC, Moshi, Tanzania Contract Lab Services, Johannesburg, South Africa LK 2005-12-20 LABK 39 15ml Cryovial true Contract Lab Services, Johannesburg, South Africa 350 true false This vial is unavailable because it is locked in a specimen request. false   1 1 1 0 0\n" +
            "5 999320264 FAA07YSC-01 Vst 201 201.0 1.0 ML Vaginal Swab Swab None   N/A 2006-01-13 12:10 2006-01-13 12:10:00 KCMC, Moshi, Tanzania Contract Lab Services, Johannesburg, South Africa LK 2006-01-13 LABK 39 15ml Cryovial true Contract Lab Services, Johannesburg, South Africa 350 true false This vial is unavailable because it is locked in a specimen request. false   1 1 1 0 0\n" +
            "6 999320520 FAA07YXY-01 Vst 501 501.0 1.0 ML Vaginal Swab Swab None   N/A 2005-12-15 10:30 2005-12-15 10:30:00 KCMC, Moshi, Tanzania Contract Lab Services, Johannesburg, South Africa LK 2006-01-15 LABK 39 15ml Cryovial true Contract Lab Services, Johannesburg, South Africa 350 true false This vial is unavailable because it is locked in a specimen request. false   1 1 1 0 0\n" +
            "7 999320498 JAA07YJB-01 Vst 501 501.0 1.0 ML Vaginal Swab Swab None   N/A 2006-01-05 09:30 2006-01-05 09:30:00 KCMC, Moshi, Tanzania Contract Lab Services, Johannesburg, South Africa LK 2006-01-11 LABK 39 15ml Cryovial true Contract Lab Services, Johannesburg, South Africa 350 true false This vial is unavailable because it is locked in a specimen request. false   1 1 1 0 0\n" +
            "8 999320476 JAA07YSQ-01 Vst 301 301.0 1.0 ML Vaginal Swab Swab None   N/A 2006-01-11 10:20 2006-01-11 10:20:00 KCMC, Moshi, Tanzania Contract Lab Services, Johannesburg, South Africa LK 2006-01-11 LABK 39 15ml Cryovial true Contract Lab Services, Johannesburg, South Africa 350 true false This vial is unavailable because it is locked in a specimen request. false   1 1 1 0 0\n" +
            "9 999320980 KAA07YV1-01 Vst 301 301.0 1.0 ML Vaginal Swab Swab None   N/A 2006-01-17 08:30 2006-01-17 08:30:00 KCMC, Moshi, Tanzania Contract Lab Services, Johannesburg, South Africa LK 2006-01-17 LABK 39 15ml Cryovial true Contract Lab Services, Johannesburg, South Africa 350 true false This vial is unavailable because it is locked in a specimen request. false   1 1 1 0 0\n" +
            "10 999320520 KAA07YY0-01 Vst 501 501.0 1.0 ML Blood (Whole) Plasma, Unknown Processing EDTA   N/A 2005-12-15 10:30 2005-12-15 10:30:00 KCMC, Moshi, Tanzania Contract Lab Services, Johannesburg, South Africa LK 2006-01-15 LABK 39 15ml Cryovial true Contract Lab Services, Johannesburg, South Africa 350 true false This vial is unavailable because it is locked in a specimen request. false   2 2 2 0 0\n" +
            "11 999320520 KAA07YY0-02 Vst 501 501.0 1.0 ML Blood (Whole) Plasma, Unknown Processing EDTA   N/A 2005-12-15 10:30 2005-12-15 10:30:00 KCMC, Moshi, Tanzania Contract Lab Services, Johannesburg, South Africa LK 2006-01-15 LABK 39 15ml Cryovial true Contract Lab Services, Johannesburg, South Africa 350 true false This vial is unavailable because it is locked in a specimen request. false   2 2 2 0 0";
    @LogMethod
    private void verifyNotificationEmails() throws IOException, HttpException
    {
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String notification = String.format(NOTIFICATION_TEMPLATE, _requestId, getStudyLabel(), _requestId, PasswordUtil.getUsername(), date);

        log("Check notification emails");
        goToHome();
        goToModule("Dumbster");
        assertTextPresent("Specimen Request Notification", 8);
        assertTextPresent(USER1, 4);
        assertTextPresent(USER2, 4);

        log("Check for correct data in notification emails");
        int emailIndex = getTableCellText(Locator.id("dataregion_EmailRecord"), 2, 0).equals(USER1) ? 1 : 0;
        click(Locator.linkContainingText("Specimen Request Notification").index(emailIndex));
        shortWait().until(LabKeyExpectedConditions.emailIsExpanded(emailIndex + 1));
        String bodyText = getText(Locator.id("dataregion_EmailRecord"));
        assertTrue(!bodyText.contains(_specimen_McMichael));
        assertTrue(bodyText.contains(_specimen_KCMC));
        DataRegionTable mailTable = new DataRegionTable("EmailRecord", this, false, false);
        String message = mailTable.getDataAsText(emailIndex, "Message");
        assertNotNull("No message found", message);
        assertTrue("Notification was not as expected.\nExpected:\n" + notification + "\n\nActual:\n" + message, message.contains(notification));

        String attachment1 = getAttribute(Locator.linkWithText(ATTACHMENT1), "href");
        String attachment2 = getAttribute(Locator.linkWithText(String.format(ATTACHMENT2, date)), "href");

        assertEquals("Bad link to attachment: " + ATTACHMENT1, HttpStatus.SC_OK, WebTestHelper.getHttpGetResponse(attachment1));
        assertEquals("Bad link to attachment: " + String.format(ATTACHMENT2, date), HttpStatus.SC_OK, WebTestHelper.getHttpGetResponse(attachment2));

        clickAndWait(Locator.linkWithText("Request Link"));
        assertTextPresent("Specimen Request " + _requestId);
    }

    @LogMethod
    private void verifyRequestCancel()
    {
        goToProjectHome();
        clickFolder(getFolderName());

        waitForElement(Locator.linkWithText("By Individual Vial"));
        waitAndClick(Locator.xpath("//span[text() = 'Specimen Requests']/../../a"));
        waitAndClick(Locator.linkWithText("View Current Requests"));

        waitForElement(Locator.id("dataregion_SpecimenRequest"));
        clickButton("Details");

        clickAndWait(Locator.linkWithText("Update Request"));
        selectOptionByText(Locator.name("status"), "Not Yet Submitted");
        clickButton("Save Changes and Send Notifications");
        clickButton("Cancel Request", 0);
        assertAlert("Canceling will permanently delete this pending request.  Continue?");
        waitForText("No data to show.");
        clickTab("Specimen Data");
        waitForVialSearch();
        click(Locator.xpath("//span[text()='Vials by Derivative Type']/../img"));
        waitForElement(Locator.linkWithText("Swab"));
        clickAndWait(Locator.linkWithText("Swab"));
        checkCheckbox(".toggle");
        clickMenuButton("Request Options", "Create New Request");
        clickButton("Cancel");
    }

    @LogMethod
    private void verifyReports()
    {
        log("check reports by participant group");
        clickTab("Specimen Data");
        waitForVialSearch();
        clickAndWait(Locator.linkWithText("Blood (Whole)"));
        pushLocation();
        clickAndWait(Locator.linkWithText("Reports"));
        clickButton("View"); // Summary Report
        //Verify by vial count
        assertElementPresent(Locator.xpath("//a[number(text()) > 0]"), 36);
        selectOptionByText(Locator.name("participantGroupFilter"), "Category1");
        clickButton("Refresh");
        assertElementNotPresent(Locator.xpath("//a[number(text()) > 6]"));
        assertElementPresent(Locator.xpath("//a[number(text()) <= 6]"), 8);
        selectOptionByText(Locator.name("participantGroupFilter"), "All Groups");
        clickButton("Refresh");
        assertElementPresent(Locator.xpath("//a[number(text()) > 0]"), 36);
        //Verify by ptid list
        checkCheckbox("viewPtidList");
        uncheckCheckbox("viewVialCount");
        clickButton("Refresh");
        assertLinkPresentWithTextCount(PTIDS[0], 3);
        assertLinkPresentWithTextCount(PTIDS[1], 5);
        selectOptionByText(Locator.name("participantGroupFilter"), "Category1");
        clickButton("Refresh");
        assertLinkPresentWithTextCount(PTIDS[0], 3);
        assertLinkPresentWithTextCount(PTIDS[1], 5);
    }

    @LogMethod
    private void verifyRequestingLocationRestriction()
    {
        goToProjectHome();
        clickFolder(getFolderName());

        verifyRequestingLocationCounts(StudyLocationType.values()); // All locations should be enabled by default

        enableAndVerifyRequestingLocationTypes();
        enableAndVerifyRequestingLocationTypes(StudyLocationType.CLINIC);
        enableAndVerifyRequestingLocationTypes(StudyLocationType.ENDPOINT);
        enableAndVerifyRequestingLocationTypes(StudyLocationType.REPOSITORY);
        enableAndVerifyRequestingLocationTypes(StudyLocationType.SAL);
        enableAndVerifyRequestingLocationTypes(StudyLocationType.REPOSITORY, StudyLocationType.CLINIC);
        enableAndVerifyRequestingLocationTypes(StudyLocationType.values());
    }

    @LogMethod
    private void verifySpecimenTableAttachments()
    {
        goToProjectHome();
        clickFolder(getFolderName());

        log("Setup Excel specimen attachment");
        clickAndWait(Locator.linkWithText("Manage"));
        clickAndWait(Locator.linkWithText("Manage Notifications"));
        checkCheckbox(Locator.checkboxById("newRequestNotifyCheckbox"));
        setFormElement(Locator.id("newRequestNotify"), PasswordUtil.getUsername());
        checkRadioButton(Locator.radioButtonByNameAndValue("specimensAttachment", "ExcelAttachment"));
        clickButton("Save");

        log("Create request with excel specimen attachment");
        clickTab("Specimen Data");
        waitForVialSearch();
        clickAndWait(Locator.linkWithText("Urine"));
        checkDataRegionCheckbox("SpecimenDetail", 0);
        _extHelper.clickMenuButton(true, "Request Options", "Create New Request");
        selectOptionByText(Locator.name("destinationLocation"), DESTINATION_SITE);
        setFormElement(Locator.id("input0"), "Assay Plan");
        setFormElement(Locator.id("input1"), "Shipping");
        setFormElement(Locator.id("input3"), "Comments");
        clickButton("Create and View Details");
        clickButton("Submit Request", 0);
        getAlert();
        waitForElement(Locator.css("h3").withText("Your request has been successfully submitted."));

        log("Setup text specimen attachment");
        clickAndWait(Locator.linkWithText("Manage"));
        clickAndWait(Locator.linkWithText("Manage Notifications"));
        checkRadioButton(Locator.radioButtonByNameAndValue("specimensAttachment", "TextAttachment"));
        clickButton("Save");

        log("Create request with text specimen attachment");
        clickTab("Specimen Data");
        waitForVialSearch();
        clickAndWait(Locator.linkWithText("Urine"));
        checkDataRegionCheckbox("SpecimenDetail", 1);
        _extHelper.clickMenuButton(true, "Request Options", "Create New Request");
        selectOptionByText(Locator.name("destinationLocation"), DESTINATION_SITE);
        setFormElement(Locator.id("input0"), "Assay Plan");
        setFormElement(Locator.id("input1"), "Shipping");
        setFormElement(Locator.id("input3"), "Comments");
        clickButton("Create and View Details");
        clickButton("Submit Request", 0);
        getAlert();
        waitForElement(Locator.css("h3").withText("Your request has been successfully submitted."));

        log("Verify specimen list attachments");
        goToModule("Dumbster");

        click(Locator.linkContainingText("Specimen Request Notification"));
        waitForElement(Locator.linkWithText("SpecimenDetail.tsv"));
        click(Locator.linkContainingText("Specimen Request Notification").index(1));
        waitForElement(Locator.linkWithText("SpecimenDetail.xls"));

        // Each notification should be have only the specimen request details, no specimen list
        assertElementPresent(Locator.css("#email_body_1 > table"), 1);
        assertElementPresent(Locator.css("#email_body_2 > table"), 1);
    }

    @LogMethod
    private void verifyInactiveUsersInRequests()
    {
        enableEmailRecorder(); // clear email recorder
        goToSiteUsers();
        DataRegionTable usersTable = new DataRegionTable("Users", this, true, true);
        int row = usersTable.getRow("Email", USER2);
        usersTable.checkCheckbox(row);
        clickButton("Deactivate");
        clickButton("Deactivate");
        assertTextNotPresent(USER2);

        goToProjectHome();
        clickFolder(getFolderName());

        waitAndClick(Locator.xpath("//span[text() = 'Specimen Requests']/../../a"));
        waitAndClick(Locator.linkWithText("View Current Requests"));

        waitForElement(Locator.id("dataregion_SpecimenRequest"));
        clickButton("Details");

        waitAndClick(Locator.linkWithText("Update Request"));

        waitForElement(Locator.pageHeader("Update Request"));
        checkCheckbox(Locator.checkboxByName("notificationIdPairs"));
        click(Locator.css(".labkey-help-pop-up"));
        waitForElement(Locator.xpath("id('helpDivBody')").containing(USER1));

        checkCheckbox(Locator.checkboxByName("notificationIdPairs").index(1));
        click(Locator.css(".labkey-help-pop-up").index(1));
        waitForElement(Locator.xpath("id('helpDivBody')/del").withText(USER2));

        setFormElement(Locator.name("requestDescription"), "Just one notification.");
        pushLocation();
        clickButton("Save Changes and Send Notifications");

        popLocation();

        waitForElement(Locator.pageHeader("Update Request"));
        checkCheckbox(Locator.checkboxByName("notificationIdPairs"));
        checkCheckbox(Locator.checkboxByName("notificationIdPairs").index(1));
        checkCheckbox(Locator.checkboxByName("emailInactiveUsers"));
        setFormElement(Locator.name("requestDescription"), "Two notifications.");
        clickButton("Save Changes and Send Notifications");

        goToModule("Dumbster");
    }

    /**
     * Allow all provided location types to make requests, disallow all others
     * @param types List of location types to allow to be requesting locations
     */
    @LogMethod
    private void enableAndVerifyRequestingLocationTypes(@LoggedParam StudyLocationType... types)
    {
        clickTab("Manage");
        waitAndClick(Locator.linkWithText("Manage Location Types"));
        waitForElement(Locator.id("labkey-nav-trail-current-page").withText("Manage Location Types"));

        for (StudyLocationType type : StudyLocationType.values())
        {
            if (Arrays.asList(types).contains(type))
                _ext4Helper.checkCheckbox(type.toString());
            else
                _ext4Helper.uncheckCheckbox(type.toString());
        }

        clickButton("Save", 0);
        waitForElement(Locator.id("labkey-nav-trail-current-page").withText("Manage Study"));

        verifyRequestingLocationCounts(types);
    }

    /**
     * Verify that only permitted locations can submit specimen requests
     * Location count algorithm is valid only for locations defined in sample_a.specimens
     */
    private void verifyRequestingLocationCounts(StudyLocationType... types)
    {
        clickTab("Specimen Data");
        waitForVialSearch();
        click(Locator.imageWithAltText("New Request Icon", true));
        waitForElement(Locator.id("labkey-nav-trail-current-page").withText("New Specimen Request"));

        int expectedLocationCount = StudyLocationType.untypedSites();

        long additionalLocations = Math.round(Math.pow(2, StudyLocationType.values().length - 1));

        for (StudyLocationType type : StudyLocationType.values())
        {
            if (Arrays.asList(types).contains(type))
            {
                assertElementPresent(Locator.xpath("id('destinationLocation')/option").containing(type.toString()), type.siteCount());
                expectedLocationCount += additionalLocations;
                additionalLocations = additionalLocations / 2; // Each additional Location type adds less unique locations
            }
        }

        assertElementPresent(Locator.css("#destinationLocation option"), expectedLocationCount + 1); // +1 for blank select option

        clickButton("Cancel", 0);
        waitForElement(Locator.id("labkey-nav-trail-current-page").withText("Specimen Requests"));
    }

    /**
     * Verify changing the specimen groupings that appear on the specimen web part
     */
    @LogMethod
    private void verifySpecimenGroupings()
    {
        goToProjectHome();
        clickFolder(getFolderName());
        clickTab("Manage");
        waitAndClick(Locator.linkWithText("Configure Specimen Groupings"));
        waitForElement(Locator.id("labkey-nav-trail-current-page").withText("Configure Specimen Web Part"));
        _ext4Helper.selectComboBoxItemById("combo11", "Processing Location");
        _ext4Helper.selectComboBoxItemById("combo12", "Primary Type");
        _ext4Helper.selectComboBoxItemById("combo13", "Site Name");
        _ext4Helper.selectComboBoxItemById("combo21", "Additive Type");
        _ext4Helper.selectComboBoxItemById("combo22", "Derivative Type");
        _ext4Helper.selectComboBoxItemById("combo23", "Tube Type");
        clickButton("Save");
        waitForElement(Locator.id("labkey-nav-trail-current-page").withText("Manage Study"));
        clickTab("Specimen Data");
        waitForVialSearch();
        assertTextPresent("Vials by Processing Location", "Vials by Additive Type", "The McMichael Lab");
        assertTextPresent("NICD - Joberg", 2);
        clickAndWait(Locator.linkContainingText("The McMichael Lab, Oxford"));
        assertTextPresent("Vials", "(ProcessingLocation = The McMichael Lab, Oxford, UK)");

        // Put groupings back for other tests
        clickTab("Manage");
        waitAndClick(Locator.linkWithText("Configure Specimen Groupings"));
        waitForElement(Locator.id("labkey-nav-trail-current-page").withText("Configure Specimen Web Part"));
        _ext4Helper.selectComboBoxItemById("combo11", "Primary Type");
        _ext4Helper.selectComboBoxItemById("combo12", "Derivative Type");
        _ext4Helper.selectComboBoxItemById("combo13", "Additive Type");
        _ext4Helper.selectComboBoxItemById("combo21", "Derivative Type");
        _ext4Helper.selectComboBoxItemById("combo22", "Additive Type");

        clickButton("Save");
    }

    /**
     * Provides info about locations defined in sample_a.specimens
     */
    private enum StudyLocationType
    {
        REPOSITORY("Repository", 8),
        CLINIC("Clinic", 8),
        SAL("Site Affiliated Lab", 8),
        ENDPOINT("Endpoint Lab", 8);

        private String _type;
        private int _count;

        private StudyLocationType(String type, int count)
        {
            _type=type;
            _count=count;
        }

        public String toString()
        {
            return _type;
        }

        public int siteCount()
        {
            return _count;
        }

        public static int untypedSites()
        {
            return 9;
        }
    }

    @LogMethod
    private void searchTest()
    {
        goToProjectHome();
        clickFolder(getFolderName());
        clickTab("Specimen Data");
        waitForVialSearch();
        Ext4FieldRefWD additiveType = Ext4FieldRefWD.getForLabel(this, "Additive Type");
        additiveType.setValue("Heparin");
        Ext4FieldRefWD.getForLabel(this, "Participant").setValue("999320812");
        clickButtonContainingText("Search");
        assertTextNotPresent("Serum Separator");
        assertTextPresent("(ParticipantId = 999320812) AND (AdditiveType = Heparin)");
        goBack();
        waitForVialSearch();
        additiveType.setValue(new String[] {"Heparin", "Ammounium Heparin"});
        clickButtonContainingText("Search");
        assertTextPresent("ONE OF");
        goBack();
        waitForVialSearch();
        additiveType.setValue(new String[] {"Ammonium Heparin","Cell Preparation Tube Heparin","Cell Preparation Tube SCI","Citrate Phosphate Dextrose","EDTA","Fetal Fibronectin Buffer","Guanidine Isothiocyanate (GITC)","Heparin","Liquid Potassium EDTA","Liquid Sodium EDTA","Lithium Heparin","Lithium Heparin and Gel for Plasma","None","Normal Saline","Optimum Cutting Temperature Medium","Orasure Collection Container","Other","PAXgene Blood RNA tube","Phosphate Buffered Saline","Plasma Preparation Tube","PLP Fixative","Port-a-cul Transport Tube","Potassium EDTA","RNA Later","Serum Separator","Sodium Citrate","Sodium EDTA","Sodium Fluoride","Sodium Fluoride/Potassium Oxalate","Sodium Heparin","Sodium Polyanetholesulfonate","Spray Dried Potassium EDTA","Spray Dried Sodium EDTA","Thrombin","Tissue Freezing Medium","Unknown Additive","Viral Transport Media"});
        clickButtonContainingText("Search");
        assertTextPresent("IS NOT ANY OF ");
    }

    @LogMethod
    private void exportSpecimenTest()
    {
        popLocation();
        addUrlParameter("&exportType=excelWebQuery");
        assertTextPresent("org.labkey.study.query.SpecimenRequestDisplayColumn");
        goBack();


        goToAuditLog();
        selectOptionByText(Locator.name("view"), "Query export events");
        waitForElement(Locator.id("dataregion_query"));

        DataRegionTable auditTable =  new DataRegionTable("query", this);
        String[][] columnAndValues = new String[][] {{"Created By", getDisplayName()},
                {"Project", PROJECT_NAME}, {"Container", getFolderName()}, {"SchemaName", "study"},
                {"QueryName", SPECIMEN_DETAIL}, {"Comment", "Exported to Excel Web Query data"}};
        for(String[] columnAndValue : columnAndValues)
        {
            log("Checking column: "+ columnAndValue[0]);
            assertEquals(columnAndValue[1], auditTable.getDataAsText(0, columnAndValue[0]));
        }
    }

    private final Hashtable<String, String> _drawTimestampConflicts = new Hashtable<>();
    private boolean _spotCheckNoConflict;

    @LogMethod
    private void verifyDrawTimestamp()
    {
        String SPECIMEN_ARCHIVE_DTS = getStudySampleDataPath() + "specimens/dts.specimens";
        startSpecimenImport(2, SPECIMEN_ARCHIVE_DTS);
        waitForSpecimenImport();

        // load our expected results
        _drawTimestampConflicts.put("526455390.2504.346", "Conflicts found: DrawDate");
        _drawTimestampConflicts.put("526515315.6304.370", "Conflicts found: DrawTime");
        _drawTimestampConflicts.put("526515315.6304.375", "Conflicts found: DrawDate, DrawTime");
        _drawTimestampConflicts.put("526515651.3704.363", "Conflicts found: DrawDate, DrawTime");
        _spotCheckNoConflict = true;

        clickTab("Specimen Data");
        waitForVialSearch();
        clickAndWait(Locator.linkWithText("By Individual Vial"));

        DataRegionTable table = new DataRegionTable("SpecimenDetail", this);

        int rowCount = table.getDataRowCount() - 1; // ignore the total row count row
        for (int i = 0; i < rowCount; i ++)
        {
            String drawTimestamp = table.getDataAsText(i, "Draw Timestamp");
            String drawDate = table.getDataAsText(i, "Draw Date");
            String drawTime = table.getDataAsText(i, "Draw Time");
            String qcControlComments = table.getDataAsText(i, "Quality Control Comments");
            String id = table.getDataAsText(i, "Global Unique Id");

            verifyDrawTimestampConflict(qcControlComments, drawTimestamp, drawDate, drawTime);
            verifyDrawTimestampExpectedConflict(id, qcControlComments);

            if (doSpecimenEventCheck(qcControlComments))
            {
                clickAndWait(Locator.linkContainingText("[history]", i));
                verifyHistory(qcControlComments);
            }
        }
    }

     // spot check the specimen events for all conflicts and only for one non-conflict
    private boolean doSpecimenEventCheck(String qcControlComments)
    {
        if (StringUtils.isNotBlank(qcControlComments))
            return true;

        if (_spotCheckNoConflict)
        {
            _spotCheckNoConflict = false;
            return true;
        }

        return false;
    }

    private void verifyDrawTimestampExpectedConflict(String id, String qcControl)
    {
        if (StringUtils.isBlank(qcControl))
        {
            assertFalse(id + " should not have a QC conflict", _drawTimestampConflicts.containsKey(id));
        }
        else
        {
            assertTrue(id + " should have a QC conflict",_drawTimestampConflicts.containsKey(id));
            String expectedConflict = _drawTimestampConflicts.get(id);
            assertTrue(id + " " + expectedConflict + " is not equal to " + qcControl, expectedConflict.equalsIgnoreCase(qcControl));
        }
    }

    private void verifyDrawTimestampConflict(String qcControl, String timestamp, String date, String time)
    {
        if (StringUtils.isBlank(qcControl))
        {
            // no conflict, so all three fields shold be valid
            assertTrue(StringUtils.isNotBlank(timestamp));
            assertTrue(StringUtils.isNotBlank(time));
            assertTrue(StringUtils.isNotBlank(date));
            assertTrue(timestamp.contains(date));

            // timestamp does not contain seconds but the time field does
            assertTrue(timestamp.contains(time.substring(0, time.lastIndexOf(":"))));
        }
        else
        {
            // All of the specimens in dts specimens table should only have Draw Timestamp related
            // conflicts.  If there is a conflict, then the timestamp should be blank
            assertTrue(StringUtils.isBlank(timestamp));

            if (qcControl.contains("DrawDate"))
                assertTrue(StringUtils.isBlank(date));
            else
                assertFalse(StringUtils.isBlank(date));

            if(qcControl.contains("DrawTime"))
                assertTrue(StringUtils.isBlank(time));
            else
                assertFalse(StringUtils.isBlank(time));
        }
    }

    @LogMethod
    private void verifyHistory(String qcControl)
    {
        DataRegionTable table = new DataRegionTable("SpecimenEvent", this);

        String prevDrawTimestamp = null;
        String drawTimestamp;
        boolean foundConflict = false;

        for (int i = 0; i < table.getDataRowCount(); i++)
        {
            drawTimestamp = table.getDataAsText(i, "Draw Timestamp");

            if (null == prevDrawTimestamp)
            {
                prevDrawTimestamp = drawTimestamp;
            }
            else
            if (!prevDrawTimestamp.equalsIgnoreCase(drawTimestamp))
            {
                foundConflict = true;
                break;
            }
        }

        if (foundConflict)
            assertTrue(StringUtils.isNotBlank(qcControl));
        else
            assertTrue(StringUtils.isBlank(qcControl));

        goBack();
        waitForText("[history]");
    }
}
