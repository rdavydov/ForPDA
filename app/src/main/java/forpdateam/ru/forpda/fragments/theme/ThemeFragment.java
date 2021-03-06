package forpdateam.ru.forpda.fragments.theme;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Observer;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.TabManager;
import forpdateam.ru.forpda.api.IBaseForumPost;
import forpdateam.ru.forpda.api.RequestFile;
import forpdateam.ru.forpda.api.favorites.Favorites;
import forpdateam.ru.forpda.api.theme.editpost.models.AttachmentItem;
import forpdateam.ru.forpda.api.theme.editpost.models.EditPostForm;
import forpdateam.ru.forpda.api.theme.models.ThemePage;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.favorites.FavoritesFragment;
import forpdateam.ru.forpda.fragments.favorites.FavoritesHelper;
import forpdateam.ru.forpda.fragments.jsinterfaces.IPostFunctions;
import forpdateam.ru.forpda.fragments.theme.editpost.EditPostFragment;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.settings.Preferences;
import forpdateam.ru.forpda.utils.FabOnScroll;
import forpdateam.ru.forpda.utils.FilePickHelper;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.Utils;
import forpdateam.ru.forpda.utils.rx.Subscriber;
import forpdateam.ru.forpda.views.messagepanel.MessagePanel;
import forpdateam.ru.forpda.views.messagepanel.attachments.AttachmentsPopup;
import forpdateam.ru.forpda.views.pagination.PaginationHelper;
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip;

/**
 * Created by radiationx on 20.10.16.
 */

public abstract class ThemeFragment extends TabFragment implements IPostFunctions {
    //Указывают на произведенное действие: переход назад, обновление, обычный переход по ссылке
    protected final static int BACK_ACTION = 0, REFRESH_ACTION = 1, NORMAL_ACTION = 2;
    protected int loadAction = NORMAL_ACTION;
    protected MenuItem toggleMessagePanelItem;
    protected MenuItem refreshMenuItem;
    protected MenuItem copyLinkMenuItem;
    protected MenuItem searchInThemeMenuItem;
    protected MenuItem searchOnPageMenuItem;
    protected MenuItem deleteFavoritesMenuItem;
    protected MenuItem addFavoritesMenuItem;
    protected MenuItem openForumMenuItem;
    protected SwipeRefreshLayout refreshLayout;
    protected ThemePage currentPage;
    protected List<ThemePage> history = new ArrayList<>();
    protected Subscriber<ThemePage> mainSubscriber = new Subscriber<>(this);
    //protected Subscriber<String> helperSubscriber = new Subscriber<>(this);
    private PaginationHelper paginationHelper = new PaginationHelper();
    //Тег для вьюхи поиска. Чтобы создавались кнопки и т.д, только при вызове поиска, а не при каждом создании меню.
    protected int searchViewTag = 0;
    //protected final ColorFilter colorFilter = new PorterDuffColorFilter(Color.argb(80, 255, 255, 255), PorterDuff.Mode.DST_IN);
    protected MessagePanel messagePanel;
    protected AttachmentsPopup attachmentsPopup;
    protected Subscriber<List<AttachmentItem>> attachmentSubscriber = new Subscriber<>(this);
    protected String tab_url = "";
    protected SimpleTooltip tooltip;
    private Observer themePreferenceObserver = (observable, o) -> {
        Log.d("SUKA", "themePreferenceObserver " + o);
        if (o == null) return;
        String key = (String) o;
        switch (key) {
            case Preferences.Theme.SHOW_AVATARS: {
                updateShowAvatarState(App.getInstance().getPreferences().getBoolean(Preferences.Theme.SHOW_AVATARS, true));
                break;
            }
            case Preferences.Theme.CIRCLE_AVATARS: {
                updateTypeAvatarState(App.getInstance().getPreferences().getBoolean(Preferences.Theme.CIRCLE_AVATARS, true));
                break;
            }
            case Preferences.Main.WEBVIEW_FONT_SIZE: {
                setFontSize(Preferences.Main.getWebViewSize());
            }
            case Preferences.Main.SCROLL_BUTTON_ENABLE: {
                if (App.getInstance().getPreferences().getBoolean(Preferences.Main.SCROLL_BUTTON_ENABLE, false)) {
                    fab.setVisibility(View.VISIBLE);
                } else {
                    fab.setVisibility(View.GONE);
                }
            }
        }
    };


