/**
 *   Log-Fragment
 *
 */

package fllog;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.androidstudio.ubtrfcommserver.R;

// import com.example.androidstudio.hfragDebugLog.R;


public class LogFragment extends Fragment {

    private static final String TAG = "fhflLogFragment";

    private static final String SHARED_PREFERENCES_TAG = "LogFragment";
    private static final String SHARED_PREFERENCES_VIEW_CONTENT = "viewContent";
    private static final String SHARED_PREFERENCES_IS_SCROLLING = "isScrolling";
    private static final String SHARED_PREFERENCES_IS_SAVING_2_FILE = "isSaving2File";

     // the fragment initialization parameters
    private static final String ARG_PARAM1 = "Save2File";
    private static final String ARG_PARAM2 = "ShowTag";

    // Übegabeparameter
    public boolean isSaving2File = true;
    public boolean isShowingTag = true;

    private TextView tView;
    private ScrollView scrollView;

    private boolean isScrolling = true;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param Save2File Parameter 1
     * @param ShowTag Parameter 2
     * @return A new instance of fragment BasicLogFragment.
     */
    public static LogFragment newInstance(boolean Save2File, boolean ShowTag) {
        android.util.Log.d(TAG, "newInstance()");

        LogFragment fragment = new LogFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_PARAM1, Save2File);
        args.putBoolean(ARG_PARAM2, ShowTag);
        fragment.setArguments(args);
        return fragment;
    }

    public LogFragment() {
        // Required empty public constructor
        android.util.Log.d(TAG, "LogFragment(): empty public constructor");
    }

    // Alternative Initialisierung bei statischen Einbinden des Fragments via .xml
    public void init(boolean Save2File, boolean ShowTag){
        android.util.Log.d(TAG, "init( SaveToFile: " + Save2File + ", ShowTag: "  + ShowTag + " )");
        isSaving2File = Save2File;
        isShowingTag = ShowTag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.util.Log.d(TAG, "onCreate()");
        if (getArguments() != null) {
            android.util.Log.d(TAG, "onCreate(): getArguments() != null ");
            isSaving2File = getArguments().getBoolean(ARG_PARAM1, true);
            isShowingTag = getArguments().getBoolean(ARG_PARAM2, true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        android.util.Log.d(TAG, "onCreateView()");

        if ( savedInstanceState != null ){
            android.util.Log.d(TAG, "onCreateView(): savedInstanceState != null ");
        }
        else {
            android.util.Log.d(TAG, "onCreateView(): savedInstanceState == null ");
        }

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_log, container, false);

        tView = (TextView) view.findViewById(R.id.textScrollView);
        scrollView = (ScrollView) view.findViewById(R.id.scrollView);

        final Button butScrollStopStart = (Button)view.findViewById(R.id.butScrollStopStart);
        final Button butClear = (Button)view.findViewById(R.id.butClear);
        final Button butFileStopStart = (Button)view.findViewById(R.id.butFileStopStart);

        // State restaurieren
        android.util.Log.d(TAG, "onCreateView(): lade SharedPreferences");
        SharedPreferences sp = getActivity().getSharedPreferences(SHARED_PREFERENCES_TAG, 0);
        String str = sp.getString(SHARED_PREFERENCES_VIEW_CONTENT, "Shared Preferences nicht initialisiert\n");
        isScrolling = sp.getBoolean(SHARED_PREFERENCES_IS_SCROLLING, true);
        isSaving2File = sp.getBoolean(SHARED_PREFERENCES_IS_SAVING_2_FILE, false);
        setTextView(str);
        setButtonColor(butScrollStopStart, isScrolling);
        setButtonColor(butFileStopStart, isSaving2File);

        butScrollStopStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                android.util.Log.d(TAG, "butStopStart.onClick():  ");
                if ( isScrolling ){
                    isScrolling = false;
                }
                else {
                    isScrolling = true;
                    scrollView.scrollTo(0, tView.getBottom());          // und scrollen
                }
                setButtonColor(butScrollStopStart, isScrolling);
            }
        });

        butFileStopStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                android.util.Log.d(TAG, "butStopStart.onClick():  ");
                if ( isSaving2File ){
                    isSaving2File = false;
                }
                else {
                    isSaving2File = true;
                }
                setButtonColor(butFileStopStart, isSaving2File);
            }
        });

        butClear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                android.util.Log.d(TAG, "butClear.onClick():  ");
                tView.setText("");
            }
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        android.util.Log.d(TAG, "onPause()");
        SharedPreferences sp = getActivity().getSharedPreferences(SHARED_PREFERENCES_TAG, 0);

        SharedPreferences.Editor ed = sp.edit();
        ed.putString(SHARED_PREFERENCES_VIEW_CONTENT, tView.getText().toString());
        ed.putBoolean(SHARED_PREFERENCES_IS_SCROLLING, isScrolling);
        ed.putBoolean(SHARED_PREFERENCES_IS_SAVING_2_FILE, isSaving2File);

        ed.commit();
    }

    public void setTextView(String str){
        tView.append(str);
        // tView.invalidate();

        if ( isScrolling ) {

            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
        }
    }

    private void setButtonColor(Button but, boolean bol) {
        if ( bol ){
            but.setBackgroundColor(0xFF00FF00);    // alpha, r, g, b
        }
        else {
            but.setBackgroundColor(0xFFFF0000);    // alpha, r, g, b
        }
    }
}

