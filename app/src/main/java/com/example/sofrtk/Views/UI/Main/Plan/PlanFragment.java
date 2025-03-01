package com.example.sofrtk.Views.UI.Main.Plan;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.sofrtk.DB.Model.FavouriteMeal;
import com.example.sofrtk.DB.Model.PlanMeal;
import com.example.sofrtk.Models.DTOs.RandomMeal;
import com.example.sofrtk.Models.Repository.Repository;
import com.example.sofrtk.Presenters.Plan.PlanPresenterImp;
import com.example.sofrtk.R;
import com.example.sofrtk.Views.Adapters.DateAdapter;
import com.example.sofrtk.Views.Adapters.FavouriteMealsAdapter;
import com.example.sofrtk.Views.Adapters.PlanMealsAdapter;
import com.example.sofrtk.Views.UI.Main.Home.HomeFragmentDirections;
import com.f2prateek.rx.preferences2.RxSharedPreferences;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PlanFragment extends Fragment implements PlanView{
    PlanPresenterImp planPresenter;
    RecyclerView weekDaysRecyclerView;
    DateAdapter dateAdapter;
    List<String> dateList;
    RecyclerView planMealsRecyclerView;
    LinearLayoutManager linearLayoutManager;
    PlanMealsAdapter planMealsAdapter;
    List<PlanMeal> planMealList = new ArrayList<>();
    RxSharedPreferences rxSharedPreferences;

    public PlanFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_plan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", getActivity().MODE_PRIVATE);
        rxSharedPreferences = RxSharedPreferences.create(sharedPreferences);

        planPresenter = new PlanPresenterImp(this, Repository.getInstance(getActivity()));

        planMealsRecyclerView = view.findViewById(R.id.planMealsRecyclerView);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        planMealsRecyclerView.setLayoutManager(linearLayoutManager);
        planMealsAdapter = new PlanMealsAdapter(getActivity(),planMealList);
        planMealsRecyclerView.setAdapter(planMealsAdapter);

        weekDaysRecyclerView = view.findViewById(R.id.weekDaysRecyclerView);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        dateList = getNextWeekDates();
        weekDaysRecyclerView.setLayoutManager(linearLayoutManager);
        dateAdapter = new DateAdapter(dateList);
        weekDaysRecyclerView.setAdapter(dateAdapter);

        dateAdapter.setOnItemClickListener(selectedDate -> {
            dateAdapter.setOnItemClickListener(new DateAdapter.OnItemClickListener() {
                @Override
                public void onClicks(String selectedDate) {
                    if (rxSharedPreferences.getBoolean("isLoggedIn", false).get()) {
                        planPresenter.getPlanMeals(rxSharedPreferences.getString("userId").get(),selectedDate);
                        Log.i("TAG", "onClicks: " + rxSharedPreferences.getString("userId").get() + selectedDate);
                    }
                }
            });
        });

    }

    @Override
    public void showPlanMeals(List<PlanMeal> planMealList) {
        planMealsAdapter.updateData(planMealList);

        planMealsAdapter.setOnItemClickListener(new PlanMealsAdapter.OnItemClickListener() {
            @Override
            public void onRemoveClicks(PlanMeal planMeal) {
                planPresenter.deletePlanMeal(planMeal);
            }

            @Override
            public void onClicks(RandomMeal randomMeal) {
                navigateToDetailedMealFragment(randomMeal.getIdMeal(),randomMeal);
            }
        });
    }

    @Override
    public void showPlanMealsError(String errorMsg) {
        Toast.makeText(getActivity(),errorMsg,Toast.LENGTH_LONG).show();
        Log.e("TAG", "showPlanMealsError: " + errorMsg );
    }

    @Override
    public void deletePlanMeals(PlanMeal planMeal) {
        planMealsAdapter.removeItem(planMeal);
        Toast.makeText(getActivity(),"Meal Removed Successfully!",Toast.LENGTH_LONG).show();
    }

    @Override
    public void deletePlanMealsError(String errorMsg) {
        Toast.makeText(getActivity(),errorMsg,Toast.LENGTH_LONG).show();
    }

    @Override
    public Activity getViewActivity() {
        return requireActivity();
    }


    public List<String> getNextWeekDates() {
        List<String> dates = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd", Locale.getDefault());

        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < 7; i++) {
            dates.add(sdf.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        return dates;
    }
    public void navigateToDetailedMealFragment(String id, RandomMeal randomMeal){
        Navigation.findNavController(requireView()).navigate(PlanFragmentDirections.actionPlanFragmentToDetailedMealFragment(id,randomMeal));
    }
}