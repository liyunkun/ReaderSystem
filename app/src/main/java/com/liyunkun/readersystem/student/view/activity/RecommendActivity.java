package com.liyunkun.readersystem.student.view.activity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.liyunkun.readersystem.MyApp;
import com.liyunkun.readersystem.R;
import com.liyunkun.readersystem.both.module.bean.BookBean;
import com.liyunkun.readersystem.both.module.bean.MyBook;
import com.liyunkun.readersystem.both.module.bean.MyBookDao;
import com.liyunkun.readersystem.read.view.activity.ReadActivity;
import com.liyunkun.readersystem.student.presenter.RecommendPresenter;
import com.liyunkun.readersystem.student.view.adapter.ClassifyListLvAdapter;
import com.liyunkun.readersystem.student.view.intf.IRecommendView;
import com.liyunkun.readersystem.utils.MyConstants;

import java.util.List;

public class RecommendActivity extends AppCompatActivity implements View.OnClickListener, IRecommendView {

    private ListView mLv;
    private TextView mBookShelf;
    private ImageView mGoBack;
    private RecommendPresenter presenter = new RecommendPresenter(this);
    private ClassifyListLvAdapter adapter;
    private MyBookDao myBookDao;
    private ImageView mIv;
    private AnimationDrawable drawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend);
        initView();
        presenter.start();
        showNoData();
    }

    private void initView() {
        mLv = ((ListView) findViewById(R.id.lv));
        mBookShelf = ((TextView) findViewById(R.id.book_shelf));
        mGoBack = ((ImageView) findViewById(R.id.go_back));
        mIv = ((ImageView) findViewById(R.id.iv));
        drawable = ((AnimationDrawable) mIv.getDrawable());

        mGoBack.setOnClickListener(this);
        mBookShelf.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.go_back:
                finish();
                break;
            case R.id.book_shelf:
                Intent intent = new Intent(this, StudentHomeActivity.class);
                intent.putExtra(MyConstants.USER_NAME, StudentHomeActivity.userName);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void updateData2Lv(final List<BookBean> list) {
        hintNoData();
        myBookDao = ((MyApp) getApplication()).daoSession.getMyBookDao();
        adapter = new ClassifyListLvAdapter(list, this, true, myBookDao);
        mLv.setAdapter(adapter);
        mLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BookBean bookBean = list.get(position);
                List<MyBook> myBooks = myBookDao.queryBuilder()
                        .where(MyBookDao.Properties.UserName.eq(MyConstants.userName))
                        .where(MyBookDao.Properties.BookId.eq(bookBean.getBookId())).list();
                if (myBooks != null && myBooks.size() > 0) {
                    Intent intent = new Intent(RecommendActivity.this, ReadActivity.class);
                    intent.putExtra(MyConstants.BOOK_BEAN, bookBean);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(RecommendActivity.this, BookDetailsActivity.class);
                    intent.putExtra("bookBean", bookBean);
                    startActivity(intent);
                }
            }
        });
        adapter.setListener(new ClassifyListLvAdapter.OnListener() {
            @Override
            public void onAddImgClickListener(View v, int position) {
                BookBean bookBean = list.get(position);
                ImageView addImg = (ImageView) v;
                List<MyBook> myBooks = myBookDao.queryBuilder()
                        .where(MyBookDao.Properties.UserName.eq(MyConstants.userName))
                        .where(MyBookDao.Properties.BookId.eq(bookBean.getBookId())).list();
                if (myBooks != null && myBooks.size() > 0) {
                    myBookDao.delete(myBooks.get(0));
                    addImg.setImageResource(R.drawable.add_to);
                } else {
                    myBookDao.save(new MyBook(null, bookBean.getName(), bookBean.getBookImg(), bookBean.getBookId(),
                            bookBean.getAuthor(), bookBean.getFrom(), bookBean.getDescription(),
                            bookBean.getCount(), bookBean.getfCount(), bookBean.getrCount(), bookBean.getClassId(), 0, 0,MyConstants.userName));
                    addImg.setImageResource(R.drawable.add_to_success);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void showNoData() {
        mIv.setVisibility(View.VISIBLE);
        mLv.setVisibility(View.GONE);
        drawable.start();
    }

    private void hintNoData() {
        mIv.setVisibility(View.GONE);
        mLv.setVisibility(View.VISIBLE);
        drawable.stop();
    }
}
