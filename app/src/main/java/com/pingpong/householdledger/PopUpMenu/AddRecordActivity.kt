package com.pingpong.householdledger.PopUpMenu

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.Toast
import com.pingpong.householdledger.CalendarTab.CalendarViewFragment
import com.pingpong.householdledger.DataClass.DateInfo
import com.pingpong.householdledger.DataClass.ExpenseInfo
import com.pingpong.householdledger.MainActivity
import com.pingpong.householdledger.MainActivity.Companion.CalDate
import com.pingpong.householdledger.MainActivity.Companion.CalMonth
import com.pingpong.householdledger.MainActivity.Companion.CalYear
import com.pingpong.householdledger.MainActivity.Companion.CalendarMonthList
import com.pingpong.householdledger.MainActivity.Companion.CalendarViewFragmentList
import com.pingpong.householdledger.MainActivity.Companion.CalendarYearList
import com.pingpong.householdledger.MainActivity.Companion.DATE
import com.pingpong.householdledger.MainActivity.Companion.DateInfoMap
import com.pingpong.householdledger.MainActivity.Companion.FullList
import com.pingpong.householdledger.MainActivity.Companion.MONTH
import com.pingpong.householdledger.MainActivity.Companion.MonthAndDate
import com.pingpong.householdledger.MainActivity.Companion.StatisticsAdapterList
import com.pingpong.householdledger.MainActivity.Companion.Today
import com.pingpong.householdledger.MainActivity.Companion.TotalCalendarFragmentNum
import com.pingpong.householdledger.MainActivity.Companion.YEAR
import com.pingpong.householdledger.R
import kotlinx.android.synthetic.main.activity_add_record.*
import java.text.SimpleDateFormat
import java.util.*

class AddRecordActivity : AppCompatActivity() {
    private var MoneyRecordString = ""
    private var Year = Today.get(Calendar.YEAR)
    private var Month = Today.get(Calendar.MONTH)
    private var Date = Today.get(Calendar.DATE)
    private val DATEPICKERCODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_record)
        InitialSetting()
        DateRecordField.setOnClickListener{
            val Intent = Intent(this, DatePickerActivity::class.java)
            Intent.putExtra(YEAR,Year)
            Intent.putExtra(MONTH,Month)
            Intent.putExtra(DATE,Date)
            startActivityForResult(Intent,DATEPICKERCODE)
        }
        ConfirmBut.setOnClickListener {
            val MoneyString = MoneyRecordField.text.toString().replace(",","")
            when{
                MoneyString.length < 4 -> Toast.makeText(applicationContext, "금액을 1,000원 이상 입력해 주세요.",Toast.LENGTH_SHORT).show()
                else -> AddSpendList()
            }

        }
        CancelBut.setOnClickListener {
            finish()
        }

        MoneyRecordField.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(!TextUtils.isEmpty(s.toString()) && !s.toString().equals(MoneyRecordString)) {
                    MoneyRecordString = MainActivity.MoneyDecimalFormat.format((s.toString().replace(",","")).toDouble())
                    MoneyRecordField.setText(MoneyRecordString)
                    MoneyRecordField.setSelection(MoneyRecordString.length)
                }
            }

        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode==DATEPICKERCODE && resultCode== Activity.RESULT_OK){
            if(data!=null && data.hasExtra(YEAR) && data.hasExtra(MONTH) && data.hasExtra(DATE)){
                val date = (data.getIntExtra(MONTH,Month)+1).toString()+"월 "+data.getIntExtra(DATE,Date)+"일"
                DateRecordField.text = date
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun InitialSetting(){
        ClassificationSpinner.adapter = ArrayAdapter(this,
            R.layout.support_simple_spinner_dropdown_item,StatisticsAdapterList)
        val calendar = Calendar.getInstance()
        DateRecordField.text = SimpleDateFormat(MonthAndDate).format(calendar.time)
    }

    private fun AddSpendList(){
        Today = Calendar.getInstance()
        val year : String = CalYear(Today).toString()
        var month : String = CalMonth(Today).toString()
        var date : String =  CalDate(Today).toString()
        if(CalMonth(Today)<10)
            month = "0"+CalMonth(Today)
        if(CalDate(Today)<10)
            date =  "0"+CalDate(Today)
        val TimeInLength8 = Integer.parseInt(year + month + date)
//        val test : Int = (CalYear(Today).toString() + CalMonth(Today).toString() + CalDate(Today).toString()).toInt()
        //이렇게도 표현 가능하다.
        val TimeinMillis = Today.timeInMillis

        val Classify : String? = when(ClassificationSpinner.selectedItemPosition) {
            0 -> null
            else ->  ClassificationSpinner.selectedItem.toString()
        }

        val Money = MoneyRecordField.text.toString().replace(",","").toInt()

        val PaymentMethod = PaymentMethodSpinner.selectedItem.toString()

        val Content = ContentRecordField.text.toString()

        val Expense = ExpenseInfo(TimeInLength8, TimeinMillis,Classify,Money,PaymentMethod,Content)

        if(DateInfoMap.containsKey(TimeInLength8)){
            for(i in FullList.indices){
                if(FullList[i].DateInLength8==TimeInLength8){
                    FullList[i].Spend+=Money
                    FullList[i].Total = FullList[i].Income - FullList[i].Spend
                    FullList[i].ExpenseList.add(Expense)
                    DateInfoMap.put(TimeInLength8, DateInfoMap.get(TimeInLength8)!!.plus(1))
                }
            }
        } else {
            FullList.add(DateInfo(TimeInLength8,TimeinMillis,Spend = Money, Total = -Money , ExpenseList = arrayListOf<ExpenseInfo>(Expense)))
            DateInfoMap.put(TimeInLength8,1)
        }

        for(i in 0 until TotalCalendarFragmentNum){
            if(CalendarYearList[i]==year.toInt()&& CalendarMonthList[i]==month.toInt()){
                val Frags = CalendarViewFragment()
                Frags.apply {
                    arguments = Bundle().apply {
                        putInt(MainActivity.YEAR,year.toInt())
                        putInt(MainActivity.MONTH,month.toInt())
                    }
                }
                CalendarViewFragmentList[i]=Frags
                break;
            }
        }
        finish()
    }

}