package com.example.book_app.filters;

import android.widget.Filter;

import com.example.book_app.adapters.AdapterCategory;
import com.example.book_app.adapters.AdapterPdfAdmin;
import com.example.book_app.models.ModelCategory;
import com.example.book_app.models.ModelPdf;

import java.util.ArrayList;
import java.util.Locale;

public class FilterPdfAdmin extends Filter {
    //array list which we want search
    ArrayList<ModelPdf> filterList;
    //adapter in which filter need to be implements
    AdapterPdfAdmin adapterPdfAdmin;

    public FilterPdfAdmin(ArrayList<ModelPdf> categoryArrayList, AdapterPdfAdmin adapterPdfAdmin) {
        this.filterList = categoryArrayList;
        this.adapterPdfAdmin = adapterPdfAdmin;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();

        if(constraint!=null && constraint.length()>0){

            constraint = constraint.toString().toUpperCase(Locale.ROOT);
            ArrayList<ModelPdf> filterModels = new ArrayList<>();
            for (int i = 0; i < filterList.size(); i++){
                //validate
                if(filterList.get(i).getTitle().toUpperCase().contains(constraint)){
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
        adapterPdfAdmin.pdfArrayList = (ArrayList<ModelPdf>)results.values;
        // notifys changes
        adapterPdfAdmin.notifyDataSetChanged();
    }
}
