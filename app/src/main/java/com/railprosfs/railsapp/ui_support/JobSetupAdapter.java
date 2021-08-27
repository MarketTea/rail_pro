package com.railprosfs.railsapp.ui_support;

import android.content.Context;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.railprosfs.railsapp.R;
import com.railprosfs.railsapp.data.dto.Actor;
import com.railprosfs.railsapp.data.observable.JobSetupAnswer;
import com.railprosfs.railsapp.data_layout.AnswerTbl;
import com.railprosfs.railsapp.data_layout.AssignmentTbl;
import com.railprosfs.railsapp.data_layout.FieldPlacementTbl;
import com.railprosfs.railsapp.databinding.ChecboxRowBinding;
import com.railprosfs.railsapp.databinding.DatePickerRowBinding;
import com.railprosfs.railsapp.databinding.FireWaterRowPickerBinding;
import com.railprosfs.railsapp.databinding.GoodFairPoorRowPickerBinding;
import com.railprosfs.railsapp.databinding.HeaderRowBinding;
import com.railprosfs.railsapp.databinding.HeaderSubRowBinding;
import com.railprosfs.railsapp.databinding.MainOrSidingRowPickerBinding;
import com.railprosfs.railsapp.databinding.PipeOrWireRowPickerBinding;
import com.railprosfs.railsapp.databinding.SignaturePickerRowBinding;
import com.railprosfs.railsapp.databinding.StateNameRowPickerBinding;
import com.railprosfs.railsapp.databinding.UserInputCityRowBinding;
import com.railprosfs.railsapp.databinding.UserInputIntRowBinding;
import com.railprosfs.railsapp.databinding.UserInputMapRowBinding;
import com.railprosfs.railsapp.databinding.UserInputMultilineRowBinding;
import com.railprosfs.railsapp.databinding.UserInputPhoneRowBinding;
import com.railprosfs.railsapp.databinding.UserInputRealRowBinding;
import com.railprosfs.railsapp.databinding.UserInputRowBinding;
import com.railprosfs.railsapp.utility.ExpClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.railprosfs.railsapp.utility.Constants.FIELD_JSF_JOBNBR;
import static com.railprosfs.railsapp.utility.Constants.FIELD_JSF_RWIC_DATE;
import static com.railprosfs.railsapp.utility.Constants.FIELD_JSF_SIGN_RWIC;

