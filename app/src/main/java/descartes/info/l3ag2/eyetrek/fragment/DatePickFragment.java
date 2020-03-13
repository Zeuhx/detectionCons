package descartes.info.l3ag2.eyetrek.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.app.DatePickerDialog;
import android.widget.DatePicker;
import android.widget.TextView;

import java.util.Calendar;

import descartes.info.l3ag2.eyetrek.R;

/**
 * Created by Ayaz ABDUL CADER on 21/02/2018.
 */

public class DatePickFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    @Override

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
        datePickerDialog.getDatePicker().setSpinnersShown(true);
        datePickerDialog.getDatePicker().setCalendarViewShown(false);
        return datePickerDialog;
    }

    @Override

    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        TextView textView = getActivity().findViewById(R.id.birthday);
        String valueMonth,valueDay;
        if (day < 10) {
            valueDay = String.valueOf("0" + day);
        } else {
            valueDay = String.valueOf(day);
        }
        if (month < 10) {
            month = month + 1;
            valueMonth = String.valueOf("0" + month);
        } else {
            valueMonth = String.valueOf(month);
        }
        textView.setText(valueDay + "/" + valueMonth + "/" + year);
    }

}
