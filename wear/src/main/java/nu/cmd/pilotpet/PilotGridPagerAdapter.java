package nu.cmd.pilotpet;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;

import java.util.LinkedList;
import java.util.List;

public class PilotGridPagerAdapter extends FragmentGridPagerAdapter {

    private final Activity mActivity;

    abstract class PilotCard {

        private Fragment mFragment;
        private boolean mDblClickToProgress;

        protected PilotCard(Fragment fragment, boolean dblClickToProgress) {
            mFragment = fragment;
            mDblClickToProgress = dblClickToProgress;
        }

        protected void progress(GridViewPager pager) {
        }

        public Fragment getFragment() {
            return mFragment;
        }

        public boolean isDoubleClickToProgress() {
            return mDblClickToProgress;
        }
    }

    abstract class TextCard extends PilotCard {
        protected TextCard(String category, String text, boolean dblClickToProgress) {
            super(CardFragment.create(category, text), dblClickToProgress);
        }
    }

    class ProgressDownCard extends TextCard {
        protected ProgressDownCard(String category, String text) {
            super(category, text, true);
        }

        @Override
        protected void progress(GridViewPager pager) {
            Point point = pager.getCurrentItem();
            pager.setCurrentItem(point.y + 1, 0, true);
        }
    }

    class NoProgressCard extends TextCard {
        protected NoProgressCard(String category, String text) {
            super(category, text, true);
        }
    }

    class NoActionCard extends TextCard {
        protected NoActionCard(String category, String text) {
            super(category, text, false);
        }
    }

    class FuelNumberCard extends PilotCard {
        protected FuelNumberCard(String category, String text, int def) {
            super(NumberFragment.newInstance(text, 1, 50, 0, def), false);
        }
    }

    class OilNumberCard extends PilotCard {
        protected OilNumberCard(String category, String text, int def) {
            super(NumberFragment.newInstance(text, 1, 9, 0, def), false);
        }
    }

    class TachoNumberCard extends PilotCard {
        protected TachoNumberCard(String category, String text) {
            super(NumberFragment.newInstance(text, 3, 99, 0, 0), false);
        }
    }

    class WeightNumberCard extends PilotCard {
        protected WeightNumberCard(String category, String text) {
            super(NumberFragment.newInstance(text, 1, 150, 0, 80), false);
        }
        protected WeightNumberCard(String category, String text, int def) {
            super(NumberFragment.newInstance(text, 1, 150, 0, def), false);
        }
    }

    private final PilotCard[][] GRID;

    private void AddRow(List<PilotCard[]> grid, List<PilotCard> row) {
        PilotCard[] rowArray = new PilotCard[row.size()];
        row.toArray(rowArray);
        grid.add(rowArray);
    }

    public PilotGridPagerAdapter(Activity activity, FragmentManager fm) {
        super(fm);
        mActivity = activity;

        List<PilotCard[]> grid = new LinkedList<>();
        List<PilotCard> row;

        // Fuel
        row = new LinkedList<>();
        row.add(new FuelNumberCard("Pre-flight", "Fuel (Left Tank)", 20));
        row.add(new FuelNumberCard("Pre-flight", "Fuel (Right Tank)", 20));
        row.add(new FuelNumberCard("Pre-flight", "Fuel (Lifted)", 0));
        row.add(new OilNumberCard("Pre-flight", "Oil", 6));
        row.add(new OilNumberCard("Pre-flight", "Oil (Lifted)", 0));
        AddRow(grid, row);

        row = new LinkedList<>();
        row.add(new NoActionCard("Pre-flight", "Fuel Summary"));
        AddRow(grid, row);

        // Weight & Balance
        row = new LinkedList<>();
        row.add(new WeightNumberCard("Pre-flight", "Passenger (Front)"));
        row.add(new WeightNumberCard("Pre-flight", "Passenger (Back)", 0));
        row.add(new WeightNumberCard("Pre-flight", "Passenger (Back)", 0));
        row.add(new WeightNumberCard("Pre-flight", "Luggage", 0));
        row.add(new WeightNumberCard("Pre-flight", "Passenger (Pilot)"));
        AddRow(grid, row);

        // Pre-flight Summary
        row = new LinkedList<>();
        row.add(new NoActionCard("Pre-flight", "Pre-flight Summary"));
        AddRow(grid, row);

        // Pre Tacho
        row = new LinkedList<>();
        row.add(new TachoNumberCard("Pre-flight", "Tacho"));
        AddRow(grid, row);

        // Start Taxi
        row = new LinkedList<>();
        row.add(new ProgressDownCard("Start Taxi", "Double tap to mark as done"));
        AddRow(grid, row);

        // Take Off
        row = new LinkedList<>();
        row.add(new ProgressDownCard("Take Off", "Double tap to mark as done"));
        AddRow(grid, row);

        // Waypoint
        row = new LinkedList<>();
        row.add(new NoProgressCard("Waypoint", "Double tap to mark"));
        AddRow(grid, row);

        // Landing
        row = new LinkedList<>();
        row.add(new ProgressDownCard("Landing", "Double tap to mark as done"));
        AddRow(grid, row);

        // Parking
        row = new LinkedList<>();
        row.add(new ProgressDownCard("Parking", "Double tap to mark as done"));
        AddRow(grid, row);

        // Post Tacho
        row = new LinkedList<>();
        row.add(new TachoNumberCard("Parking", "Tacho"));
        AddRow(grid, row);

        PilotCard[][] gridArray = new PilotCard[grid.size()][];
        grid.toArray(gridArray);
        GRID = gridArray;
    }

    @Override
    public Fragment getFragment(int row, int col) {
        return GRID[row][col].getFragment();
    }

    @Override
    public int getRowCount() {
        return GRID.length;
    }

    @Override
    public int getColumnCount(int row) {
        return GRID[row].length;
    }

    @Override
    public Drawable getBackgroundForPage(int row, int column) {
        return super.getBackgroundForPage(row, column);
    }

    public void next(GridViewPager pager) {
        Point point = pager.getCurrentItem();
        GRID[point.y][point.x].progress(pager);
    }

    public boolean isDoubleClickToProgress(int row, int column) {
        return GRID[row][column].isDoubleClickToProgress();
    }

    public void reset(GridViewPager pager) {
        pager.setCurrentItem(0, 0);
    }
}