public class JobSetupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final String HEADER = "FormHeader";
    private final String SUBHEADER = "FormSubHeader";
    public static final String INPUTTEXT = "UserInput";
    public static final String INPUTMULTILINE = "MultiLine";
    public static final String INPUTCITY = "36";
    public static final String INPUTCITY2 = "UserInputAllFirstCapital";
    public static final String REALLIST = "RealList";
    public static final int UI_GROUP_STATE_NAME = 10;
    public static final int UI_GROUP_MAPPING = 20;
    public static final int UI_GROUP_CITY = 21;
    public static final int UI_GROUP_MULTILINE = 30;
    public static final String CHECKBOX = "YesNo";
    public static final String INTEGERTEXT = "Integer";
    public static final String DECIMAL = "Decimal2";
    private static final String PHONE = "Phone";
    private static final String ODDTYPE_POC = "DB_PointOfContactPhone_editable";
    public static final String SIGNATURE = "Signature";
    public static final String DATE = "Date";
    private static final String PICKONE = "PickOne";
    private static final String GPS = "GpsCoordinates";
    private static final String PIPEORWIRE = "PIPEORWIRE";
    private static final String FIREWATER = "FIREWATER";
    private static final String GOODFAIRPOOR = "GOODFAIRPOOR";
    private static final String MAINORSIDING = "MAINORSIDING";
    private static final String MAINORSLIDING = "MAINORSLIDING"; /* Need to support both for a time.  Once everyone on this version or above, can remove from backend (and then here). */
    private static final String USASTATES = "SOLE_USASTATES";

    private LayoutInflater layoutInflater;
    private List<FieldPlacementTbl> mJobSetupQuestions;
    private List<JobSetupAnswer> mJobSetupAnswers;
    private final Actor user;
    private boolean editable = true;
    private final PhoneNumberFormattingTextWatcher phoneFormatter = new PhoneNumberFormattingTextWatcher();


    public JobSetupAdapter(Context ctx) {
        mJobSetupQuestions = new ArrayList<>();
        mJobSetupAnswers = new ArrayList<>();
        user = new Actor(ctx);
    }

    public void RefreshJobSetupData(List<FieldPlacementTbl> data) {
        if (data != null && data.size() > 0) {
            mJobSetupQuestions = data;
            initializeAnswers(data);
            notifyDataSetChanged();
        }
    }

    public void RefreshDataContent(List<AnswerTbl> answers) {
        if(answers != null && mJobSetupAnswers != null) {
            if (answers.size() == mJobSetupAnswers.size()) {
                for (int i = 0; i < answers.size(); i++) {
                    //Fill Answers
                    JobSetupAnswer temp = mJobSetupAnswers.get(i);
                    AnswerTbl ans = answers.get(i);
                    temp.setUserInput(ans.CommentResponse);
                    temp.setYesNo(ans.yesNo);
                    temp.setDate(ans.date);
                    temp.setSignatureImage(ans.signatureFileName, 0); // Id not used at all
                }
            }
        }
    }

    public void setEnabled(boolean enable) {
        this.editable = enable;
        notifyDataSetChanged();
    }

    // Check if the Input Fields are Required and if so Check if they have something inputed
    public boolean checkValidation() {
        boolean validation = true;
        for (int i = 0; i < mJobSetupQuestions.size(); i++) {
            FieldPlacementTbl question = mJobSetupQuestions.get(i);
            JobSetupAnswer answer = mJobSetupAnswers.get(i);
            if (question.Required) {
                // Empty Input Do Something To that Position
                if (question.FieldType.equals(INPUTTEXT) && (answer.getUserInput() == null || answer.getUserInput().equals(""))) {
                    answer.setInvalid(true);
                    validation = false;
                } else if (question.FieldType.equals(INPUTMULTILINE) && (answer.getUserInput() == null || answer.getUserInput().equals(""))) {
                    answer.setInvalid(true);
                    validation = false;
                } else if ((question.FieldType.equals(PHONE) || question.FieldType.equals(ODDTYPE_POC))
                        && (answer.getUserInput() == null || answer.getUserInput().equals(""))) {
                    answer.setInvalid(true);
                    validation = false;
                } else if (question.FieldType.equals(DATE) && (answer.getDate() == null || answer.getDate().equals(""))) {
                    answer.setInvalid(true);
                    validation = false;
                } else if (question.FieldType.equals(SIGNATURE) && (answer.getSignatureFileName() == null || answer.getSignatureFileName().equals(""))) {
                    answer.setInvalid(true);
                    validation = false;
                } else if(question.FieldType.equals(CHECKBOX) && (answer.isYesNo())) {
                    /* nothing to do at the moment */
                } else if((answer.getUserInput() == null || answer.getUserInput().length() == 0)
                        && (answer.getDate() == null || answer.getDate().length() == 0)
                        && (answer.getSignatureFileName() == null || answer.getSignatureFileName().length() == 0)) {
                        answer.setInvalid(true);
                        validation = false;
                }
            }
        }
        return validation;
    }

    private void initializeAnswers(List<FieldPlacementTbl> tbl) {
        if(mJobSetupAnswers == null || mJobSetupAnswers.size() == 0) {
            mJobSetupAnswers = new ArrayList<>();
            for (int i = 0; i < tbl.size(); i++) {
                // The FieldId is the backend database id, it is the best (most stable)
                // key to use when linking answers to questions as we are doing here.
                JobSetupAnswer temp = new JobSetupAnswer(tbl.get(i).FieldId);
                switch (mJobSetupQuestions.get(i).FieldPrompt) {
                    case "Signature":
                        temp.specialQuestionID = FIELD_JSF_SIGN_RWIC;
                        break;
                    case "Signature Date":
                        temp.specialQuestionID = FIELD_JSF_RWIC_DATE;
                        break;
                    case "Job Number":
                        temp.specialQuestionID = FIELD_JSF_JOBNBR;
                        break;
                }
                mJobSetupAnswers.add(temp);
            }
        }
    }

    public void setAnswers(List<JobSetupAnswer> answerList) {
        mJobSetupAnswers = answerList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.getContext());
        }
        switch (viewType) {
            case R.layout.header_row:
                HeaderRowBinding hrb = DataBindingUtil.inflate(layoutInflater, R.layout.header_row, parent, false);
                return new HeaderViewHolder(hrb);
            case R.layout.header_sub_row:
                HeaderSubRowBinding hsrb = DataBindingUtil.inflate(layoutInflater, R.layout.header_sub_row, parent, false);
                return new HeaderSubViewHolder(hsrb);
            case R.layout.user_input_row:
                UserInputRowBinding uirb = DataBindingUtil.inflate(layoutInflater, R.layout.user_input_row, parent, false);
                return new UserInputViewHolder(uirb);
            case R.layout.checbox_row:
                ChecboxRowBinding cbrb = DataBindingUtil.inflate(layoutInflater, R.layout.checbox_row, parent, false);
                return new CheckBoxViewHolder(cbrb);
            case R.layout.date_picker_row:
                DatePickerRowBinding dprb = DataBindingUtil.inflate(layoutInflater, R.layout.date_picker_row, parent, false);
                return new DatePickerViewHolder(dprb);
            case R.layout.signature_picker_row:
                SignaturePickerRowBinding sprb = DataBindingUtil.inflate(layoutInflater, R.layout.signature_picker_row, parent, false);
                return new SignaturePickerViewHolder(sprb);
            case R.layout.user_input_int_row:
                UserInputIntRowBinding uiirb = DataBindingUtil.inflate(layoutInflater, R.layout.user_input_int_row, parent, false);
                return new IntegerViewHolder(uiirb);
            case R.layout.user_input_real_row:
                UserInputRealRowBinding uirrb = DataBindingUtil.inflate(layoutInflater, R.layout.user_input_real_row, parent, false);
                return new RealViewHolder(uirrb);
            case R.layout.user_input_multiline_row:
                UserInputMultilineRowBinding uimrb = DataBindingUtil.inflate(layoutInflater, R.layout.user_input_multiline_row, parent, false);
                return new MultilineViewHolder(uimrb);
            case R.layout.user_input_city_row:
                UserInputCityRowBinding uicrb = DataBindingUtil.inflate(layoutInflater, R.layout.user_input_city_row, parent, false);
                return new CityViewHolder(uicrb);
            case R.layout.user_input_phone_row:
                UserInputPhoneRowBinding uiprb = DataBindingUtil.inflate(layoutInflater, R.layout.user_input_phone_row, parent, false);
                return new PhoneViewHolder(uiprb);
            case R.layout.user_input_map_row:
                UserInputMapRowBinding uimaprb = DataBindingUtil.inflate(layoutInflater, R.layout.user_input_map_row, parent, false);
                return new MapViewHolder(uimaprb);
            case R.layout.good_fair_poor_row_picker:
                GoodFairPoorRowPickerBinding gfprb = DataBindingUtil.inflate(layoutInflater, R.layout.good_fair_poor_row_picker, parent, false);
                return new GoodFairPoorViewHolder(gfprb);
            case R.layout.main_or_siding_row_picker:
                MainOrSidingRowPickerBinding mosrb = DataBindingUtil.inflate(layoutInflater, R.layout.main_or_siding_row_picker, parent, false);
                return new MainOrSidingViewHolder(mosrb);
            case R.layout.pipe_or_wire_row_picker:
                PipeOrWireRowPickerBinding powrb = DataBindingUtil.inflate(layoutInflater, R.layout.pipe_or_wire_row_picker, parent, false);
                return new PipeOrWireViewHolder(powrb);
            case R.layout.fire_water_row_picker:
                FireWaterRowPickerBinding fwrb = DataBindingUtil.inflate(layoutInflater, R.layout.fire_water_row_picker, parent, false);
                return new FireWaterViewHolder(fwrb);
            case R.layout.state_name_row_picker:
                StateNameRowPickerBinding snrb = DataBindingUtil.inflate(layoutInflater, R.layout.state_name_row_picker, parent, false);
                return new StateNameViewHolder(snrb);
            default:
                return new EmptyViewHolder(layoutInflater.inflate(R.layout.empty_row, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        if(mJobSetupQuestions == null || mJobSetupAnswers == null) { return; /* if null we can skip it I guess. */ }
        FieldPlacementTbl tbl = mJobSetupQuestions.get(position);
        final JobSetupAnswer answ = mJobSetupAnswers.get(position);
        answ.setViewVisible(editable);

        if (tbl.FieldType.equals(HEADER)) {
            ((HeaderViewHolder) holder).binding.setField(tbl);
        } else if (tbl.FieldType.equals(SUBHEADER)) {
            ((HeaderSubViewHolder) holder).binding.setField(tbl);
        } else if (tbl.FieldType.equals(INPUTTEXT)) {
            switch (tbl.Group) {
                case UI_GROUP_STATE_NAME:
                    ((StateNameViewHolder) holder).binding.setField(tbl);
                    ((StateNameViewHolder) holder).binding.setAnswer(answ);
                    break;
                case UI_GROUP_MULTILINE:
                    ((MultilineViewHolder) holder).binding.setField(tbl);
                    ((MultilineViewHolder) holder).binding.setAnswer(answ);
                    break;
                case UI_GROUP_CITY:
                    ((CityViewHolder) holder).binding.setField(tbl);
                    ((CityViewHolder) holder).binding.setAnswer(answ);
                    break;
                case UI_GROUP_MAPPING:
                    ((MapViewHolder) holder).binding.setField(tbl);
                    ((MapViewHolder) holder).binding.setAnswer(answ);
                    break;
                default:
                    ((UserInputViewHolder) holder).binding.setField(tbl);
                    ((UserInputViewHolder) holder).binding.setAnswer(answ);
                    break;
            }
        } else if (tbl.FieldType.equals(PHONE) || tbl.FieldType.equals(ODDTYPE_POC)) {
            ((PhoneViewHolder) holder).binding.setField(tbl);
            ((PhoneViewHolder) holder).binding.setAnswer(answ);
            if(((PhoneViewHolder) holder).binding.inputPhone.getTag() == null) {
                ((PhoneViewHolder) holder).binding.inputPhone.setTag(PHONE);
                ((PhoneViewHolder) holder).binding.inputPhone.addTextChangedListener(phoneFormatter);
            }
        } else if (tbl.FieldType.equals(CHECKBOX)) {
            ((CheckBoxViewHolder) holder).binding.setField(tbl);
            ((CheckBoxViewHolder) holder).binding.setAnswer(answ);
            if(tbl.Group > 1000) {
                final int group = tbl.Group;
                ((CheckBoxViewHolder) holder).binding.checkboxItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toggleRequired(answ.isYesNo(), group);
                    }
                });
            }
        } else if (tbl.FieldType.equals(REALLIST)) {
            if (tbl.Code.equalsIgnoreCase(USASTATES)){
                ((StateNameViewHolder) holder).binding.setField(tbl);
                ((StateNameViewHolder) holder).binding.setAnswer(answ);
            }
        } else if (tbl.FieldType.equals(PICKONE) && tbl.Code.equals(GOODFAIRPOOR)) {
            ((GoodFairPoorViewHolder) holder).binding.setField(tbl);
            ((GoodFairPoorViewHolder) holder).binding.setAnswer(answ);
        } else if (tbl.FieldType.equals(PICKONE) && tbl.Code.equals(PIPEORWIRE)) {
            ((PipeOrWireViewHolder) holder).binding.setField(tbl);
            ((PipeOrWireViewHolder) holder).binding.setAnswer(answ);
        } else if (tbl.FieldType.equals(PICKONE) && tbl.Code.equals(FIREWATER)) {
            ((FireWaterViewHolder) holder).binding.setField(tbl);
            ((FireWaterViewHolder) holder).binding.setAnswer(answ);
        } else if (tbl.FieldType.equals(PICKONE) && tbl.Code.equals(MAINORSIDING)) {
            ((MainOrSidingViewHolder) holder).binding.setField(tbl);
            ((MainOrSidingViewHolder) holder).binding.setAnswer(answ);
        } else if (tbl.FieldType.equals(PICKONE) && tbl.Code.equals(MAINORSLIDING)) {
            ((MainOrSidingViewHolder) holder).binding.setField(tbl);
            ((MainOrSidingViewHolder) holder).binding.setAnswer(answ);
        } else if (tbl.FieldType.equals(DATE)) {
            ((DatePickerViewHolder) holder).binding.setField(tbl);
            ((DatePickerViewHolder) holder).binding.setAnswer(answ);
        } else if (tbl.FieldType.equals(SIGNATURE)) {
            ((SignaturePickerViewHolder) holder).binding.setField(tbl);
            ((SignaturePickerViewHolder) holder).binding.setAnswer(answ);
        } else if(tbl.FieldType.equals(INTEGERTEXT)) {
            ((IntegerViewHolder) holder).binding.setField(tbl);
            ((IntegerViewHolder) holder).binding.setAnswer(answ);
        } else if(tbl.FieldType.equals(DECIMAL)) {
            ((RealViewHolder) holder).binding.setField(tbl);
            ((RealViewHolder) holder).binding.setAnswer(answ);
        } else if(tbl.FieldType.equals(GPS)) {
            ((MapViewHolder) holder).binding.setField(tbl);
            ((MapViewHolder) holder).binding.setAnswer(answ);
        } else if(tbl.FieldType.equals(INPUTMULTILINE)) {
            ((MultilineViewHolder) holder).binding.setField(tbl);
            ((MultilineViewHolder) holder).binding.setAnswer(answ);
        } else if(tbl.FieldType.equals(INPUTCITY) || tbl.FieldType.equals(INPUTCITY2)) {
            ((CityViewHolder) holder).binding.setField(tbl);
            ((CityViewHolder) holder).binding.setAnswer(answ);
        }
        else {
            if (tbl.Group == 0) {
                ((UserInputViewHolder) holder).binding.setField(tbl);
                ((UserInputViewHolder) holder).binding.setAnswer(answ);
            } else if (tbl.Group == 6) {
                ((DatePickerViewHolder) holder).binding.setField(tbl);
                ((DatePickerViewHolder) holder).binding.setAnswer(answ);
            }
        }

    }

    public List<JobSetupAnswer> getmJobSetupAnswers() {
        return mJobSetupAnswers;
    }

    @Override
    public int getItemCount() {
        return mJobSetupQuestions.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= mJobSetupQuestions.size()) {
            return R.layout.empty_row;
        } else {
            String type = mJobSetupQuestions.get(position).FieldType;

            switch (type) {
                case HEADER:
                    return R.layout.header_row;
                case SUBHEADER:
                    return R.layout.header_sub_row;
                case INPUTTEXT:
                    switch (mJobSetupQuestions.get(position).Group) {
                        case UI_GROUP_STATE_NAME:
                            return R.layout.state_name_row_picker;
                        case UI_GROUP_MULTILINE:
                            return R.layout.user_input_multiline_row;
                        case UI_GROUP_CITY:
                            return R.layout.user_input_city_row;
                        case UI_GROUP_MAPPING:
                            return R.layout.user_input_map_row;
                        default:
                            return R.layout.user_input_row;
                    }
                case PHONE:
                case ODDTYPE_POC:
                    return R.layout.user_input_phone_row;
                case CHECKBOX:
                    return R.layout.checbox_row;
                case REALLIST:
                    if(mJobSetupQuestions.get(position).Code.equalsIgnoreCase(USASTATES)) {
                        return R.layout.state_name_row_picker;
                    }
                case DATE:
                    return R.layout.date_picker_row;
                case SIGNATURE:
                    return R.layout.signature_picker_row;
                case PICKONE:
                    switch (mJobSetupQuestions.get(position).Code) {
                        case GOODFAIRPOOR:
                            return R.layout.good_fair_poor_row_picker;
                        case PIPEORWIRE:
                            return R.layout.pipe_or_wire_row_picker;
                        case FIREWATER:
                            return R.layout.fire_water_row_picker;
                        case MAINORSIDING:
                            return R.layout.main_or_siding_row_picker;
                        case MAINORSLIDING:
                            return R.layout.main_or_siding_row_picker;
                        default:
                            return R.layout.empty_row;
                    }
                case INTEGERTEXT:
                    return R.layout.user_input_int_row;
                case DECIMAL:
                    return R.layout.user_input_real_row;
                case GPS:
                    return R.layout.user_input_map_row;
                case INPUTMULTILINE:
                    return R.layout.user_input_multiline_row;
                case INPUTCITY:
                case INPUTCITY2:
                    return R.layout.user_input_city_row;
                default:
                    // This manages non-type db names that sometimes come down as a type.  Need to look at the group to find the real type.
                    switch (mJobSetupQuestions.get(position).Group) {
                        case 0:
                            return R.layout.user_input_row;
                        case 6:
                            return R.layout.date_picker_row;
                    }
                    return R.layout.empty_row;
            }
        }
    }

    public List<JobSetupAnswer> getAnswers() {
        return mJobSetupAnswers;
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final HeaderRowBinding binding;

        public HeaderViewHolder(final HeaderRowBinding itemBinding) {
            super(itemBinding.getRoot());
            this.binding = itemBinding;
        }
    }

    public class HeaderSubViewHolder extends RecyclerView.ViewHolder {

        private final HeaderSubRowBinding binding;

        public HeaderSubViewHolder(final HeaderSubRowBinding itemBinding) {
            super(itemBinding.getRoot());
            this.binding = itemBinding;
        }
    }

    public class UserInputViewHolder extends RecyclerView.ViewHolder {

        private final UserInputRowBinding binding;

        public UserInputViewHolder(final UserInputRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public class MultilineViewHolder extends RecyclerView.ViewHolder {

        private final UserInputMultilineRowBinding binding;

        public MultilineViewHolder(final UserInputMultilineRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public class CityViewHolder extends RecyclerView.ViewHolder {

        private final UserInputCityRowBinding binding;

        public CityViewHolder(final UserInputCityRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public class PhoneViewHolder extends RecyclerView.ViewHolder {

        private final UserInputPhoneRowBinding binding;

        public PhoneViewHolder(final UserInputPhoneRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public class MapViewHolder extends RecyclerView.ViewHolder {

        private final UserInputMapRowBinding binding;

        public MapViewHolder(final UserInputMapRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public class IntegerViewHolder extends RecyclerView.ViewHolder {

        private final UserInputIntRowBinding binding;

        public IntegerViewHolder(final UserInputIntRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public class RealViewHolder extends RecyclerView.ViewHolder {

        private final UserInputRealRowBinding binding;

        public RealViewHolder(final UserInputRealRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public class CheckBoxViewHolder extends RecyclerView.ViewHolder {

        private final ChecboxRowBinding binding;

        public CheckBoxViewHolder(final ChecboxRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public class DatePickerViewHolder extends RecyclerView.ViewHolder {
        private final DatePickerRowBinding binding;

        public DatePickerViewHolder(final DatePickerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public class SignaturePickerViewHolder extends RecyclerView.ViewHolder {
        private final SignaturePickerRowBinding binding;

        public SignaturePickerViewHolder(final SignaturePickerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public class StateNameViewHolder extends RecyclerView.ViewHolder {
        private final StateNameRowPickerBinding binding;

        public StateNameViewHolder(final StateNameRowPickerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public class GoodFairPoorViewHolder extends RecyclerView.ViewHolder {
        private final GoodFairPoorRowPickerBinding binding;

        public GoodFairPoorViewHolder(final GoodFairPoorRowPickerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public class MainOrSidingViewHolder extends RecyclerView.ViewHolder {
        private final MainOrSidingRowPickerBinding binding;

        public MainOrSidingViewHolder(final MainOrSidingRowPickerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public class PipeOrWireViewHolder extends RecyclerView.ViewHolder {
        private final PipeOrWireRowPickerBinding binding;

        public PipeOrWireViewHolder(final PipeOrWireRowPickerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public class FireWaterViewHolder extends RecyclerView.ViewHolder {
        private final FireWaterRowPickerBinding binding;

        public FireWaterViewHolder(final FireWaterRowPickerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(View itemView) {
            super(itemView);

        }
    }

    public String parseLocationCity(String locationName) {
        if (locationName == null) {
            return "";
        }
        List<String> tempList = Arrays.asList(locationName.split(","));
        if (tempList.size() >= 2) {
            return tempList.get(0);
        }
        return "";
    }

    public String parseLocationState(String locationName) {
        if (locationName == null) {
            return "";
        }
        List<String> tempList = Arrays.asList(locationName.split(","));
        if (tempList.size() >= 2) {
            return tempList.get(1);
        }
        return "";
    }

    public void toggleRequired(boolean toggle, int group) {
        for(int i = 0; i < mJobSetupQuestions.size(); i++) {
            FieldPlacementTbl temp = mJobSetupQuestions.get(i);
            if(temp.Group == group) {
                temp.setRequired(toggle);
            }
        }
    }

    public void setDefault(AssignmentTbl assignmentTbl) {
        if (assignmentTbl == null) {
            return;
        }

        for (int i = 0; i < mJobSetupQuestions.size(); i++) {
            try {
                switch (mJobSetupQuestions.get(i).FieldType) {
                    case "DB_RwicName_readonly":
                        mJobSetupAnswers.get(i).setUserInputNoOverride(user.display);
                        break;
                    case "DB_JobNumber_readonly":
                        mJobSetupAnswers.get(i).setUserInputNoOverride(Integer.toString(assignmentTbl.JobId));
                        break;
                    case "DB_CustomerCompanyName_readonly":
                        mJobSetupAnswers.get(i).setUserInputNoOverride(assignmentTbl.CustomerName);
                        break;
                    case "DB_PointOfContactPhone_editable":
                        mJobSetupAnswers.get(i).setUserInputNoOverride(assignmentTbl.CustomerPhone);
                        break;
                    case "DB_JobSubdivision_editable":
                        mJobSetupAnswers.get(i).setUserInputNoOverride(assignmentTbl.Subdivision);
                        break;
                    case "DB_JobMilePost_editable":
                        mJobSetupAnswers.get(i).setUserInputNoOverride(assignmentTbl.MilePostStart);
                        break;
                    case "UserInput":
                        switch (mJobSetupQuestions.get(i).FieldPrompt) {
                            case "City":
                                mJobSetupAnswers.get(i).setUserInputNoOverride(parseLocationCity(assignmentTbl.LocationName));
                                break;
                            case "State":
                                mJobSetupAnswers.get(i).setUserInputNoOverride(parseLocationState(assignmentTbl.LocationName));
                                break;
                            case "Job Number":
                                mJobSetupAnswers.get(i).setUserInputNoOverride(assignmentTbl.JobNumber);
                                break;
                        }
                }
            } catch (Exception ex) {
                // Just trying to fill in default answers, so if some fail, just skip.
                ExpClass.LogEX(ex, "SetDefaults JobSetup");
            }
        }
    }
}
