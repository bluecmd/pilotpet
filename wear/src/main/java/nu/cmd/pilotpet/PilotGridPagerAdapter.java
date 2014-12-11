package nu.cmd.pilotpet;

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

    private final Context mContext;

    static final int[] BG_IMAGES = new int[]{
            R.drawable.ic_full_sad,
            R.drawable.ic_launcher,
            R.drawable.ic_full_sad,
            R.drawable.ic_launcher,
            R.drawable.ic_full_sad,
            R.drawable.ic_launcher
    };

    abstract class PilotCard {

        private Fragment mFragment;
        private boolean mDblClickToProgress;

        protected PilotCard(String category, String text, boolean dblClickToProgress) {
            mFragment = CardFragment.create(category, text);
            mDblClickToProgress = dblClickToProgress;
        }

        abstract protected void progress(GridViewPager pager);

        public Fragment getFragment() {
            return mFragment;
        }

        public boolean isDoubleClickToProgress() {
            return mDblClickToProgress;
        }
    }

    class ProgressDownCard extends PilotCard {
        protected ProgressDownCard(String category, String text) {
            super(category, text, true);
        }

        @Override
        protected void progress(GridViewPager pager) {
            Point point = pager.getCurrentItem();
            pager.setCurrentItem(point.y + 1, 0, true);
        }
    }

    class NoProgressCard extends PilotCard {
        protected NoProgressCard(String category, String text) {
            super(category, text, true);
        }

        @Override
        protected void progress(GridViewPager pager) {
            // No scroll
        }
    }

    class NoActionCard extends PilotCard {
        protected NoActionCard(String category, String text) {
            super(category, text, false);
        }

        @Override
        protected void progress(GridViewPager pager) {
        }
    }

    private final PilotCard[][] GRID;

    private void AddRow(List<PilotCard[]> grid, List<PilotCard> row) {
        PilotCard[] rowArray = new PilotCard[row.size()];
        row.toArray(rowArray);
        grid.add(rowArray);
    }


    public PilotGridPagerAdapter(Context ctx, FragmentManager fm) {
        super(fm);
        mContext = ctx;

        List<PilotCard[]> grid = new LinkedList<>();
        List<PilotCard> row;

        // Fuel
        row = new LinkedList<>();
        row.add(new NoActionCard("Pre-flight", "Fuel (Left)"));
        row.add(new NoActionCard("Pre-flight", "Fuel (Right)"));
        row.add(new NoActionCard("Pre-flight", "Fuel (Total)"));
        AddRow(grid, row);

        // Weight & Balance
        row = new LinkedList<>();
        row.add(new NoActionCard("Pre-flight", "Passenger (Front)"));
        row.add(new NoActionCard("Pre-flight", "Passenger (Back)"));
        row.add(new NoActionCard("Pre-flight", "Passenger (Back)"));
        row.add(new NoActionCard("Pre-flight", "Luggage"));
        row.add(new NoActionCard("Pre-flight", "Passenger (Pilot)"));
        AddRow(grid, row);

        // Pre-flight Summary
        row = new LinkedList<>();
        row.add(new NoActionCard("Pre-flight", "Summary"));
        AddRow(grid, row);

        // Pre Tacho
        row = new LinkedList<>();
        row.add(new NoActionCard("Pre-flight", "Tacho"));
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
        row.add(new NoActionCard("Parking", "Tacho"));
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
        return mContext.getResources().getDrawable(BG_IMAGES[row % BG_IMAGES.length]);
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