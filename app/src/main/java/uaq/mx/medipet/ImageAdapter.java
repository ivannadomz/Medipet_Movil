package uaq.mx.medipet;

import android.view.ViewGroup;
import android.widget.ImageView;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private int[] images;

    public ImageAdapter(int[] images) {
        this.images = images;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = new ImageView(parent.getContext()); // Se cambió aquí
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.imageView.setImageResource(images[position]);
    }

    @Override
    public int getItemCount() {
        return images.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(View itemView) { // Se cambió aquí
            super(itemView);
            imageView = (ImageView) itemView;
        }
    }
}
