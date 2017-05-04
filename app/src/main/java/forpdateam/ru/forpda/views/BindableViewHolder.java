package forpdateam.ru.forpda.views;

/**
 * Original here ->> https://github.com/princeparadoxes/RecyclerBindableAdapter.git
 */

import android.support.annotation.CallSuper;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class BindableViewHolder<T, I extends BindableViewHolder.ActionListener<T>>
        extends RecyclerView.ViewHolder {

    public BindableViewHolder(View itemView) {
        super(itemView);
    }

    @CallSuper
    public void bindView(final int position, final T item, final I actionListener) {
        if (actionListener != null) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    actionListener.onItemClickListener(position, item);
                }
            });
        }
    }

    public interface ActionListener<T> {
        void onItemClickListener(int position, T item);
    }
}
