package de.test.antennapod.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.ActivityInstrumentationTestCase2;
import android.test.FlakyTest;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ScrollView;

import com.robotium.solo.Solo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.activity.OnlineFeedViewActivity;
import de.danoeh.antennapod.core.feed.Feed;
import de.danoeh.antennapod.core.preferences.UserPreferences;
import de.danoeh.antennapod.core.storage.PodDBAdapter;
import de.danoeh.antennapod.fragment.DownloadsFragment;
import de.danoeh.antennapod.fragment.EpisodesFragment;
import de.danoeh.antennapod.fragment.PlaybackHistoryFragment;
import de.danoeh.antennapod.fragment.QueueFragment;
import de.danoeh.antennapod.preferences.PreferenceController;

/**
 * User interface tests for MainActivity
 */
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private Solo solo;
    private UITestUtils uiTestUtils;

    private SharedPreferences prefs;

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Context context = getInstrumentation().getTargetContext();
        uiTestUtils = new UITestUtils(context);
        uiTestUtils.setup();

        // create new database
        PodDBAdapter.init(context);
        PodDBAdapter.deleteDatabase();
        PodDBAdapter adapter = PodDBAdapter.getInstance();
        adapter.open();
        adapter.close();

        // override first launch preference
        // do this BEFORE calling getActivity()!
        prefs = getInstrumentation().getTargetContext().getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(MainActivity.PREF_IS_FIRST_LAUNCH, false).commit();

        solo = new Solo(getInstrumentation(), getActivity());
    }


    @Override
    protected void tearDown() throws Exception {
        uiTestUtils.tearDown();
        solo.finishOpenedActivities();

        PodDBAdapter.deleteDatabase();

        // reset preferences
        prefs.edit().clear().commit();

        super.tearDown();
    }

    private void openNavDrawer() {
        solo.clickOnImageButton(0);
        getInstrumentation().waitForIdleSync();
    }

    public void testAddFeed() throws Exception {
        uiTestUtils.addHostedFeedData();
        final Feed feed = uiTestUtils.hostedFeeds.get(0);
        openNavDrawer();
        solo.clickOnText(solo.getString(R.string.add_feed_label));
        solo.enterText(0, feed.getDownload_url());
        solo.clickOnButton(solo.getString(R.string.confirm_label));
        solo.waitForActivity(OnlineFeedViewActivity.class);
        solo.waitForView(R.id.butSubscribe);
        assertEquals(solo.getString(R.string.subscribe_label), solo.getButton(0).getText().toString());
        solo.clickOnButton(0);
        solo.waitForText(solo.getString(R.string.subscribed_label));
    }

    @FlakyTest(tolerance = 3)
    public void testClickNavDrawer() throws Exception {
        uiTestUtils.addLocalFeedData(false);

        UserPreferences.setHiddenDrawerItems(new ArrayList<String>());

        // queue
        openNavDrawer();
        solo.clickOnText(solo.getString(R.string.queue_label));
        solo.waitForView(android.R.id.list);
        assertEquals(solo.getString(R.string.queue_label), getActionbarTitle());

        // episodes
        openNavDrawer();
        solo.clickOnText(solo.getString(R.string.episodes_label));
        solo.waitForView(android.R.id.list);
        assertEquals(solo.getString(R.string.episodes_label), getActionbarTitle());

        // Subscriptions
        openNavDrawer();
        solo.clickOnText(solo.getString(R.string.subscriptions_label));
        solo.waitForView(R.id.subscriptions_grid);
        assertEquals(solo.getString(R.string.subscriptions_label), getActionbarTitle());

        // downloads
        openNavDrawer();
        solo.clickOnText(solo.getString(R.string.downloads_label));
        solo.waitForView(android.R.id.list);
        assertEquals(solo.getString(R.string.downloads_label), getActionbarTitle());

        // playback history
        openNavDrawer();
        solo.clickOnText(solo.getString(R.string.playback_history_label));
        solo.waitForView(android.R.id.list);
        assertEquals(solo.getString(R.string.playback_history_label), getActionbarTitle());

        // add podcast
        openNavDrawer();
        solo.clickOnText(solo.getString(R.string.add_feed_label));
        solo.waitForView(R.id.txtvFeedurl);
        assertEquals(solo.getString(R.string.add_feed_label), getActionbarTitle());

        // podcasts
        ListView list = (ListView) solo.getView(R.id.nav_list);
        for (int i = 0; i < uiTestUtils.hostedFeeds.size(); i++) {
            Feed f = uiTestUtils.hostedFeeds.get(i);
            openNavDrawer();
            solo.scrollListToLine(list, i);
            solo.clickOnText(f.getTitle());
            solo.waitForView(android.R.id.list);
            assertEquals("", getActionbarTitle());
        }
    }

    private String getActionbarTitle() {
        return ((MainActivity) solo.getCurrentActivity()).getSupportActionBar().getTitle().toString();
    }

    @SuppressWarnings("unchecked")
    @FlakyTest(tolerance = 3)
    public void testGoToPreferences() {
        openNavDrawer();
        solo.clickOnText(solo.getString(R.string.settings_label));
        solo.waitForActivity(PreferenceController.getPreferenceActivity());
    }

    public void testDrawerPreferencesHideSomeElements() {
        UserPreferences.setHiddenDrawerItems(new ArrayList<String>());
        openNavDrawer();
        solo.clickLongOnText(solo.getString(R.string.queue_label));
        solo.waitForDialogToOpen();
        solo.clickOnText(solo.getString(R.string.episodes_label));
        solo.clickOnText(solo.getString(R.string.playback_history_label));
        solo.clickOnText(solo.getString(R.string.confirm_label));
        solo.waitForDialogToClose();
        List<String> hidden = UserPreferences.getHiddenDrawerItems();
        assertEquals(2, hidden.size());
        assertTrue(hidden.contains(EpisodesFragment.TAG));
        assertTrue(hidden.contains(PlaybackHistoryFragment.TAG));
    }

    public void testDrawerPreferencesUnhideSomeElements() {
        List<String> hidden = Arrays.asList(PlaybackHistoryFragment.TAG, DownloadsFragment.TAG);
        UserPreferences.setHiddenDrawerItems(hidden);
        openNavDrawer();
        solo.clickLongOnText(solo.getString(R.string.queue_label));
        solo.waitForDialogToOpen();
        solo.clickOnText(solo.getString(R.string.downloads_label));
        solo.clickOnText(solo.getString(R.string.queue_label));
        solo.clickOnText(solo.getString(R.string.confirm_label));
        solo.waitForDialogToClose();
        hidden = UserPreferences.getHiddenDrawerItems();
        assertEquals(2, hidden.size());
        assertTrue(hidden.contains(QueueFragment.TAG));
        assertTrue(hidden.contains(PlaybackHistoryFragment.TAG));
    }

    public void testDrawerPreferencesHideAllElements() {
        UserPreferences.setHiddenDrawerItems(new ArrayList<String>());
        String[] titles = getInstrumentation().getTargetContext().getResources().getStringArray(R.array.nav_drawer_titles);

        openNavDrawer();
        solo.clickLongOnText(solo.getString(R.string.queue_label));
        solo.waitForDialogToOpen();
        for (String title : titles) {
            solo.clickOnText(title);
        }
        solo.clickOnText(solo.getString(R.string.confirm_label));
        solo.waitForDialogToClose();
        List<String> hidden = UserPreferences.getHiddenDrawerItems();
        assertEquals(titles.length, hidden.size());
        for (String tag : MainActivity.NAV_DRAWER_TAGS) {
            assertTrue(hidden.contains(tag));
        }
    }

    public void testDrawerPreferencesHideCurrentElement() {
        UserPreferences.setHiddenDrawerItems(new ArrayList<String>());

        openNavDrawer();
        String downloads = solo.getString(R.string.downloads_label);
        solo.clickOnText(downloads);
        solo.waitForView(android.R.id.list);
        openNavDrawer();
        solo.clickLongOnText(downloads);
        solo.waitForDialogToOpen();
        solo.clickOnText(downloads);
        solo.clickOnText(solo.getString(R.string.confirm_label));
        solo.waitForDialogToClose();
        List<String> hidden = UserPreferences.getHiddenDrawerItems();
        assertEquals(1, hidden.size());
        assertTrue(hidden.contains(DownloadsFragment.TAG));
    }

    /******** (opens homepage) ********/
    public void testOpenHomePageToplist() {
        //opens the homepage
        openNavDrawer();
        //clicks on the home button on the nav drawer
        solo.clickOnText(solo.getString(R.string.homepage_label));


        //wait for toplist fragment to load and scroll down the bottom
        solo.waitForView(android.R.id.list);

        //compares to see if the homepage label is the same as the page it's currently on
        assertEquals(solo.getString(R.string.homepage_label), getActionbarTitle());
    }

    /******** (stream from toplist) ********/
    public void testHomePageToplistStream() {
        //Navigate to home page
        openNavDrawer();
        solo.clickOnText(solo.getString(R.string.homepage_label));

        //wait for toplist fragment to load and scroll to bottom
        solo.waitForView(android.R.id.list);

        //open 2nd podcast in list view
        solo.clickInList(2);

        //Subscribe to podcast
        solo.waitForView(R.id.subscriptionLayout);
        solo.clickOnButton(solo.getString(R.string.subscribe_label));

        //Open podcast
        solo.waitForView(R.id.subscriptionLayout);
        solo.clickOnText(solo.getString(R.string.open_podcast));

        //open 3rd episode in list
        solo.clickInList(4);

        //stream episode
        solo.clickOnText(solo.getString(R.string.stream_label));
    }

    /******** (Download from toplist) ********/
    public void testHomePageTopListDownload() {
        //Navigate to home page
        openNavDrawer();
        solo.clickOnText(solo.getString(R.string.homepage_label));

        //wait for toplist fragment to load and scroll to bottom
        solo.waitForView(android.R.id.list);

        //open 1st podcast in list view
        solo.clickInList(1);

        //Subscribe to podcast
        solo.waitForView(R.id.subscriptionLayout);
        solo.clickOnButton(solo.getString(R.string.subscribe_label));

        //Open podcast
        solo.waitForView(R.id.subscriptionLayout);
        solo.clickOnText(solo.getString(R.string.open_podcast));

        //open 1st episode in list
        solo.clickInList(2);

        //Download episode
        solo.clickOnText(solo.getString(R.string.download_label));
    }

    public void testRandomEpisodeButton() {
        openNavDrawer();
        solo.clickOnText(solo.getString(R.string.homepage_label));

        solo.clickOnText(solo.getString(R.string.featured_tab));

        solo.waitForView(R.id.gridViewHome);

        //open 1st podcast in list view
        solo.clickInList(1);

        //Subscribe to podcast
        solo.waitForView(R.id.subscriptionLayout);
        solo.clickOnButton(solo.getString(R.string.subscribe_label));

        //Open podcast
        solo.waitForView(R.id.subscriptionLayout);
        solo.clickOnText(solo.getString(R.string.open_podcast));

        solo.waitForView(R.id.feedItemListHeader);
        solo.clickOnView(solo.getView(R.id.btnRandomEpisode));
    }

    public void testManageQueues() {
        //Navigate to queues page
        openNavDrawer();
        solo.clickOnText(solo.getString(R.string.queues_label));

        solo.waitForView(R.id.queueList);

        //Create new queue
        solo.clickOnView(solo.getView(R.id.addQueue));
        solo.waitForView(R.id.queue_name);
        solo.clickOnView(solo.getView(R.id.addQueue));
        solo.waitForView(R.id.queue_name);
        solo.clickOnView(solo.getView(R.id.addQueue));
        solo.waitForView(R.id.queue_name);

        //Delete queue
        solo.clickOnView(solo.getView(R.id.queue_delete_button));
        solo.waitForView(R.id.queue_name);

        //Access queue
        solo.clickOnView(solo.getView(R.id.queue_name));
        solo.waitForView(R.id.queue_fragment);
    }

    public void testAddToQueues(){

        //Navigate to queues page
        openNavDrawer();
        solo.clickOnText(solo.getString(R.string.queues_label));
        solo.waitForView(R.id.queueList);

        //Create new queue
        solo.clickOnView(solo.getView(R.id.addQueue));
        solo.enterText(0,"test");
        solo.clickOnText("Create");
        solo.waitForView(R.id.queue_name);

        //open nav bar
        openNavDrawer();

        //open search podcast page
        solo.clickOnText(solo.getString(R.string.add_feed_label));

        //Click on the "category" button to open the dialog
        solo.clickOnView(solo.getView(R.id.butCategorySearch));
        //select second in list
        solo.clickInList(2);
        solo.sleep(2000);
        solo.clickInList(2);
        solo.sleep(1000);
        //Subscribe to podcast
        solo.clickOnButton(solo.getString(R.string.subscribe_label));
        //Open podcast
        solo.waitForView(R.id.subscriptionLayout);
        solo.clickOnText(solo.getString(R.string.open_podcast));
        //open 1st episode in list
        solo.clickLongInList(2);
        solo.clickOnMenuItem(solo.getString(R.string.add_to_queue_label));
        solo.clickOnText(solo.getString(R.string.confirm));


        //Navigate to queues page
        openNavDrawer();
        solo.clickOnText(solo.getString(R.string.queues_label));
        solo.waitForView(R.id.queueList);
        solo.clickInList(0);
        solo.waitForView(R.id.queue_fragment);
        solo.goBack();

        //Delete queue
        solo.clickOnView(solo.getView(R.id.queue_delete_button));
        solo.clickOnButton(solo.getString(R.string.confirm));
        solo.waitForView(R.id.queue_name);

    }

  public void testCentralSearch() throws Exception{
        //Navigate to home page
        openNavDrawer();
        solo.clickOnText(solo.getString(R.string.homepage_label));

        solo.clickOnView(solo.getView(R.id.action_search));
        solo.enterText(0, "Joe Rogan");
        solo.pressSoftKeyboardSearchButton();

        solo.sleep(1000);

        ScrollView homeView = (ScrollView) solo.getView(R.id.homeScrollView);
        GridView searchResultView = (GridView) solo.getView(R.id.gridSearchResult);

        //Scroll down home page
        homeView.scrollTo(0, homeView.getHeight());
    }


    public void testCategorySearch() {
        //open nav bar
        openNavDrawer();

        //open search podcast page
        solo.clickOnText(solo.getString(R.string.add_feed_label));

        //Click on the "category" button to open the dialog
        solo.clickOnView(solo.getView(R.id.butCategorySearch));

        //select second in list
        solo.clickInList(2);

        solo.sleep(2000);


        solo.clickInList(2);

        solo.sleep(1000);

        //Subscribe to podcast
        solo.clickOnButton(solo.getString(R.string.subscribe_label));

        //Open podcast
        solo.waitForView(R.id.subscriptionLayout);
        solo.clickOnText(solo.getString(R.string.open_podcast));

        //open 1st episode in list
        solo.clickInList(2);

        //Download episode
        solo.clickOnText(solo.getString(R.string.download_label));
    }

    public void testRandomPodcast() {
        //Navigate to home page
        openNavDrawer();
        solo.clickOnText(solo.getString(R.string.homepage_label));

        //Check random podcast category - cancel
        solo.clickOnView(solo.getView(R.id.btnRandomPodcast));
        solo.clickOnText(solo.getString(R.string.cancel));

        //Check random podcast category - OK
        solo.clickOnView(solo.getView(R.id.btnRandomPodcast));
        solo.clickOnText(solo.getString(R.string.confirm));

        solo.waitForView(R.id.subscriptionLayout);
    }

    public void testStatisticsFragment() {
        // subscribe to a podcast
        openNavDrawer();
        solo.clickOnText(solo.getString(R.string.homepage_label));
        solo.clickOnText(solo.getString(R.string.featured_tab));
        solo.waitForView(R.id.gridViewHome);
        solo.clickInList(1);
        solo.waitForView(R.id.subscriptionLayout);
        solo.clickOnButton(solo.getString(R.string.subscribe_label));

        // look at it in the stats fragment
        // twice to press the back button, then the hamburger button
        openNavDrawer();
        openNavDrawer();
        solo.clickOnText(solo.getString(R.string.statistics_label));
        solo.waitForView(R.id.statistics_list);

        solo.clickInList(1);

        solo.clickOnText(solo.getString(android.R.string.ok));
    }

    //this is not testing the top list nor the suggested podcasts as they are
    //already tested in other UI tests
    public void testBHFragment() {

        //open nav bar
        openNavDrawer();

        //open search podcast page
        solo.clickOnText(solo.getString(R.string.homepage_label));

        solo.sleep(1000);

        //Swipe left
        solo.clickOnText(solo.getString(R.string.featured_tab));
        solo.sleep(500);
        //Swipe right
        solo.clickOnText(solo.getString(R.string.categories_tab));

        solo.sleep(500);

        //select second category in list
        solo.getView(R.id.cat_listview);
        solo.clickInList(2);

        solo.sleep(2000);

        solo.clickInList(2);

        solo.sleep(1000);

        //Subscribe to podcast
        solo.clickOnButton(solo.getString(R.string.subscribe_label));

        //Open podcast
        solo.waitForView(R.id.subscriptionLayout);
        solo.clickOnText(solo.getString(R.string.open_podcast));

        //open 1st episode in list
        solo.clickInList(2);

        //Download episode
        solo.clickOnText(solo.getString(R.string.download_label));
    }

    //Subscribes to a podcast then navigates to home page to see suggestion based on subscription(s)
    public void testSuggestedPodcast() {
        //Navigate to home page
        openNavDrawer();
        solo.clickOnText(solo.getString(R.string.homepage_label));

        //Check random podcast category - OK
        solo.clickOnView(solo.getView(R.id.btnRandomPodcast));
        solo.clickOnText(solo.getString(R.string.confirm));

        //Subscribe to podcast
        solo.waitForView(R.id.subscriptionLayout);
        solo.clickOnButton(solo.getString(R.string.subscribe_label));

        //Open podcast
        solo.waitForView(R.id.subscriptionLayout);
        solo.clickOnText(solo.getString(R.string.open_podcast));

        //Navigate to home page
        openNavDrawer();
        solo.clickOnText(solo.getString(R.string.homepage_label));

        solo.waitForView(R.id.suggestedPodcasts);
        GridView searchResultView = (GridView) solo.getView(R.id.gridViewHome);

        //Scroll down home page
        searchResultView.scrollTo(0, searchResultView.getHeight());
    }

    public void testResetStatistics() {
        // subscribe to a podcast
        openNavDrawer();
        solo.clickOnText(solo.getString(R.string.homepage_label));
        solo.clickOnText(solo.getString(R.string.featured_tab));
        solo.waitForView(R.id.gridViewHome);
        solo.clickInList(1);
        solo.waitForView(R.id.subscriptionLayout);
        solo.clickOnButton(solo.getString(R.string.subscribe_label));

        // twice to press the back button, then the hamburger button
        openNavDrawer();
        openNavDrawer();
        solo.clickOnText(solo.getString(R.string.statistics_label));
        solo.waitForView(R.id.statistics_list);

        solo.clickOnText(solo.getString(R.string.reset_all_statistics));
        solo.clickOnText(solo.getString(android.R.string.ok));
    }

}