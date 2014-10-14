package blackman.matt.ochoseer;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BoardListCard.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BoardListCard#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class BoardListCard extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_BOARD_NAME = "board_name";
    private static final String ARG_BOARD_LINK = "board_link";
    private static final String ARG_BOARD_FAVORITED = "board_favorited";
    private static final String ARG_BOARD_NATIONAILITY = "board_nation";
    private static final String ARG_BOARD_VALUE = "board_selector";

    // TODO: Rename and change types of parameters
    private String mBoardName;
    private String mBoardLink;
    private String mBoardValue;
    private String mBoardNation;
    private int mBoardFavorited;

    private CompoundButton.OnCheckedChangeListener mCheckChangedListener;
    private ToggleButton mFavoriteButton;
    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BoardListCard.
     */
    // TODO: Rename and change types and number of parameters
    public static BoardListCard newInstance(String boardName, String boardLink, String value,
                                            String nation, int favorited) {
        BoardListCard fragment = new BoardListCard();
        Bundle args = new Bundle();
        args.putString(ARG_BOARD_NAME, boardName);
        args.putString(ARG_BOARD_LINK, boardLink);
        args.putString(ARG_BOARD_VALUE, value);
        args.putString(ARG_BOARD_NATIONAILITY, nation);
        args.putInt(ARG_BOARD_FAVORITED, favorited);
        fragment.setArguments(args);
        return fragment;
    }
    public BoardListCard() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mBoardName = getArguments().getString(ARG_BOARD_NAME);
            mBoardLink = getArguments().getString(ARG_BOARD_LINK);
            mBoardValue = getArguments().getString(ARG_BOARD_VALUE);
            mBoardNation = getArguments().getString(ARG_BOARD_NATIONAILITY);
            mBoardFavorited = getArguments().getInt(ARG_BOARD_FAVORITED);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View listView = inflater.inflate(R.layout.fragment_board_list_card, container, false);
        TextView tv_name = (TextView) listView.findViewById(R.id.tv_board_name);
        TextView tv_link = (TextView) listView.findViewById(R.id.tv_board_link);
        TextView tv_value = (TextView) listView.findViewById(R.id.tv_board_value);
        ToggleButton tb_fav = (ToggleButton) listView.findViewById(R.id.tb_board_fav);

        tv_name.setText(mBoardName);
        tv_link.setText(mBoardLink);
        tv_value.setText(mBoardValue);
        tb_fav.setChecked(mBoardFavorited == 1 ? Boolean.TRUE : Boolean.FALSE);

        ((ToggleButton) listView.findViewById(R.id.tb_board_fav)).setOnCheckedChangeListener(mCheckChangedListener);

        return listView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
        public void onFragmentInteraction(Uri uri);
    }

    public void saveOnChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        this.mCheckChangedListener = listener;
    }
}
