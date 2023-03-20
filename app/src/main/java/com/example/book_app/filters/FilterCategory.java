package com.example.book_app.filters;

import android.widget.Filter;

import com.example.book_app.adapters.AdapterCategory;
import com.example.book_app.models.ModelCategory;

import java.util.ArrayList;
import java.util.Locale;

public class FilterCategory extends Filter {
    //array list which we want search
    ArrayList<ModelCategory> filterList;
    //adapter in which filter need to be implements
    AdapterCategory adapterCategory;

    public FilterCategory(ArrayList<ModelCategory> categoryArrayList, AdapterCategory adapterCategory) {
        this.filterList = categoryArrayList;
        this.adapterCategory = adapterCategory;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();

        if(constraint!=null && constraint.length()>0){

            constraint = constraint.toString().toUpperCase(Locale.ROOT);
            ArrayList<ModelCategory> filterModels = new ArrayList<>();
            for (int i = 0; i < filterList.size(); i++){
                //validate
                if(filterList.get(i).getCategory().toUpperCase().contains(constraint)){
                    // add to filter list
                    filterModels.add(filterList.get(i));
                }
            }
            results.count = filterModels.size();
            results.values = filterModels;
        }else{
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        //apply filter changes
        adapterCategory.categoryArrayList = (ArrayList<ModelCategory>)results.values;
        // notifys changes
        adapterCategory.notifyDataSetChanged();
    }
}