    protected abstract void addShowingView();

    protected abstract void findNext(boolean next);

    protected abstract void findText(String text);

    protected abstract void saveToHistory(ThemePage themePage);

    protected abstract void updateHistoryLast(ThemePage themePage);

    protected abstract void updateShowAvatarState(boolean isShow);

    protected abstract void updateTypeAvatarState(boolean isCircle);

    protected abstract void setFontSize(int size);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tab_url = getArguments().getString(ARG_TAB);
        }
    }

    @Override
    protected void initFabBehavior() {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        FabOnScroll behavior = new FabOnScroll(fab.getContext(), null);
        params.setBehavior(behavior);
        params.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            params.setMargins(App.px16, App.px16, App.px16, App.px16);
        }
        fab.requestLayout();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        initFabBehavior();
        fab.setSize(FloatingActionButton.SIZE_MINI);
        if (App.getInstance().getPreferences().getBoolean(Preferences.Main.SCROLL_BUTTON_ENABLE, false)) {
            fab.setVisibility(View.VISIBLE);
        } else {
            fab.setVisibility(View.GONE);
        }
        fab.setScaleX(0.0f);
        fab.setScaleY(0.0f);
        fab.setAlpha(0.0f);
        baseInflateFragment(inflater, R.layout.fragment_theme);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_list);
        messagePanel = new MessagePanel(getContext(), fragmentContainer, coordinatorLayout, false);
        messagePanel.enableBehavior();
        messagePanel.addSendOnClickListener(v -> sendMessage());
        messagePanel.getSendButton().setOnLongClickListener(v -> {
            EditPostForm form = createEditPostForm();
            if (form != null) {
                TabManager.getInstance().add(EditPostFragment.newInstance(createEditPostForm(), currentPage.getTitle()));
            }
            return true;
        });
        messagePanel.getFullButton().setVisibility(View.VISIBLE);
        messagePanel.getFullButton().setOnClickListener(v -> {
            EditPostForm form = createEditPostForm();
            if (form != null) {
                TabManager.getInstance().add(EditPostFragment.newInstance(createEditPostForm(), currentPage.getTitle()));
            }
        });
        messagePanel.getHideButton().setVisibility(View.VISIBLE);
        messagePanel.getHideButton().setOnClickListener(v -> {
            hideMessagePanel();
        });
        attachmentsPopup = messagePanel.getAttachmentsPopup();
        attachmentsPopup.setAddOnClickListener(v -> tryPickFile());
        attachmentsPopup.setDeleteOnClickListener(v -> removeFiles());

        paginationHelper.inflatePagination(getContext(), inflater, toolbar);
        paginationHelper.setupToolbar(toolbarLayout);
        paginationHelper.setListener(new PaginationHelper.PaginationListener() {
            @Override
            public boolean onTabSelected(TabLayout.Tab tab) {
                return refreshLayout.isRefreshing();
            }

            @Override
            public void onSelectedPage(int pageNumber) {
                Log.e("FORPDA_LOG", "SELECTED TAB URL " + tab_url);
                String url = "http://4pda.ru/forum/index.php?showtopic=";
                url = url.concat(Uri.parse(tab_url).getQueryParameter("showtopic"));
                if (pageNumber != 0) url = url.concat("&st=").concat(Integer.toString(pageNumber));
                tab_url = url;
                loadData(NORMAL_ACTION);
            }
        });
        addShowingView();
        setFontSize(Preferences.Main.getWebViewSize());
        viewsReady();
        App.getInstance().addPreferenceChangeObserver(themePreferenceObserver);
        refreshLayoutStyle(refreshLayout);
        refreshLayout.setOnRefreshListener(() -> {
            loadData(REFRESH_ACTION);
        });
        if (App.getInstance().getPreferences().getBoolean("theme.tooltip.long_click_send", true)) {
            tooltip = new SimpleTooltip.Builder(getContext())
                    .anchorView(messagePanel.getSendButton())
                    .text("Долгое нажатие откроет полную форму ответа")
                    .gravity(Gravity.TOP)
                    .animated(false)
                    .modal(true)
                    .transparentOverlay(false)
                    .backgroundColor(Color.BLACK)
                    .textColor(Color.WHITE)
                    .padding((float) App.px16)
                    .build();
            tooltip.show();
            App.getInstance().getPreferences().edit().putBoolean("theme.tooltip.long_click_send", false).apply();
        }

        if (App.getInstance().getPreferences().getBoolean(Preferences.Main.IS_EDITOR_DEFAULT_HIDDEN, true)) {
            hideMessagePanel();
        } else {
            showMessagePanel();
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        messagePanel.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        messagePanel.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.getInstance().removePreferenceChangeObserver(themePreferenceObserver);
        history.clear();
        messagePanel.onDestroy();
    }

    @Override
    public void hidePopupWindows() {
        super.hidePopupWindows();
        messagePanel.hidePopupWindows();
    }

    public abstract void scrollToAnchor(String anchor);

    @Override
    public boolean onBackPressed() {
        if (tooltip != null && tooltip.isShowing()) {
            tooltip.dismiss();
            return true;
        }
        if (messagePanel.onBackPressed())
            return true;
        if (getMenu().findItem(R.id.action_search) != null && getMenu().findItem(R.id.action_search).isActionViewExpanded()) {
            toolbar.collapseActionView();
            return true;
        }
        if (App.getInstance().getPreferences().getBoolean("theme.anchor_history", true)) {
            if (currentPage != null && currentPage.getAnchors().size() > 1) {
                currentPage.removeAnchor();
                scrollToAnchor(currentPage.getAnchor());
                return true;
            }
        }
        if (history.size() > 1) {
            setAction(BACK_ACTION);

            history.remove(history.size() - 1);
            currentPage = history.get(history.size() - 1);
            Log.e("console", "BACK PRESS REMOVE " + currentPage);
            tab_url = currentPage.getUrl();
            updateView();
            return true;
        }
        if ((messagePanel.getMessage() != null && !messagePanel.getMessage().isEmpty()) || messagePanel.getAttachments().size() > 0) {
            new AlertDialog.Builder(getContext())
                    .setMessage("Забыть изменения?")
                    .setPositiveButton("Да", (dialog, which) -> {
                        history.clear();
                        TabManager.getInstance().remove(ThemeFragment.this);
                    })
                    .setNegativeButton("Нет", null)
                    .show();
            return true;
        }
        return false;
    }


    /*
    *
    * LOADING POST FUNCTIONS
    *
    * */

    public void setAction(int action) {
        this.loadAction = action;
    }

    protected abstract void updateHistoryLastHtml();

    public void loadData(int action) {
        setAction(action);
        if (action == NORMAL_ACTION) {
            updateHistoryLastHtml();
        }
        loadData();
    }

    @Override
    public void loadData() {
        refreshLayout.setRefreshing(true);

        refreshToolbarMenuItems(false);
        boolean hatOpen = false;
        boolean pollOpen = false;
        if (currentPage != null) {
            hatOpen = currentPage.isHatOpen();
            pollOpen = currentPage.isPollOpen();
        }
        mainSubscriber.subscribe(RxApi.Theme().getTheme(tab_url, true, hatOpen, pollOpen), this::onLoadData, new ThemePage(), v -> loadData());
    }

    protected void onLoadData(ThemePage newPage) throws Exception {
        refreshLayout.setRefreshing(false);
        if (newPage == null || newPage.getId() == 0 || newPage.getUrl() == null) {
            if (currentPage != null)
                tab_url = currentPage.getUrl();
            return;
        }
        if (currentPage == null) {
            new Handler().postDelayed(() -> (appBarLayout).setExpanded(false, true), 225);
        }

        currentPage = newPage;

        if (loadAction == NORMAL_ACTION) {
            saveToHistory(currentPage);
        }
        if (loadAction == REFRESH_ACTION) {
            updateHistoryLast(currentPage);
        }
        updateFavorites(currentPage);
        updateView();
    }

    protected void updateTitle() {
        setTitle(currentPage.getTitle());
    }

    protected void updateSubTitle() {
        setSubtitle(String.valueOf(currentPage.getPagination().getCurrent()).concat("/").concat(String.valueOf(currentPage.getPagination().getAll())));
    }

    protected void updateFavorites(ThemePage themePage) {
        if (themePage.getPagination().getCurrent() < themePage.getPagination().getAll()) return;
        String tag = TabManager.getInstance().getTagContainClass(FavoritesFragment.class);
        if (tag == null) return;
        Log.e("FORPDA_LOG", "UPDATE FOVARITE " + tag + " : " + TabManager.getInstance().get(tag));
        ((FavoritesFragment) TabManager.getInstance().get(tag)).markRead(themePage.getId());
    }

    @CallSuper
    protected void updateView() {
        paginationHelper.updatePagination(currentPage.getPagination());
        tab_url = currentPage.getUrl();
        updateTitle();
        updateSubTitle();
        refreshToolbarMenuItems(true);
    }

    private void toggleMessagePanel() {
        if (messagePanel.getVisibility() == View.VISIBLE) {
            hideMessagePanel();
        } else {
            showMessagePanel();
        }
    }

    private void showMessagePanel() {
        messagePanel.setVisibility(View.VISIBLE);
        messagePanel.getHeightChangeListener().onChangedHeight(messagePanel.getLastHeight());
        toggleMessagePanelItem.setIcon(App.getAppDrawable(getContext(), R.drawable.ic_toolbar_transcribe_close));
    }

    private void hideMessagePanel() {
        messagePanel.setVisibility(View.GONE);
        messagePanel.hidePopupWindows();
        hidePopupWindows();
        messagePanel.getHeightChangeListener().onChangedHeight(0);
        toggleMessagePanelItem.setIcon(App.getAppDrawable(getContext(), R.drawable.ic_toolbar_create));
    }

    @Override
    protected void addBaseToolbarMenu() {
        super.addBaseToolbarMenu();
        toggleMessagePanelItem = getMenu()
                .add("Ответить")
                .setIcon(App.getAppDrawable(getContext(), R.drawable.ic_toolbar_create))
                .setOnMenuItemClickListener(menuItem -> {
                    toggleMessagePanel();
                    return false;
                })
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);

        refreshMenuItem = getMenu().add("Обновить")
                .setIcon(App.getAppDrawable(getContext(), R.drawable.ic_toolbar_refresh))
                .setOnMenuItemClickListener(menuItem -> {
                    loadData(REFRESH_ACTION);
                    return false;
                });

        copyLinkMenuItem = getMenu().add("Скопировать ссылку")
                .setOnMenuItemClickListener(menuItem -> {
                    String url = tab_url;
                    if (currentPage != null) {
                        url = "http://4pda.ru/forum/index.php?showtopic=" + currentPage.getId();
                    }
                    Utils.copyToClipBoard(url);
                    return false;
                });
        addSearchOnPageItem(getMenu());
        searchInThemeMenuItem = getMenu().add("Найти в теме")
                .setOnMenuItemClickListener(menuItem -> {
                    IntentHandler.handle("http://4pda.ru/forum/index.php?forums=" + currentPage.getForumId() + "&topics=" + currentPage.getId() + "&act=search&source=pst");
                    return false;
                });

        SubMenu subMenu = getMenu().addSubMenu("Опции темы");
        deleteFavoritesMenuItem = subMenu.add("Удалить из избранного")
                .setOnMenuItemClickListener(menuItem -> {
                    if (currentPage.getFavId() == 0) {
                        Toast.makeText(App.getContext(), "ID темы не найден, попробуйте перезагрузить страницу", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    FavoritesHelper.delete(aBoolean -> {
                        Toast.makeText(App.getContext(), aBoolean ? "Тема удалена из избранного" : "Ошибка", Toast.LENGTH_SHORT).show();
                        currentPage.setInFavorite(!aBoolean);
                        refreshToolbarMenuItems(true);
                    }, currentPage.getFavId());
                    return false;
                });
        addFavoritesMenuItem = subMenu.add("Добавить в избранное")
                .setOnMenuItemClickListener(menuItem -> {
                    new AlertDialog.Builder(getContext())
                            .setItems(Favorites.SUB_NAMES, (dialog1, which1) -> FavoritesHelper.add(aBoolean -> {
                                Toast.makeText(App.getContext(), aBoolean ? "Тема добавлена в избранное" : "Ошибка", Toast.LENGTH_SHORT).show();
                                currentPage.setInFavorite(aBoolean);
                                refreshToolbarMenuItems(true);
                            }, currentPage.getId(), Favorites.SUB_TYPES[which1]))
                            .show();
                    return false;
                });
        openForumMenuItem = subMenu.add("Открыть форум темы")
                .setOnMenuItemClickListener(menuItem -> {
                    IntentHandler.handle("http://4pda.ru/forum/index.php?showforum=" + currentPage.getForumId());
                    return false;
                });

        refreshToolbarMenuItems(false);
    }

    @Override
    protected void refreshToolbarMenuItems(boolean enable) {
        super.refreshToolbarMenuItems(enable);
        Log.d("SUKA", "refreshToolbarMenuItems " + enable);
        if (enable) {
            boolean pageNotNull = !(currentPage == null || currentPage.getId() == 0 || currentPage.getUrl() == null);
            Log.d("SUKA", "SADASD " + pageNotNull + " : " + currentPage + " : " + currentPage.getId() + " : " + currentPage.getUrl());

            toggleMessagePanelItem.setEnabled(true);
            refreshMenuItem.setEnabled(true);
            copyLinkMenuItem.setEnabled(pageNotNull);
            searchInThemeMenuItem.setEnabled(pageNotNull);
            searchOnPageMenuItem.setEnabled(pageNotNull);
            deleteFavoritesMenuItem.setEnabled(pageNotNull);
            addFavoritesMenuItem.setEnabled(pageNotNull);
            if (pageNotNull) {
                if (currentPage.isInFavorite()) {
                    deleteFavoritesMenuItem.setVisible(true);
                    addFavoritesMenuItem.setVisible(false);
                } else {
                    deleteFavoritesMenuItem.setVisible(false);
                    addFavoritesMenuItem.setVisible(true);
                }
            }
            openForumMenuItem.setEnabled(pageNotNull);
        } else {
            toggleMessagePanelItem.setEnabled(true);
            refreshMenuItem.setEnabled(true);
            copyLinkMenuItem.setEnabled(false);
            searchInThemeMenuItem.setEnabled(false);
            searchOnPageMenuItem.setEnabled(false);
            deleteFavoritesMenuItem.setEnabled(false);
            addFavoritesMenuItem.setEnabled(false);
            deleteFavoritesMenuItem.setVisible(false);
            addFavoritesMenuItem.setVisible(false);
            openForumMenuItem.setEnabled(false);
        }
    }

    private void addSearchOnPageItem(Menu menu) {
        toolbar.inflateMenu(R.menu.theme_search_menu);
        searchOnPageMenuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchOnPageMenuItem.getActionView();
        searchView.setTag(searchViewTag);

        searchView.setOnSearchClickListener(v -> {
            if (searchView.getTag().equals(searchViewTag)) {
                ImageView searchClose = (ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
                if (searchClose != null)
                    ((ViewGroup) searchClose.getParent()).removeView(searchClose);

                ViewGroup.LayoutParams navButtonsParams = new ViewGroup.LayoutParams(App.px48, App.px48);
                TypedValue outValue = new TypedValue();
                getContext().getTheme().resolveAttribute(android.R.attr.actionBarItemBackground, outValue, true);

                AppCompatImageButton btnNext = new AppCompatImageButton(searchView.getContext());
                btnNext.setImageDrawable(App.getAppDrawable(getContext(), R.drawable.ic_toolbar_search_next));
                btnNext.setBackgroundResource(outValue.resourceId);

                AppCompatImageButton btnPrev = new AppCompatImageButton(searchView.getContext());
                btnPrev.setImageDrawable(App.getAppDrawable(getContext(), R.drawable.ic_toolbar_search_prev));
                btnPrev.setBackgroundResource(outValue.resourceId);

                ((LinearLayout) searchView.getChildAt(0)).addView(btnPrev, navButtonsParams);
                ((LinearLayout) searchView.getChildAt(0)).addView(btnNext, navButtonsParams);

                btnNext.setOnClickListener(v1 -> findNext(true));
                btnPrev.setOnClickListener(v1 -> findNext(false));
                searchViewTag++;
            }
        });

        SearchManager searchManager = (SearchManager) getMainActivity().getSystemService(Context.SEARCH_SERVICE);
        if (null != searchManager)
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getMainActivity().getComponentName()));

        searchView.setIconifiedByDefault(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                findText(newText);
                return false;
            }
        });
    }


    /*
    *
    * EDIT POST FUNCTIONS
    *
    * */

    public MessagePanel getMessagePanel() {
        return messagePanel;
    }

    public AttachmentsPopup getAttachmentsPopup() {
        return attachmentsPopup;
    }

    private EditPostForm createEditPostForm() {
        EditPostForm form = new EditPostForm();
        if (currentPage == null) {
            return null;
        }
        form.setForumId(currentPage.getForumId());
        form.setTopicId(currentPage.getId());
        form.setSt(currentPage.getPagination().getCurrent() * currentPage.getPagination().getPerPage());
        form.setMessage(messagePanel.getMessage());
        List<AttachmentItem> attachments = messagePanel.getAttachments();
        for (AttachmentItem item : attachments) {
            form.addAttachment(item);
        }
        return form;
    }

    public void onSendPostCompleted(ThemePage themePage) throws Exception {
        messagePanel.clearAttachments();
        messagePanel.clearMessage();
        onLoadData(themePage);
    }

    public void onEditPostCompleted(ThemePage themePage) throws Exception {
        onLoadData(themePage);
    }

    private void sendMessage() {
        hidePopupWindows();
        EditPostForm form = createEditPostForm();
        if (form != null) {
            messagePanel.setProgressState(true);
            mainSubscriber.subscribe(RxApi.EditPost().sendPost(form), s -> {
                messagePanel.setProgressState(false);
                if (s != currentPage) {
                    onLoadData(s);
                    messagePanel.clearAttachments();
                    messagePanel.clearMessage();
                    if (App.getInstance().getPreferences().getBoolean(Preferences.Main.IS_EDITOR_DEFAULT_HIDDEN, true)) {
                        hideMessagePanel();
                    }
                }
            }, currentPage);
        }

    }

    public void tryPickFile() {
        getMainActivity().checkStoragePermission(() -> startActivityForResult(FilePickHelper.pickImage(false), REQUEST_PICK_FILE));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_FILE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }
            uploadFiles(FilePickHelper.onActivityResult(getContext(), data));
        }
    }

    public void uploadFiles(List<RequestFile> files) {
        List<AttachmentItem> pending = attachmentsPopup.preUploadFiles(files);
        attachmentSubscriber.subscribe(RxApi.EditPost().uploadFiles(0, files, pending), items -> attachmentsPopup.onUploadFiles(items), new ArrayList<>(), null);
    }

    public void removeFiles() {
        attachmentsPopup.preDeleteFiles();
        List<AttachmentItem> selectedFiles = attachmentsPopup.getSelected();
        attachmentSubscriber.subscribe(RxApi.EditPost().deleteFiles(0, selectedFiles), item -> attachmentsPopup.onDeleteFiles(selectedFiles), selectedFiles, null);
    }



    /*
    *
    * Post functions
    *
    * */

    public IBaseForumPost getPostById(int postId) {
        for (IBaseForumPost post : currentPage.getPosts())
            if (post.getId() == postId)
                return post;
        return null;
    }

    public void firstPage() {
        paginationHelper.firstPage();
    }

    public void prevPage() {
        paginationHelper.prevPage();
    }


    public void nextPage() {
        paginationHelper.nextPage();
    }

    public void lastPage() {
        paginationHelper.lastPage();
    }

    public void selectPage() {
        paginationHelper.selectPageDialog();
    }

    public void showUserMenu(final String postId) {
        showUserMenu(getPostById(Integer.parseInt(postId)));
    }

    public void showReputationMenu(final String postId) {
        showReputationMenu(getPostById(Integer.parseInt(postId)));
    }

    public void showPostMenu(final String postId) {
        showPostMenu(getPostById(Integer.parseInt(postId)));
    }

    public void reportPost(final String postId) {
        reportPost(getPostById(Integer.parseInt(postId)));
    }

    public void insertNick(final String postId) {
        insertNick(getPostById(Integer.parseInt(postId)));
    }

    @Override
    public void insertNick(IBaseForumPost post) {
        if (messagePanel.getVisibility() != View.VISIBLE) {
            showMessagePanel();
        }
        String insert = String.format(Locale.getDefault(), "[snapback]%s[/snapback] [b]%s,[/b] \n", post.getId(), post.getNick());
        messagePanel.insertText(insert);
    }

    public void quotePost(final String text, final String postId) {
        quotePost(text, getPostById(Integer.parseInt(postId)));
    }

    @Override
    public void quotePost(String text, IBaseForumPost post) {
        if (messagePanel.getVisibility() != View.VISIBLE) {
            showMessagePanel();
        }
        String date = Utils.getForumDateTime(Utils.parseForumDateTime(post.getDate()));
        String insert = String.format(Locale.getDefault(), "[quote name=\"%s\" date=\"%s\" post=%S]%s[/quote]\n", post.getNick(), date, post.getId(), text);
        messagePanel.insertText(insert);
    }


    public void deletePost(final String postId) {
        deletePost(getPostById(Integer.parseInt(postId)));
    }

    public void editPost(final String postId) {
        editPost(getPostById(Integer.parseInt(postId)));
    }

    @Override
    public void editPost(IBaseForumPost post) {
        TabManager.getInstance().add(EditPostFragment.newInstance(post.getId(), currentPage.getId(), currentPage.getForumId(), currentPage.getSt(), currentPage.getTitle()));
    }

    public void votePost(final String postId, final boolean type) {
        votePost(getPostById(Integer.parseInt(postId)), type);
    }

    public void setHistoryBody(final String index, final String body) {
        setHistoryBody(Integer.parseInt(index), body);
    }

    public void setHistoryBody(int index, String body) {
        history.get(index).setHtml(body);
    }

    public void copySelectedText(final String text) {
        Utils.copyToClipBoard(text);
    }

    public void toast(final String text) {
        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
    }

    public void log(final String text) {
        int maxLogSize = 1000;
        for (int i = 0; i <= text.length() / maxLogSize; i++) {
            int start = i * maxLogSize;
            int end = (i + 1) * maxLogSize;
            end = end > text.length() ? text.length() : end;
            Log.v("FORPDA_LOG", text.substring(start, end));
        }
    }

    public void showPollResults() {
        tab_url = tab_url.replaceFirst("#[^&]*", "").replace("&mode=show", "").replace("&poll_open=true", "").concat("&mode=show&poll_open=true");
        loadData(NORMAL_ACTION);
    }

    public void showPoll() {
        tab_url = tab_url.replaceFirst("#[^&]*", "").replace("&mode=show", "").replace("&poll_open=true", "").concat("&poll_open=true");
        loadData(NORMAL_ACTION);
    }

    @Override
    public void showUserMenu(IBaseForumPost post) {
        ThemeDialogsHelper.showUserMenu(getContext(), this, post);
    }

    @Override
    public void showReputationMenu(IBaseForumPost post) {
        ThemeDialogsHelper.showReputationMenu(getContext(), this, post);
    }

    @Override
    public void showPostMenu(IBaseForumPost post) {
        ThemeDialogsHelper.showPostMenu(getContext(), this, post);
    }

    @Override
    public void reportPost(IBaseForumPost post) {
        ThemeDialogsHelper.tryReportPost(getContext(), post);
    }

    //Удаление сообщения
    @Override
    public void deletePost(IBaseForumPost post) {
        ThemeDialogsHelper.deletePost(getContext(), post, aBoolean -> {
            if (aBoolean)
                deletePostUi(post);

        });
    }

    public abstract void deletePostUi(IBaseForumPost post);

    //Изменение репутации сообщения
    @Override
    public void votePost(IBaseForumPost post, boolean type) {
        new AlertDialog.Builder(getContext())
                .setMessage((type ? "Повысить" : "Понизить").concat(" репутацию поста пользователя ").concat(post.getNick()).concat("?"))
                .setPositiveButton("Да", (dialog, which) -> ThemeHelper.votePost(s -> toast(s.isEmpty() ? "Неизвестная ошибка" : s), post.getId(), type))
                .setNegativeButton("Отмена", null)
                .show();

    }

    //Изменение репутации пользователя
    @SuppressLint("InflateParams")
    @Override
    public void changeReputation(IBaseForumPost post, boolean type) {
        ThemeDialogsHelper.changeReputation(getContext(), post, type);
    }

}
