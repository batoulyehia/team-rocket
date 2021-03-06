package de.danoeh.antennapod.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.danoeh.antennapod.R;
import de.danoeh.antennapod.activity.MainActivity;

//This fragment is the base fragment of the Home Page. The FeaturedFragment and CategoriesFragment will be
//loaded in the tabs contained in the BaseHomeFragment
public class HomeFragment extends Fragment{

    public static final String TAG = "BaseHomeFragment";

    private static final String LAST_TAB = "tab_position";

    private SharedPreferences prefs;

    //positions of the tabs
    public static final int POS_FEATURED = 0;
    public static final int POS_CATEGORIES = 1;
    public static final int TOTAL = 2;

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private MainActivity act;

    public void setAct(MainActivity mainActivity){
        this.act = mainActivity;
    }
    public HomeFragment(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);
        this.setAct((MainActivity) getActivity());
        act.getSupportActionBar().setTitle(R.string.homepage_label);

        View rootView = inflater.inflate(R.layout.home_fragment, container, false);
        viewPager = (ViewPager)rootView.findViewById(R.id.home_viewpager);
        viewPager.setAdapter(new HomePagerAdapter(getChildFragmentManager(), getResources()));

        // Give the tabs layout the viewpager
        tabLayout = (TabLayout) rootView.findViewById(R.id.home_tabs);
        tabLayout.setupWithViewPager(viewPager);

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        // save the tab selection
        prefs = getActivity().getSharedPreferences(TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(LAST_TAB, tabLayout.getSelectedTabPosition());
        editor.apply();
    }

    @Override
    public void onStart() {
        super.onStart();
        // restore our last position
        prefs = getActivity().getSharedPreferences(TAG, Context.MODE_PRIVATE);
        int lastPosition = prefs.getInt(LAST_TAB, 0);
        viewPager.setCurrentItem(lastPosition);
    }


    //HomePagerAdapter is a static inner class of HomeFragment
    public static class HomePagerAdapter extends FragmentPagerAdapter {

        private final Resources resources; //it is required in order to have the constructor
        private Fragment[] fragments = {
                new FeaturedFragment(),
                new CategoriesFragment(),
        };

        //constructor
        public HomePagerAdapter(FragmentManager fm, Resources resources) {
            super(fm);
            this.resources = resources;
        }

        //get a certain fragment from the array of Fragment
        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        //get the numbers of tabs
        @Override
        public int getCount() {
            return TOTAL;
        }

        //get title of each tabs
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case POS_FEATURED:
                    return resources.getString(R.string.featured_tab);
                case POS_CATEGORIES:
                    return resources.getString(R.string.categories_tab);
                default:
                    return super.getPageTitle(position);
            }
        }
    }
}
