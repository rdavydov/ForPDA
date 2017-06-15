package forpdateam.ru.forpda.fragments.news.details.presenter

import forpdateam.ru.forpda.fragments.news.details.INewsDetailsView

/**
 * Created by isanechek on 6/13/17.
 */
interface INewsDetailsPresenter {
    fun bind(view: INewsDetailsView)
    fun unbind()
}