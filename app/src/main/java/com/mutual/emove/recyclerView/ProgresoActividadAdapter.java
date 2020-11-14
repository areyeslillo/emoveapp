package com.mutual.emove.recyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.mutual.emove.R;
import com.mutual.emove.entidades.DiaActividad;

import java.util.ArrayList;
import java.util.List;

public class ProgresoActividadAdapter extends RecyclerView.Adapter<ProgresoActividadAdapter.DiaActividadAdapterVh> implements Filterable {

    private List<DiaActividad> DiaActividadList;
    private List<DiaActividad> getDiaActividadListFiltered;
    private Context context;
    private SelectedUser selectedUser;
    public ProgresoActividadAdapter(List<DiaActividad> DiaActividadList) {
        this.DiaActividadList = DiaActividadList;
        this.getDiaActividadListFiltered = DiaActividadList;
//        this.selectedUser = selectedUser;
    }

    @NonNull
    @Override
    public ProgresoActividadAdapter.DiaActividadAdapterVh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();

        return new DiaActividadAdapterVh(LayoutInflater.from(context).inflate(R.layout.row_inicio,null));
    }

    @Override
    public void onBindViewHolder(@NonNull ProgresoActividadAdapter.DiaActividadAdapterVh holder, int position) {

        DiaActividad DiaActividad = DiaActividadList.get(position);

        String dia = DiaActividad.getDia();
        String prefix = DiaActividad.getDia().substring(0,2);

        holder.tvDia.setText("Día "+dia);
        holder.tvPrefix.setText(prefix);

        if(DiaActividad.getEstado().equals("Realizado")){
            holder.tvBG.setBackground(ContextCompat.getDrawable(context, R.drawable.elipse_ant));
        }
        else if(DiaActividad.getEstado().equals("Actual")){
            holder.tvBG.setBackground(ContextCompat.getDrawable(context, R.drawable.elipse_actual));
        }
        else if(DiaActividad.getEstado().equals("Pendiente")){
            holder.tvBG.setBackground(ContextCompat.getDrawable(context, R.drawable.elipse_prox));
            holder.tvPrefix.setTextColor(0xffbdbdbd);
        }
    }

    @Override
    public int getItemCount() {
        return DiaActividadList.size();
    }

    @Override
    public Filter getFilter() {

        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults filterResults = new FilterResults();

                if(charSequence == null | charSequence.length() == 0){
                    filterResults.count = getDiaActividadListFiltered.size();
                    filterResults.values = getDiaActividadListFiltered;

                }else{
                    String searchChr = charSequence.toString().toLowerCase();

                    List<DiaActividad> resultData = new ArrayList<>();

                    for(DiaActividad DiaActividad: getDiaActividadListFiltered){
                        if(DiaActividad.getDia().toLowerCase().contains(searchChr)){
                            resultData.add(DiaActividad);
                        }
                    }
                    filterResults.count = resultData.size();
                    filterResults.values = resultData;

                }

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

                DiaActividadList = (List<DiaActividad>) filterResults.values;
                notifyDataSetChanged();

            }
        };
        return filter;
    }


    public interface SelectedUser{

        void selectedUser(DiaActividad DiaActividad);

    }

    public class DiaActividadAdapterVh extends RecyclerView.ViewHolder {

        TextView tvPrefix;
        TextView tvDia;
        RelativeLayout tvBG;
        ImageView imIcon;
        public DiaActividadAdapterVh(@NonNull View itemView) {
            super(itemView);
            tvPrefix = itemView.findViewById(R.id.prefix);
            tvDia = itemView.findViewById(R.id.dia);
            tvBG = itemView.findViewById(R.id.rvPrefix);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // selectedUser.selectedUser(DiaActividadList.get(getAdapterPosition()));
                }
            });


        }
    }
}
