package com.jackz314.todo;

import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ClipboardFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ClipboardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ClipboardFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM = "param";

    // TODO: Rename and change types of parameters
    private String mParam;

    private OnFragmentInteractionListener mListener;

    public ClipboardFragment() {

        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ClipboardFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ClipboardFragment newInstance(int position) {
        ClipboardFragment fragment = new ClipboardFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM, position);
        fragment.setArguments(args);
        return fragment;
    }

    public interface OnClipboardBackPressedListener {
        void doBack();
    }

    public class ClipboardBackPressedListener implements OnClipboardBackPressedListener {
        private final FragmentActivity activity;

        public ClipboardBackPressedListener(FragmentActivity activity) {
            this.activity = activity;
        }

        @Override
        public void doBack() {//go back to main tab
            //activity.getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            ViewPager viewPager = getActivity().findViewById(R.id.pager);
            viewPager.setCurrentItem(1);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //if (getArguments() != null) {
        //    mParam = getArguments().getString(ARG_PARAM);
        //}
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ((MainActivity)getActivity()).setOnClipboardBackPressedListener(new ClipboardBackPressedListener(getActivity()){
            @Override
            public void doBack() {//handle back press here
                DrawerLayout drawer = getActivity().findViewById(R.id.drawer_layout);
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }else {
                    super.doBack();
                }
            }
        });

        return inflater.inflate(R.layout.fragment_clipboard, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final TextView clipboardText = view.findViewById(R.id.clipboard_text);
        final ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager != null && clipboardManager.getPrimaryClip() != null) {
            clipboardText.setText(clipboardManager.getPrimaryClip().getItemAt(0).getText().toString());
            clipboardManager.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
                @Override
                public void onPrimaryClipChanged() {
                    clipboardText.setText(clipboardManager.getPrimaryClip().getItemAt(0).getText().toString());
                }
            });
        }else {
            clipboardText.setText(getString(R.string.clipboard_is_empty));
        }

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
