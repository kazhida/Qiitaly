package com.abplus.qiitaly.app;


import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import org.jetbrains.annotations.NotNull;

public class NavigationDrawerFragment extends Fragment {
    @InjectView(R.id.list_view)
    ListView screenList;
    @InjectView(R.id.logout_button)
    View logoutButton;

    public static interface Callback {
        void onItemSelected(int position);
        void onLogout();
    }
    private Callback callback;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ScreenAdapter adapter = new ScreenAdapter();
        screenList.setAdapter(adapter);
        screenList.setOnItemClickListener(adapter);
        screenList.setItemChecked(0, true);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NotNull View v) {
                if (callback != null) {
                    callback.onLogout();
                }
            }
        });
    }

    public void selectItem(int position) {
        if (screenList != null) {
            screenList.setItemChecked(position, true);
        }
        if (callback != null) {
            callback.onItemSelected(position);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            callback = (Callback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    private ActionBar getActionBar() {
        return getActivity().getActionBar();
    }

    private class ScreenAdapter extends ArrayAdapter<String> implements AdapterView.OnItemClickListener {

        ScreenAdapter() {
            super(
                    getActionBar().getThemedContext(),
                    android.R.layout.simple_list_item_activated_1,
                    android.R.id.text1,
                    new String[]{
                            getString(R.string.title_section1),
                            getString(R.string.title_section2),
                            getString(R.string.title_section3),
                    }
            );
        }

        @Override
        public void onItemClick(@NotNull AdapterView<?> parent, @NotNull View view, int position, long id) {
            selectItem(position);
        }
    }
}
