package forpdateam.ru.forpda.fragments.news.callbacks;

import android.view.View;

/**
 * Created by isanechek on 5/12/17.
 */

public interface NewsListClickListener {
    void click(View view, int position);
    void longClick(int position);
}
