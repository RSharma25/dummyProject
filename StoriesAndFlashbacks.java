package com.synchronoss.cqe.cloud.mobile.tests.SingleClient.SmokeAndSanity;

import com.synchronoss.cqe.cloud.mobile.base.CloudFile;
import com.synchronoss.cqe.cloud.mobile.base.CloudTest;
import com.synchronoss.cqe.cloud.mobile.base.TestGroups;
import com.synchronoss.cqe.cloud.mobile.pages.Notifications.Android.Notification;
import com.synchronoss.cqe.cloud.mobile.pages.Notifications.Android.NotificationsPage;
import com.synchronoss.cqe.cloud.mobile.pages.media.PhotosAndVideos.*;
import com.synchronoss.cqe.common.mobile.app.Content;
import com.synchronoss.cqe.common.mobile.utils.device.DeviceUtils;
import com.synchronoss.cqe.common.mobile.utils.device.android.AndroidUtils;
import io.appium.java_client.MobileElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class StoriesAndFlashbacks extends CloudTest {
    public static Logger logger = LogManager.getLogger(StoriesAndFlashbacks.class.getName());
    PhotosAndVideosPage photosAndVideosPage;
    AllPhotosPane allPhotosPane;

    @BeforeTest(groups = {TestGroups.SC_SANITY})
    public void beforeTest(ITestContext context) {
        cloudFiles.add(new CloudFile(Content.PHOTOS,5,0,0,1,"/photos"));
        setTestDataCleanupCloud(true,CleanupType.MEDIA_ONLY);
        super.beforeTest(context);
        photosAndVideosPage = homePage.goPhotosAndVideos();
    }

    @AfterTest(groups = {TestGroups.SC_SANITY})
    public void afterTest(ITestContext context) {
        super.afterTest(context);
    }

    @Test(testName = "View flashbacks",
            description = "View flashbacks",
            priority = 1,
            groups = {TestGroups.SC_SANITY})
    public void Flashbacks_View() {
        FlashbackPane flashbackPane = (FlashbackPane) photosAndVideosPage.goToPane(PhotosAndVideosPage.Pane.FLASH_BACKS);
        if (flashbackPane != null) {
            if (flashbackPane.getFlashBackItemCount() < 1) {
                flashbackPane.reporter.fail(logger, "Failed to create flash back no flash back is present.");
            }
            flashbackPane.waitForLoadingPage();
            int thumbNailCount = flashbackPane.getFlashbackThumbNailCount(0);
            if (thumbNailCount > 0) {
                flashbackPane.reporter.pass(logger, "Videos are displayed for the flashback grouping within the specified week range");
            } else {
                flashbackPane.reporter.fail(logger, "Videos are displayed for the flashback grouping within the specified week range");
            }
        }
    }


    @Test(testName = "View and Play Stories",
            description = "View and Play Stories",
            priority = 2,
            groups = {TestGroups.SC_SANITY})
    public void Stories_ViewPlaySave() {
        String storyName = "MyStory";
        photosAndVideosPage.waitForLoadingPage();
        StoriesPane storiesPane = (StoriesPane) photosAndVideosPage.goToPane(PhotosAndVideosPage.Pane.STORIES);
        if (storiesPane != null) {
            boolean success = storiesPane.clickStoryItem(0);
            if (success) {
                storiesPane.reporter.pass(logger, "Clicked story");
                testSut.navigateBack();
            } else {
                storiesPane.reporter.fail(logger, "Unable to click story");
            }
            StoryViewerPage storyViewerPage = new StoryViewerPage(testSut);
            storyViewerPage.clickPlayButton();
            InadequateConnectionPopup inadequateConnectionPopup = new InadequateConnectionPopup(testSut,true);
            if (inadequateConnectionPopup.isValid()) {
                inadequateConnectionPopup.clickPlayButton();
            }
            RealPlayerPaneForAddStory realplayerAddStory = new RealPlayerPaneForAddStory(testSut);
            if (!realplayerAddStory.isValid()) {
                reporter.fail(logger,"Real time player is not valid");
            } else {
                reporter.pass(logger,"Real time player plays the video");
            }
            realplayerAddStory.clickSaveItButton();
            SaveYourStoryPopup saveStorypopup = new SaveYourStoryPopup(testSut, true);
            DeviceUtils.safe_sleep(2000);
            if (!saveStorypopup.isValid()) {
                saveStorypopup.reporter.fail(logger, "Save story popup is not valid");
            }
            storyName = realplayerAddStory.generateFileName(storyName);
            saveStorypopup.enterAddStoryName(storyName);
            saveStorypopup.clickSaveButton();
            realplayerAddStory.clickBackButton();

            testSut.navigateBack();
            storiesPane.waitForLoadingPage();
            if (!storiesPane.isValid()) {
                testSut.navigateBack();
                storiesPane.waitForLoadingPage();
            }
            allPhotosPane = (AllPhotosPane) photosAndVideosPage.goToPane(PhotosAndVideosPage.Pane.ALL);
            allPhotosPane.selectFilterByOption(FilterByPopup.FilterByOption.SAVED_STORIES);
            homePage = photosAndVideosPage.goHome();
            MobileElement backedUpstoryElement = homePage.getMobileElement(homePage.savedStoryIcon);
            if(backedUpstoryElement == null) {
                reporter.info(logger,"Story generated not backed up in 60 seconds, trying manual backup");
                homePage.backUp();
                NotificationsPage notificationsPage = new NotificationsPage(testSut);
                notificationsPage.waitForNotification(NotificationsPage.NotificationTexts.BACKUP_COMPLETE,true);
            }
            homePage.goPhotosAndVideos();
            allPhotosPane = (AllPhotosPane) photosAndVideosPage.goToPane(PhotosAndVideosPage.Pane.ALL);
            customAssert.assertTrue(allPhotosPane.getGridItemCount() > 0, "Verifying saved story is successfully created or not");
        }
    }
}